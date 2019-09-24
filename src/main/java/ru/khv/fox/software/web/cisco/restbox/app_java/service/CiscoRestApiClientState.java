/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@ToString
@Getter(AccessLevel.PACKAGE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
final class CiscoRestApiClientState {
	private static final String TOKEN_SERVICES_ENDPOINT = "auth/token-services";
	private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";
	Router router;
	Semaphore semaphore = new Semaphore(1);
	WebClient webClient;      // payload services access
	WebClient authWebClient;  // authentication service access
	AtomicReference<AuthServiceResponse> authServiceResponse = new AtomicReference<>();


	CiscoRestApiClientState(final Router router, final WebClient.Builder webClientBuilder, final String baseUriTemplate) {
		this.router = router;
		// Set common properties. Keep provided builder intact.
		val baseBuilder = webClientBuilder.clone()
		                                  .baseUrl(baseUriTemplate.replace("{hostname}", router.getHost()))
		                                  .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		this.authWebClient = baseBuilder.clone()
		                                .filter(ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()))
		                                .build();
		this.webClient = baseBuilder.clone()
		                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		                            .filter(tokenAuthenticationFilter())
		                            .build();
	}

	// Has side effect as backsetting of the authentication token to router object
	Router getRouter() {
		Optional.ofNullable(getAuthServiceResponse()).map(AuthServiceResponse::getTokenId).ifPresent(router::setToken);
		return router;
	}

	// Adds token authentication header
	private ExchangeFilterFunction tokenAuthenticationFilter() {
		return (clientRequest, next) -> {
			log.debug("Token filter request: {} {}", clientRequest.method(), clientRequest.url());
			// Get authentication token if none exist yet or expired.
			// If token is missing or got cleared or expired on server-side, reauthenticate once.
			return obtainAuthenticationToken()
					.doOnNext(token -> log.debug("router {}: obtained token {}", router.getName(), token))
					.map(token -> ClientRequest.from(clientRequest)
					                           .header(AUTH_TOKEN_HEADER, token)
					                           .build())
					.flatMap(next::exchange)
					.retryWhen(Retry.onlyIf(context -> context.exception() instanceof CiscoRestApiException && ((CiscoRestApiException) context.exception()).getHttpStatus() == HttpStatus.UNAUTHORIZED)
					                .doOnRetry(objectRetryContext -> clearAuthServiceResponse())
					                .retryOnce());
		};
	}

	// Use authentication service web client to request a new token
	private Mono<String> obtainAuthenticationToken() {
		// Token request chain is synchronized per router.
		val lockSupplier = Mono.subscriberContext()
		                       .doOnNext(ctx -> log.trace("ctxS = {}", ctx))
		                       .map(ctx -> ctx.get(Lock.class))
		                       .cast(Lock.class)
		                       .doOnNext(Lock::lock);
		val lockDisposer = Mono.subscriberContext()
		                       .doOnNext(ctx -> log.trace("ctxD = {}", ctx))
		                       .map(ctx -> ctx.get(Lock.class))
		                       .cast(Lock.class)
		                       .doOnNext(Lock::unlock)
		                       .then();
		// Release lock early after obtaining a token, either saved or requested
		return getAuthenticationToken().switchIfEmpty(
				Mono.usingWhen(lockSupplier,
				               lock -> getAuthenticationToken().switchIfEmpty(Mono.defer(() -> getAuthWebClient().post()
				                                                                                                 .uri(TOKEN_SERVICES_ENDPOINT)
				                                                                                                 .contentLength(0)
				                                                                                                 .retrieve()
				                                                                                                 .onStatus(status -> status == HttpStatus.NOT_FOUND,
				                                                                                                           clientResponse -> Mono.defer(() -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), null))))
				                                                                                                 .onStatus(HttpStatus.UNAUTHORIZED::equals,
				                                                                                                           clientResponse -> Mono.defer(() -> Mono.error(new CiscoRestApiException(clientResponse.statusCode()))))
				                                                                                                 .bodyToMono(AuthServiceResponse.class)
				                                                                                                 .onErrorMap(IOException.class, e -> new CiscoServiceException("Network error", e.getLocalizedMessage(), e))
				                                                                                                 .flatMap(this::processAuthServiceResponse)))
				                                               .doOnNext(r -> lock.unlock()),
				               complete -> lockDisposer,
				               error -> lockDisposer,
				               cancel -> lockDisposer)
				    .subscriberContext(ctx -> ctx.put(Lock.class, new Lock())));
	}

	// Check authentication token server-side state
	Mono<AuthServiceResponse> checkAuthenticationToken() {
		return getAuthenticationTokenUri()
				.switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("No authentication token information available"))))
				.flatMap(tokenUri -> getWebClient()
						.get()
						.uri(tokenUri)
						.retrieve()
						.bodyToMono(AuthServiceResponse.class)
						.flatMap(this::cacheAuthServiceResponse));
		// Token lifetime is prolonged by this operation, so it may be useful to update cached auth response
	}

	// Destroy authentication token in router auth service.
	// Authentication errors are ignored as token may had already expired.
	Mono<Void> destroyAuthenticationToken() {
		return getAuthenticationTokenUri()
				.flatMap(tokenUri -> getWebClient().delete()
				                                   .uri(tokenUri)
				                                   .retrieve()
				                                   .onStatus(HttpStatus.UNAUTHORIZED::equals,
				                                             clientResponse -> Mono.empty())
				                                   .bodyToMono(Void.class)
				                                   .then(clearAuthentication()));
	}


	// Re-authenticate client (destroy current authentication token, if any, and try to acquire new)
	Mono<Void> reAuthentication() {
		return destroyAuthenticationToken().then(obtainAuthenticationToken()).then();
	}

	// Get authentication token
	private Mono<String> getAuthenticationToken(final Mono<AuthServiceResponse> authServiceResponse) {
		return authServiceResponse.filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
		                          .map(AuthServiceResponse::getTokenId);
	}

	@Nullable
	private AuthServiceResponse getAuthServiceResponse() {
		return authServiceResponse.get();
	}

	private void setAuthServiceResponse(@Nullable final AuthServiceResponse authServiceResponse) {
		log.debug("set authservice response to {} for router {}", authServiceResponse, router.getName());
		this.authServiceResponse.set(authServiceResponse);
	}

	private void clearAuthServiceResponse() {
		log.debug("clear authservice response for router {}", router.getName());
		setAuthServiceResponse(null);
	}

	private Mono<AuthServiceResponse> cacheAuthServiceResponse(final AuthServiceResponse authServiceResponse) {
		return Mono.fromRunnable(() -> setAuthServiceResponse(authServiceResponse))
		           .thenReturn(authServiceResponse);
	}

	private Mono<String> processAuthServiceResponse(final AuthServiceResponse authServiceResponse) {
		return getAuthenticationToken(cacheAuthServiceResponse(authServiceResponse))
				.switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("Authorization service didn't provide token"))));
	}


	private Mono<Void> clearAuthentication() {
		return Mono.fromRunnable(this::clearAuthServiceResponse);
	}


	private Mono<AuthServiceResponse> retrieveAuthServiceResponse() {
		return Mono.fromCallable(this::getAuthServiceResponse);
	}


	private Mono<String> getAuthenticationToken() {
		return getAuthenticationToken(retrieveAuthServiceResponse());
	}

	// Return token URI with opaque id
	private Mono<URI> getAuthenticationTokenUri() {
		return retrieveAuthServiceResponse().map(AuthServiceResponse::getLink)
		                                    .flatMap(Mono::justOrEmpty);
	}


	// Solely purprose of this class is to bind the lock state to executing chain.
	// Therefore instances are subscriber-bound through context.
	// This class have access to container instance fields (semaphore and router).
	private final class Lock {
		private boolean holding = false;

		private synchronized void lock() {
			log.trace("Acquire lock for router {}", router.getName());
			if (!holding) {
				try {
					semaphore.acquire();
				} catch (InterruptedException ex) {
					throw new RuntimeException("Semaphore waiting interrupted", ex);
				}
				holding = true;
				log.trace("Lock for router {} acquired", router.getName());
			} else
				log.warn("Lock has been ALREADY HELD for router {}", router.getName());
		}

		private synchronized void unlock() {
			log.trace("Release lock for router {}", router.getName());
			if (holding) {
				holding = false;
				if (semaphore.availablePermits() == 0)
					semaphore.release();
				else
					log.warn("Semaphore WAS NOT ACQUIRED for router {}", router.getName());
				log.trace("Lock for router {} released", router.getName());
			} else
				log.trace("Lock isn't held");
		}
	}
}

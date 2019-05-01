/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import reactor.retry.Retry;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

@Slf4j
@ToString
@Getter(AccessLevel.PACKAGE)
final class CiscoRestApiClientState {
	private static final String BASE_URI_TEMPLATE = "https://{hostname}:55443/api/v1/";  // TODO make configurable?
	private static final String TOKEN_SERVICES_ENDPOINT = "auth/token-services";
	private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";
	@NonNull
	private final Router router;
	@NonNull
	private final Semaphore semaphore = new Semaphore(1);
	@NonNull
	private final WebClient webClient;      // payload services access
	@NonNull
	private final WebClient authWebClient;  // authentication service access
	@Nullable
	private volatile AuthServiceResponse authServiceResponse;


	private CiscoRestApiClientState(@NonNull final Router router,
	                                @NonNull final WebClient.Builder webClientBuilder,
	                                @NonNull final ClientHttpConnector clientHttpConnector) {
		this.router = router;
		// keep provided builder intact
		this.authWebClient = webClientBuilder.clone()
		                                     .clientConnector(clientHttpConnector)
		                                     .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
		                                     .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		                                     .filter(ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()))
		                                     .filter(requestLoggingFilter())    // TODO for debugging
		                                     .build();
		// TODO is content-type required on all requests?
		this.webClient = webClientBuilder.clone()
		                                 .clientConnector(clientHttpConnector)
		                                 .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
		                                 .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		                                 .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		                                 .filter(tokenAuthenticationFilter())
		                                 .filter(requestLoggingFilter())    // TODO for debugging
		                                 .build();
	}

	// Request logger
	@NonNull
	private static ExchangeFilterFunction requestLoggingFilter() {
		return (clientRequest, next) -> {
			log.trace("Request: {} {}", clientRequest.method(), clientRequest.url());
			clientRequest.headers()
			             .forEach((name, values) -> values.forEach(value -> log.trace("{}={}", name, value)));
			return next.exchange(clientRequest);
		};
	}

	@NonNull
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
	@NonNull
	private Mono<String> obtainAuthenticationToken() {
		// Token request chain is synchronized per router.
		val lockSupplier = Mono.subscriberContext()
		                       .doOnNext(ctx -> log.trace("ctx1 = {}", ctx))
		                       .map(ctx -> ctx.get(Lock.class))
		                       .cast(Lock.class)
		                       .doOnNext(Lock::lock);
		val lockDisposer = Mono.subscriberContext()
		                       .doOnNext(ctx -> log.trace("ctx3 = {}", ctx))
		                       .map(ctx -> ctx.get(Lock.class))
		                       .cast(Lock.class)
		                       .doOnNext(Lock::unlock)
		                       .then();
		// Release lock early after obtaining a token, either saved or requested
		return getAuthenticationToken().switchIfEmpty(
				Mono.usingWhen(lockSupplier,
				               lock -> getAuthenticationToken().switchIfEmpty(getAuthWebClient().post()
				                                                                                .uri(TOKEN_SERVICES_ENDPOINT)
				                                                                                .retrieve()
				                                                                                .onStatus(status -> status == HttpStatus.NOT_FOUND,
				                                                                                          clientResponse -> Mono.defer(() -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), null))))
				                                                                                .onStatus(HttpStatus.UNAUTHORIZED::equals,
				                                                                                          clientResponse -> Mono.defer(() -> Mono.error(new CiscoRestApiException(clientResponse.statusCode()))))
				                                                                                .bodyToMono(AuthServiceResponse.class)
				                                                                                .flatMap(this::processAuthServiceResponse))
				                                               .doOnNext(r -> lock.unlock()),
				               complete -> lockDisposer,
				               error -> lockDisposer,
				               cancel -> lockDisposer)
				    .subscriberContext(ctx -> ctx.put(Lock.class, new Lock())));
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

	// Check authentication token server-side state
	@NonNull
	Mono<AuthServiceResponse> checkAuthenticationToken() {
		return getAuthenticationTokenUri()
				.switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("No authentication token information available"))))
				.flatMap(tokenUri -> getWebClient()
						.get()
						.uri(tokenUri)
						.retrieve()
						.bodyToMono(AuthServiceResponse.class)
						.map(asr -> {
							processAuthServiceResponse(asr);
							return asr;
						}));
		// Token lifetime is prolonged by this operation, so it may be useful to update cached auth response
	}

	// Destroy authentication token in router auth service.
	// Authentication errors are ignored as token may had already expired.
	@NonNull
	Mono<Void> destroyAuthenticationToken() {
		return getAuthenticationTokenUri()
				.flatMap(tokenUri -> getWebClient()
						.delete()
						.uri(tokenUri)
						.retrieve()
						.onStatus(HttpStatus.UNAUTHORIZED::equals,
						          clientResponse -> Mono.empty())
						.bodyToMono(Void.class)
						.then(clearAuthentication()));
	}


	// Re-authenticate client (destroy current authentication token, if any, and try to acquire new)
	@NonNull
	Mono<Void> reAuthentication() {
		return destroyAuthenticationToken().then(obtainAuthenticationToken()).then();
	}

	@NonNull
	private Mono<String> getAuthenticationToken() {
		return getAuthServiceResponse()
		           .filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
		           .map(AuthServiceResponse::getTokenId);
	}

	// Return token URI with opaque id
	@NonNull
	private Mono<URI> getAuthenticationTokenUri() {
		return getAuthServiceResponse()
		           .map(AuthServiceResponse::getLink)
		           .flatMap(Mono::justOrEmpty);
	}

	@NonNull
	private Mono<AuthServiceResponse> getAuthServiceResponse() {
		return Mono.defer(() -> Mono.justOrEmpty(authServiceResponse));
	}

	private synchronized void setAuthServiceResponse(@Nullable final AuthServiceResponse authServiceResponse) {
		log.debug("set authservice response to {} for router {}", authServiceResponse, router.getName());
		this.authServiceResponse = authServiceResponse;
	}

	private synchronized void clearAuthServiceResponse() {
		log.debug("clear authservice response for router {}", router.getName());
		this.authServiceResponse = null;
	}

	private Mono<String> processAuthServiceResponse(@NonNull final AuthServiceResponse authServiceResponse) {
		setAuthServiceResponse(authServiceResponse);
		return getAuthenticationToken()
				.switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("Authorization service didn't provide token"))));
	}

	@NonNull
	private Mono<Void> clearAuthentication() {
		return Mono.fromCallable(() -> {
			clearAuthServiceResponse();
			return null;
		});
	}


	/**
	 * Get Cisco RESTful Api Client State object factory instance.
	 *
	 * @param webClientBuilder    Spring WebClient builder
	 * @param sslIgnoreValidation Ignore SSL/TLS certificate validation, if true
	 *
	 * @return Factory object instance
	 *
	 * @throws SSLException On SSL configuration errors
	 */
	static Factory getFactory(@NonNull final WebClient.Builder webClientBuilder,
	                          final boolean sslIgnoreValidation) throws SSLException {
		return Factory.getInstance(webClientBuilder, sslIgnoreValidation);
	}


	// Helper class to build router client state objects
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static final class Factory {
		@NonNull
		private final WebClient.Builder webClientBuilder;
		@NonNull
		private final ClientHttpConnector httpConnector;


		@NonNull
		private static Factory getInstance(@NonNull final WebClient.Builder webClientBuilder,
		                                   final boolean sslIgnoreValidation) throws SSLException {
			val tcpClient = TcpClient.create()
			                         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
			                         .option(ChannelOption.TCP_NODELAY, true)
			                         .option(ChannelOption.SO_KEEPALIVE, true)
			                         .doOnConnected(connection ->
					                                        connection.addHandlerLast(new ReadTimeoutHandler(30))       // to send the request
					                                                  .addHandlerLast(new WriteTimeoutHandler(10)));    // to wait for the response
			var httpClient = HttpClient.from(tcpClient);
			if (sslIgnoreValidation) {
				val sslContext = SslContextBuilder.forClient()
				                                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
				                                  .build();
				httpClient = httpClient.secure(t -> t.sslContext(sslContext));
			}
			return new Factory(webClientBuilder, new ReactorClientHttpConnector(httpClient));
		}

		CiscoRestApiClientState createRestApiClientState(@NonNull final Router router) {
			return new CiscoRestApiClientState(router, webClientBuilder, httpConnector);
		}
	}
}

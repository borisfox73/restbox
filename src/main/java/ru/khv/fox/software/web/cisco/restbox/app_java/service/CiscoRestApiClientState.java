/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import reactor.retry.Retry;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Getter(AccessLevel.PACKAGE)
final class CiscoRestApiClientState {
	private static final String BASE_URI_TEMPLATE = "https://{hostname}:55443/api/v1";  // TODO make configurable?
	private static final String TOKEN_SERVICES_ENDPOINT = "/auth/token-services";
	private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";
	@NonNull
	private final Router router;
	@NonNull
	private final WebClient webClient;      // payload services access
	@NonNull
	private final WebClient authWebClient;  // authentication service access
	@Nullable
	private volatile AuthServiceResponse authServiceResponse;


	private CiscoRestApiClientState(@NonNull final Router router,
	                                @NonNull final WebClient.Builder webClientBuilder,
	                                @Nullable final ClientHttpConnector clientHttpConnector) {
		this.router = router;
		// keep provided builder intact
		val webClientBuilder1 = webClientBuilder.clone();
		val webClientBuilder2 = webClientBuilder.clone();
		val authBuilder = webClientBuilder1.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		                                   .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
		                                   .filter(ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()))
		                                   .filter(requestLoggingFilter());   // TODO for debugging
		// TODO is content-type required on all requests?
		val workBuilder = webClientBuilder2.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		                                   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		                                   .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
		                                   .filter(tokenAuthenticationFilter())
		                                   .filter(requestLoggingFilter());   // TODO for debugging
		if (clientHttpConnector != null) {
			authBuilder.clientConnector(clientHttpConnector);
			workBuilder.clientConnector(clientHttpConnector);
		}
		this.authWebClient = authBuilder.build();
		this.webClient = workBuilder.build();
	}

	// Request logger
	@NonNull
	private static ExchangeFilterFunction requestLoggingFilter() {
		return (clientRequest, next) -> {
			log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
			clientRequest.headers()
			             .forEach((name, values) -> values.forEach(value -> log.debug("{}={}", name, value)));
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
					.doOnNext(token -> log.debug("obtained token: {}", token))
					.map(token -> ClientRequest.from(clientRequest)
					                           .header(AUTH_TOKEN_HEADER, token)
					                           .build())
					.flatMap(next::exchange)
					.retryWhen(Retry.onlyIf(context -> context.exception() instanceof CiscoRestApiAuthorizationException && ((CiscoRestApiAuthorizationException) context.exception()).getHttpStatus() == HttpStatus.UNAUTHORIZED)
					                .doOnRetry(objectRetryContext -> clearAuthentication())
					                .retryOnce());
		};
	}

	// TODO synchronize?
	// Use authentication service web client to request a new token
	@NonNull
	private Mono<String> obtainAuthenticationToken() {
		log.debug("obtain auth token");
		return getAuthenticationToken().switchIfEmpty(Mono.defer(() -> getAuthWebClient()
				.post()
				.uri(TOKEN_SERVICES_ENDPOINT)
				.retrieve()
				.onStatus(HttpStatus.UNAUTHORIZED::equals,
				          clientResponse -> Mono.error(new CiscoRestApiAuthorizationException(clientResponse.statusCode())))
				.bodyToMono(AuthServiceResponse.class)
				.flatMap(this::processAuthServiceResponse)));
	}

	// Check authentication token server-side state
	@NonNull
	Mono<AuthServiceResponse> checkAuthenticationToken() {
		return getAuthenticationTokenUri()
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalStateException("No authentication token information available"))))
				.flatMap(tokenUri -> getWebClient()
						.get()
						.uri(tokenUri)
						.retrieve()
						.bodyToMono(AuthServiceResponse.class));
		// TODO token lifetime is prolonged by this operation, so it may be useful to update cached auth response
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
						.doOnSuccess(none -> clearAuthentication()));
	}

	@NonNull
	private Mono<String> getAuthenticationToken() {
		return Mono.justOrEmpty(authServiceResponse)
		           .filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
		           .map(AuthServiceResponse::getTokenId);
	}

	// Return token URI with opaque id
	@NonNull
	private Mono<URI> getAuthenticationTokenUri() {
		return Mono.justOrEmpty(authServiceResponse)
		           .map(AuthServiceResponse::getLink)
		           .flatMap(Mono::justOrEmpty);
	}

	private synchronized void clearAuthentication() {
		log.debug("clear authservice response");
		this.authServiceResponse = null;
	}

	private synchronized void setAuthServiceResponse(@Nullable final AuthServiceResponse authServiceResponse) {
		log.debug("set authservice response to {}", authServiceResponse);
		this.authServiceResponse = authServiceResponse;
	}

	private Mono<String> processAuthServiceResponse(@NonNull final AuthServiceResponse authServiceResponse) {
		setAuthServiceResponse(authServiceResponse);
		return getAuthenticationToken().switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoRestApiAuthorizationException(HttpStatus.UNAUTHORIZED, "Authorization service didn't provide token"))));
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
		@Nullable
		private final ClientHttpConnector httpConnector;


		@NonNull
		private static Factory getInstance(@NonNull final WebClient.Builder webClientBuilder,
		                                   final boolean sslIgnoreValidation) throws SSLException {
			ReactorClientHttpConnector httpConnector;
			if (sslIgnoreValidation) {
				val sslContext = SslContextBuilder.forClient()
				                                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
				                                  .build();
				val httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
				httpConnector = new ReactorClientHttpConnector(httpClient);
			} else
				httpConnector = null;
			return new Factory(webClientBuilder, httpConnector);
		}

		CiscoRestApiClientState createRestApiClientState(@NonNull final Router router) {
			return new CiscoRestApiClientState(router, webClientBuilder, httpConnector);
		}
	}
}

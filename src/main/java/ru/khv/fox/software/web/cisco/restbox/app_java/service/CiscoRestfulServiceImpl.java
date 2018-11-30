/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.HostnameServiceResponse;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CiscoRestfulServiceImpl implements CiscoRestfulService {
	private static final String BASE_URI_TEMPLATE = "https://{hostname}:55443/api/v1";
	// private static final String BASE_URI_TEMPLATE = "http://{hostname}:55080/api/v1";
	private static final String TOKEN_SERVICES_ENDPOINT = "/auth/token-services";
	private static final String HOSTNAME_SERVICES_ENDPOINT = "/global/host-name";
	private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";

	@NonNull
	private final Map<String, RouterClientState> routers;


	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class RouterClientStateFactory {
		@NonNull
		private final WebClient.Builder webClientBuilder;
		@Nullable
		private final ClientHttpConnector httpConnector;


		@NonNull
		static RouterClientStateFactory getInstance(@NonNull final WebClient.Builder webClientBuilder,
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
			return new RouterClientStateFactory(webClientBuilder, httpConnector);
		}

		RouterClientState getRouterClientState(@NonNull final Router router) {
			return new RouterClientState(router, webClientBuilder, httpConnector);
		}
	}

	@Getter
	private static final class RouterClientState {
		@NonNull
		private final Router router;
		@NonNull
		private final WebClient webClient;      // payload services access
		@NonNull
		private final WebClient authWebClient;  // authentication service access
		@Nullable
		private volatile AuthServiceResponse authServiceResponse;


		private RouterClientState(@NonNull final Router router,
		                          @NonNull final WebClient.Builder webClientBuilder,
		                          @Nullable final ClientHttpConnector clientHttpConnector) {
			this.router = router;
			// keep provided builder intact
			val webClientBuilder1 = webClientBuilder.clone();
			val webClientBuilder2 = webClientBuilder.clone();
			val authBuilder = webClientBuilder1.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			                                   .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
			                                   .filter(ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()))
			                                   .filter(logRequest());   // TODO for debugging
			// TODO is content-type required on all requests?
			val workBuilder = webClientBuilder2.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			                                   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			                                   .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
			                                   .filter(tokenAuthenticationFilter())
			                                   .filter(logRequest());   // TODO for debugging
			if (clientHttpConnector != null) {
				authBuilder.clientConnector(clientHttpConnector);
				workBuilder.clientConnector(clientHttpConnector);
			}
			this.authWebClient = authBuilder.build();
			this.webClient = workBuilder.build();
		}

/*
		@NonNull
		private void buildWebClients(@NonNull final WebClient.Builder webClientBuilder,
		                             @Nullable final ClientHttpConnector clientHttpConnector) {
			val authBuilder = webClientBuilder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			                              .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
										  .filter(ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()))
										  .filter(logRequest());   // TODO for debugging
			// TODO is content-type required on all requests?
			val workBuilder = webClientBuilder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			                                  .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			                                  .baseUrl(BASE_URI_TEMPLATE.replace("{hostname}", router.getHost()))
			                                  .filter(tokenAuthenticationFilter())
			                                  .filter(logRequest());   // TODO for debugging
			if (clientHttpConnector != null) {
				authBuilder.clientConnector(clientHttpConnector);
				workBuilder.clientConnector(clientHttpConnector);
			}
			this.authWebClient = authBuilder.build();
			this.webClient = workBuilder.build();
		}
*/

/*
		@NonNull
		private ExchangeFilterFunction basicAuthenticationFilter() {
			return (clientRequest, next) -> {
				log.debug("Basic Auth filter request: {} {}", clientRequest.method(), clientRequest.url());
				log.debug("username: {}, password: {}", router.getUsername(), router.getPassword());
				// Delegate token services endpoint authentication to basic authentication filter
				if (clientRequest.url().getPath().endsWith(TOKEN_SERVICES_ENDPOINT))
					return ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()).filter(clientRequest, next);
				// TODO это выполняется в потоке main, поэтому authentication token здесь берётся пустой.
				//  заполняется он в doOnNext, который выполняется в execution stage, и в другом потоке (ctor-http-nio-4), после завершения всех запросов.
				//  Почему?
				// all other endpoints got the token authentication
				return next.exchange(getAuthenticationToken().map(token -> ClientRequest.from(clientRequest)
				                                                 .header(AUTH_TOKEN_HEADER, token)
				                                                 .build())
				                               .orElse(clientRequest));
			};
		}
*/

		/*
				@NonNull
				private ExchangeFilterFunction tokenAuthenticationFilter() {
					return (clientRequest, next) -> {
						log.debug("Auth filter request: {} {}", clientRequest.method(), clientRequest.url());
						log.debug("get path: {}, tse? {}", clientRequest.url().getPath(), clientRequest.url().getPath().endsWith(TOKEN_SERVICES_ENDPOINT));
						log.debug("username: {}, password: {}", router.getUsername(), router.getPassword());
						log.debug("token: {}", getAuthenticationToken());
						// Delegate token services endpoint authentication to basic authentication filter
						if (clientRequest.url().getPath().endsWith(TOKEN_SERVICES_ENDPOINT))
							return ExchangeFilterFunctions.basicAuthentication(router.getUsername(), router.getPassword()).filter(clientRequest, next);
						// TODO это выполняется в потоке main, поэтому authentication token здесь берётся пустой.
						//  заполняется он в doOnNext, который выполняется в execution stage, и в другом потоке (ctor-http-nio-4), после завершения всех запросов.
						//  Почему?
						// all other endpoints got the token authentication
						return next.exchange(getAuthenticationToken().map(token -> ClientRequest.from(clientRequest)
																								.header(AUTH_TOKEN_HEADER, token)
																								.build())
																	 .orElse(clientRequest));
					};
				}
		*/
		@NonNull
		private ExchangeFilterFunction tokenAuthenticationFilter() {
			return (clientRequest, next) -> {
				log.debug("Token filter request: {} {}", clientRequest.method(), clientRequest.url());
/*
				return next.exchange(getAuthenticationToken().map(token -> ClientRequest.from(clientRequest)
				                                                                        .header(AUTH_TOKEN_HEADER, token)
				                                                                        .build())
				                                             .orElse(clientRequest));
*/
				// Get authentication token if none exist or expired.
				// If token is missing on server-side, reauthenticate once.
				return obtainAuthenticationToken()
						.doOnNext(token -> log.debug("obtained token: {}", token))
						.map(token -> ClientRequest.from(clientRequest)
						                           .header(AUTH_TOKEN_HEADER, token)
						                           .build())
						.flatMap(next::exchange)
						.retryWhen(Retry.onlyIf(context -> context.exception() instanceof CiscoRestfulApiAuthorizationException && ((CiscoRestfulApiAuthorizationException) context.exception()).getHttpStatus() == HttpStatus.UNAUTHORIZED)
						                .doOnRetry(objectRetryContext -> clearAuthentication())
						                .retryOnce());
			};
		}

		// TODO synchronize?
		@NonNull
		private Mono<String> obtainAuthenticationToken() {
			log.debug("obtain auth token");
			return getAuthenticationTokenMono().switchIfEmpty(Mono.defer(() -> getAuthWebClient()
					.post()
					.uri(TOKEN_SERVICES_ENDPOINT)
					.retrieve()
					.onStatus(HttpStatus.UNAUTHORIZED::equals,
					          clientResponse -> Mono.error(new CiscoRestfulApiAuthorizationException(clientResponse.statusCode())))
					.bodyToMono(AuthServiceResponse.class)
					.flatMap(this::processAuthServiceResponse)));
		}


		@NonNull
		private static ExchangeFilterFunction logRequest() {
			return (clientRequest, next) -> {
				log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
				clientRequest.headers()
				             .forEach((name, values) -> values.forEach(value -> log.debug("{}={}", name, value)));
				return next.exchange(clientRequest);
			};
		}

/*
		private void setAuthServiceResponse(@Nullable final AuthServiceResponse authServiceResponse) {
			log.debug("set auth service response: {}", authServiceResponse);
			this.authServiceResponse = authServiceResponse;
		}
*/

		// Return non-expired authentication token, if any
/*
		@NonNull
		private Optional<String> getAuthenticationToken() {
//			log.debug("get auth token: service response = {}", authServiceResponse);
			return Optional.ofNullable(authServiceResponse)
			        .filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
			        .map(AuthServiceResponse::getTokenId);
		}
*/

		@NonNull
		private Mono<String> getAuthenticationTokenMono() {
			return Mono.justOrEmpty(authServiceResponse)
			           .filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
			           .map(AuthServiceResponse::getTokenId);
		}

		// Return token URI with opaque id
		@NonNull
		private Optional<URI> getAuthenticationTokenUri() {
			return Optional.ofNullable(authServiceResponse)
			               .filter(resp -> resp.getExpiryTime().isAfter(LocalDateTime.now()))
			               .flatMap(asResp -> Optional.ofNullable(asResp.getLink()));
			// TODO is expiration checking required here?
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
			return getAuthenticationTokenMono().switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoRestfulApiAuthorizationException(HttpStatus.UNAUTHORIZED, "Authorization service didn't provide token"))));
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	CiscoRestfulServiceImpl(@NonNull final Collection<Router> routerCollection,
	                        @NonNull final WebClient.Builder webClientBuilder,
	                        @Value("#{appProperties.sslIgnoreValidation}") final boolean sslIgnoreValidation) throws SSLException {
		val routerClientStateFactory = RouterClientStateFactory.getInstance(webClientBuilder, sslIgnoreValidation);
		routers = routerCollection.stream()
		                          .map(routerClientStateFactory::getRouterClientState)
		                          .collect(Collectors.toUnmodifiableMap(state -> state.getRouter().getId(), state -> state));
		log.trace("Router client state map: {}", routers);
	}


	@NonNull
	@Override
	public Map<String, Router> getRouters() {
		return routers.values()
		              .stream()
		              .map(RouterClientState::getRouter)
		              .collect(Collectors.toMap(Router::getId, router -> router));
	}

/*
	@NonNull
	@Override
	public Mono<AuthServiceResponse> authenticate(@NonNull final String routerId) {
		return Mono.justOrEmpty(routers.get(routerId))
		           .flatMap(CiscoRestfulServiceImpl::obtainAuthenticationToken);
	}
*/

	@NonNull
	private static Mono<String> obtainAuthenticationToken(@NonNull final RouterClientState clientState) {
		return clientState.getWebClient()
		                  .post()
		                  .uri(TOKEN_SERVICES_ENDPOINT)
		                  .retrieve()
		                  .bodyToMono(AuthServiceResponse.class)
		                  .flatMap(clientState::processAuthServiceResponse);
//				          .doOnNext(clientState::setAuthServiceResponse).doOnNext(x -> log.debug("authenticated1: {}", x)).doOnSuccess(x -> log.debug("authenticated2: {}", x));   // store for further authentications
		// TODO doonnext triggers too late, after payload GET request got issued with still empty token
	}

	@NonNull
	@Override
	public Mono<AuthServiceResponse> checkAuthToken(@NonNull final String routerId) {
		return Mono.justOrEmpty(routers.get(routerId))
		           .flatMap(CiscoRestfulServiceImpl::checkAuthenticationToken);
	}

	@NonNull
	private static Mono<AuthServiceResponse> checkAuthenticationToken(@NonNull final RouterClientState clientState) {
		return clientState.getAuthenticationTokenUri().map(tokenUri -> clientState.getWebClient()
		                                                                          .get()
		                                                                          .uri(tokenUri)
		                                                                          .retrieve()
		                                                                          .bodyToMono(AuthServiceResponse.class))
		                  .orElse(Mono.defer(() -> Mono.error(new IllegalStateException("No authentication token information available"))));
	}

	@NonNull
	@Override
	public Mono<Void> invalidateAuthToken(@NonNull final String routerId) {
		return Mono.justOrEmpty(routers.get(routerId))
		           .flatMap(CiscoRestfulServiceImpl::invalidateAuthenticationToken);
	}

	@NonNull
	private static Mono<Void> invalidateAuthenticationToken(@NonNull final RouterClientState clientState) {
		return clientState.getAuthenticationTokenUri().map(tokenUri -> clientState.getWebClient()
		                                                                          .delete()
		                                                                          .uri(tokenUri)
		                                                                          .retrieve()
		                                                                          .bodyToMono(Void.class)
		                                                                          .doOnSuccess(none -> clientState.clearAuthentication()))
		                  .orElse(Mono.empty());
	}

	// TODO implement seamless [re-]authentication on payload methods
	//      should automatically obtain a new token if there's no one yet or previous has expired or authentication failure experienced
	//      re-authentication when still valid token is present should be done only once to avoid authentication loop.

	@NonNull
	@Override
	public Mono<HostnameServiceResponse> getHostname(@NonNull final String routerId) {
		return Mono.justOrEmpty(routers.get(routerId))
		           .flatMap(CiscoRestfulServiceImpl::retrieveHostname);
	}

	@NonNull
	private static Mono<HostnameServiceResponse> retrieveHostname(@NonNull final RouterClientState clientState) {
		// если токена ещё нет, или он устарел, перед вызовом полезного запроса выполняется запрос аутентификации и получение токена.
		// при ошибках аутентификации повторы не делаются.
		// если токен получен успешно (либо был), делается полезный запрос.
		// при ошибках аутентификации, если токен был ранее (не только что получен) делается повторная аутентификация и полезный запрос повторяется, но только один раз.
//		val authToken = clientState.getAuthenticationToken();
		val webClient = clientState.getWebClient();
//		return doAuthentication(clientState).doOnNext(x -> log.debug("just authenticated? {}", x)).then(
		return webClient
				.get()
				.uri(HOSTNAME_SERVICES_ENDPOINT)
				.retrieve()
				.bodyToMono(HostnameServiceResponse.class);
		// как протащить результат doAuthentication (true) в onError(401) полезного запроса, чтобы исключить повтор аутентификации, т.к. она только что была выполнена?
		// использовать compose?
	}

	@Data
	private static class AuthenticationState {
		private final AuthServiceResponse authServiceResponse;
		private boolean justAuthenticated;
	}

/*
	// return true if just have been authenticated, false if valid token was already existent
	private static Mono<Boolean> doAuthentication(@NonNull final RouterClientState clientState) {
		return Mono.justOrEmpty(clientState.getAuthenticationToken()).map(x -> false)
		           .switchIfEmpty(obtainAuthenticationToken(clientState).thenReturn(true));
	}
*/

	// TODO continue.
}

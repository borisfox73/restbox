/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginRequest;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginResponse;

import javax.annotation.Nonnull;

// TODO переделать на handler функцию
@Slf4j
@RequiredArgsConstructor
@RestController
class Login {

	//	@Nonnull private final AppProperties appProperties;
	@Nonnull private final UserDetailsRepositoryReactiveAuthenticationManager authenticationManager;
	@Nonnull private final AccountStatusUserDetailsChecker accountChecker = new AccountStatusUserDetailsChecker();
	@Nonnull private final JwtHandler jwtHandler;


	// TODO should return 201 and JWT token in "token" field
	// or 401 and "authentication failed" in "message".
	@SuppressWarnings("unused")
	@PostMapping(path = "/login")
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	Mono<LoginResponse> login(@RequestBody final LoginRequest loginParameters, @Nonnull final ServerWebExchange exchange) {
		val username = loginParameters.getUsername();
		val password = loginParameters.getPassword();
		log.debug("username = {}, password = {}", username, password);
		log.debug("request path = {}", exchange.getRequest().getPath());
		log.debug("request uri = {}", exchange.getRequest().getURI());
		log.debug("request uri host = {}", exchange.getRequest().getURI().getHost());
		val token = new UsernamePasswordAuthenticationToken(username, password);
		// authenticate only check user existence and password match
		// account and credentials validity state should be checked explicitly
		return authenticationManager.authenticate(token)
//		                            .publishOn(Schedulers.elastic())    // TODO needed?
                                    .map(Authentication::getPrincipal)
                                    .cast(UserDetails.class)
/*
		                            .handle((user, sink) -> {
			                            try {
				                            accountChecker.check(user);
				                            sink.next(createLoginResponse(user));
			                            } catch (Exception e) {
				                            sink.error(e);
			                            }
		                            });
*/
                                    .doOnNext(accountChecker::check)
//									.map(userDetails -> createLoginResponse(userDetails, exchange));
                                    .map(userDetails -> jwtHandler.createJwt(userDetails, exchange))
                                    .map(LoginResponse::of);
/*
		                            .filter(UserDetails::isEnabled)
		                            .switchIfEmpty(Mono.defer(() -> Mono.error(new DisabledException("Account disabled"))))
		                            .filter(UserDetails::isAccountNonLocked)
		                            .switchIfEmpty(Mono.defer(() -> Mono.error(new LockedException("Account locked"))))
		                            .filter(UserDetails::isAccountNonExpired)
		                            .switchIfEmpty(Mono.defer(() -> Mono.error(new AccountExpiredException("Account Expired"))))
		                            .filter(UserDetails::isCredentialsNonExpired)
		                            .switchIfEmpty(Mono.defer(() -> Mono.error(new CredentialsExpiredException("Credentials Expired"))))
		                            .flatMap(Login::createLoginResponse);
*/
	}

/*
	@Nonnull
	private LoginResponse createLoginResponse(@Nonnull final UserDetails user, @Nonnull final ServerWebExchange exchange) {
		log.trace("make JWT with username '{}' and authorities {}", user.getUsername(), user.getAuthorities());
		val jwtProperties = appProperties.getJwt();
		val jwtId = UUID.randomUUID().toString();
		val jwtSubject = user.getUsername();
		val jwtSecret = jwtProperties.getSecret();
		val jwtIssuedAt = Instant.now();
		val jwtExpiration = jwtIssuedAt.plus(jwtProperties.getTimeToLive());
		// claims that always added
		val jwtBuilder = Jwts.builder()
		                     .setId(jwtId)
		                     .setSubject(jwtSubject)
		                     .setIssuedAt(Date.from(jwtIssuedAt))
		                     .setNotBefore(Date.from(jwtIssuedAt))
		                     .setExpiration(Date.from(jwtExpiration));
		// optional claims
		computeIssuer(exchange).ifPresent(jwtBuilder::setIssuer);
		jwtProperties.getAudience().ifPresent(jwtBuilder::setAudience);
		jwtBuilder.addClaims(Collections.singletonMap("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray()));
		val jwtToken = jwtBuilder.signWith(SignatureAlgorithm.HS512, jwtSecret)
		                         .compact();
		return LoginResponse.of(jwtToken);
	}
*/

/*
	@Nonnull
	private static Optional<String> computeIssuer(@Nonnull final ServerWebExchange exchange) {
		val requestUri = exchange.getRequest().getURI();
		val sb = new StringBuilder();
		if (requestUri.getScheme() != null)
			sb.append(requestUri.getScheme()).append(":");
		if (requestUri.getHost() != null)
			sb.append("//").append(requestUri.getHost());
		if (requestUri.getPort() != -1)
			sb.append(":").append(requestUri.getPort());
		return sb.length() > 0 ? Optional.of(sb.toString()) : Optional.empty();
	}
*/

	// TODO добавить в authentication manager два authentication provider:
	// + первый типа UsernamePassword для аутентификации при /login с поиском по UserDetailsService
	// второй типа Token для заполнения контекста авторизации в security фильтре, обрабатывающем заголовок HTTP Authorization.

	// обработка исключений:
	// https://stackoverflow.com/questions/24292373/spring-boot-rest-controller-how-to-return-different-http-status-codes
	// https://spring.io/guides/tutorials/bookmarks/
	// http://engineering.pivotal.io/post/must-know-spring-boot-annotations-controllers/
}

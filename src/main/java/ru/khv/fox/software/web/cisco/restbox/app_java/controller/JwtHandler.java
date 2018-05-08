/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
class JwtHandler {
	@Nonnull private final AppProperties appProperties;

	@Nonnull
	String createJwt(@Nonnull final UserDetails user, @Nonnull final ServerWebExchange exchange) {
		log.trace("make JWT with username '{}' and authorities {}", user.getUsername(), user.getAuthorities());
		val jwtProperties = appProperties.getJwt();
		val jwtId = UUID.randomUUID().toString();
		val jwtSubject = user.getUsername();
		val jwtIssuedAt = Instant.now();
		val jwtExpiration = jwtIssuedAt.plus(jwtProperties.getTimeToLive());
		// claims that are always added
		val jwtBuilder = Jwts.builder()
		                     .setId(jwtId)
		                     .setSubject(jwtSubject)
		                     .setIssuedAt(Date.from(jwtIssuedAt))
		                     .setNotBefore(Date.from(jwtIssuedAt))
		                     .setExpiration(Date.from(jwtExpiration))
		                     .claim("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());
		// optional claims
		determineIssuer(exchange).ifPresent(jwtBuilder::setIssuer);
		jwtProperties.getAudience().ifPresent(jwtBuilder::setAudience);
		return jwtBuilder.signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
		                 .compact();
	}

	@Nonnull
	private static Optional<String> determineIssuer(@Nonnull final ServerWebExchange exchange) {
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

}

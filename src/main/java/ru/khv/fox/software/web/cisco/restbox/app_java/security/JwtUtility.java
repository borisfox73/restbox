/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtility {
	private static final String AUTHORITIES_CLAIM = "authorities";
	private static final long ALLOWED_CLOCK_SKEW_SECONDS = 30;

	@NonNull private final AppProperties.JwtProperties jwtProperties;


	JwtUtility(@NonNull final AppProperties appProperties) {
		this.jwtProperties = appProperties.getJwt();
	}

	@NonNull
	public String createJwt(@NonNull final UserDetails user, @NonNull final ServerWebExchange exchange) {
		log.trace("make JWT with username '{}' and authorities {}", user.getUsername(), user.getAuthorities());
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
		                     .claim(AUTHORITIES_CLAIM, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());
		// optional claims
		determineIssuer(exchange).ifPresent(jwtBuilder::setIssuer);
		jwtProperties.getAudience().ifPresent(jwtBuilder::setAudience);
		return jwtBuilder.signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
		                 .compact();
	}

	@NonNull
	private static Optional<String> determineIssuer(@NonNull final ServerWebExchange exchange) {
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

	@NonNull
	JwtClaims parseJwt(@NonNull final String jwtToken) {
		log.trace("parse JWT {}", jwtToken);
		// parse the token.
		// can throw ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException
		val jwtParser = Jwts.parser()
		                    .setSigningKey(jwtProperties.getSecret().getBytes())
		                    .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_SECONDS);
		jwtProperties.getAudience().ifPresent(jwtParser::requireAudience);
		val jwtBody = jwtParser.parseClaimsJws(jwtToken).getBody();
		// can throw RequiredTypeException, IllegalArgumentException
		return new JwtClaims(jwtBody.getId(),
		                     jwtBody.getSubject(),
		                     jwtBody.getIssuer(),
		                     jwtBody.get(AUTHORITIES_CLAIM, Collection.class));
	}
}

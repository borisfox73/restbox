/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the JWT Service using JWTK JJWT library.
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
	private static final long ALLOWED_CLOCK_SKEW_SECONDS = 30;

	@NonNull private final AppProperties.JwtProperties jwtProperties;


	JwtServiceImpl(@NonNull final AppProperties appProperties) {
		this.jwtProperties = appProperties.getJwt();
	}

	// There is not so much of reactivity in this service

	@Override
	@NonNull
	public Mono<Authentication> createJwt(@NonNull final UserDetails user/*, @NonNull final ServerWebExchange exchange*/) {
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
		                     .claim(CLAIM_NAME_AUTHORITIES, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());
		// TODO add claim "login" with username for compatibility? check with the frontend whether it interpretes JWT.
		// optional claims
		jwtProperties.getIssuer().ifPresent(jwtBuilder::setIssuer);
		jwtProperties.getAudience().ifPresent(jwtBuilder::setAudience);
		return Mono.just(new JwtAuthenticationToken(jwtBuilder.signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
		                                                      .compact()));
	}

	@Override
	@NonNull
	public Mono<Authentication> parseJwt(@NonNull final String jwtToken) {
		log.trace("parse JWT {}", jwtToken);
		try {
			// I doubt whether the parser instance is thread-safe.
			// parse the token.
			// can throw ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException
			val jwtParser = Jwts.parser()
			                    .setSigningKey(jwtProperties.getSecret().getBytes())
			                    .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_SECONDS);
			jwtProperties.getAudience().ifPresent(jwtParser::requireAudience);
			val jwtBody = jwtParser.parseClaimsJws(jwtToken).getBody();
			// can throw RequiredTypeException, IllegalArgumentException
			val id = Optional.ofNullable(jwtBody.getId()).map(UUID::fromString).orElse(null);
			val username = jwtBody.getSubject();
			val authorities = Optional.ofNullable((Collection<?>) jwtBody.get(CLAIM_NAME_AUTHORITIES, Collection.class)).stream()
			                          .flatMap(Collection::stream)
			                          .map(Object::toString)
			                          .map(SimpleGrantedAuthority::new)
			                          .collect(Collectors.toList());
			log.trace("id {}, username {}, authorities: {}", id, username, authorities);
			// Empty credentials will be handled in authentication manager
			if (id == null || username == null)
				return Mono.empty();
			// create user object to be used as a principal
			val principal = new User(username, "", authorities);
			principal.eraseCredentials();
			// token will be created already in authenticated state
			return Mono.just(new JwtAuthenticationToken(id, principal, principal.getAuthorities()));
			// IllegalArgumentException is not wrapped and cause 500 Internal Server Error response
		} catch (JwtException e) {
			return Mono.error(new AuthenticationServiceException(e.getLocalizedMessage(), e));
		}
	}
}

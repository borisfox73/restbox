/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * A service to process JSON Web Tokens.
 */
public interface JwtService {
	// authorities collection is holded in this claim
	static final String CLAIM_NAME_AUTHORITIES = "authorities";


	/**
	 * Create, encode and sign JWT based on specified user details.
	 *
	 * @param user User details object
	 *
	 * @return Raw JWT Authentication token with JWT string in principal
	 */
	@NonNull
	Mono<Authentication> createJwt(@NonNull UserDetails user);

	/**
	 * Decode, verify and parse specified JWT.
	 *
	 * @param jwtToken Encoded JWT string
	 *
	 * @return JWT Authentication token with principal extracted from the JWT
	 */
	@NonNull
	Mono<Authentication> parseJwt(@NonNull String jwtToken);
}

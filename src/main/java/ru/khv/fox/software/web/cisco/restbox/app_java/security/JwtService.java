/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * A service to process JSON Web Tokens.
 */
public interface JwtService {

	/**
	 * Create, encode and sign JWT based on specified user details.
	 *
	 * @param user User details object
	 *
	 * @return Raw JWT Authentication token with JWT string in principal
	 */
	Mono<Authentication> createJwt(UserDetails user);

	/**
	 * Decode, verify and parse specified JWT.
	 *
	 * @param jwtToken Encoded JWT string
	 *
	 * @return JWT Authentication token with principal extracted from the JWT
	 */
	Mono<Authentication> parseJwt(String jwtToken);
}

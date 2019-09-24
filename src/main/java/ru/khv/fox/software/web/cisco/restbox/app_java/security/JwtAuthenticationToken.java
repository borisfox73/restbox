/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * JSON Web Token authentication container.
 * Used both with raw and parsed tokens by authentication converter and manager.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	@Nullable
	Object id;
	Object principal;


	/**
	 * Constructor used by request header converter.
	 * Extracts token from web request to be parsed and verified by authentication manager.
	 *
	 * @param jwtEncoded String representation of the encoded JWT
	 */
	JwtAuthenticationToken(final String jwtEncoded) {
		super(null);

		Assert.notNull(jwtEncoded, "Cannot pass null JWT to constructor");

		this.id = null;
		this.principal = jwtEncoded;
	}

	/**
	 * Constructor used by JWT authentication manager to create authenticated token.
	 *
	 * @param id          Token ID (based on jti claim)
	 * @param principal   Principal (usually a {@link org.springframework.security.core.userdetails.UserDetails} instance based on subject claim)
	 * @param authorities Granted authorities extracted from the JWT authorities claim
	 */
	JwtAuthenticationToken(@Nullable final Object id, final Object principal,
	                       @Nullable final Collection<? extends GrantedAuthority> authorities) {
		super(authorities);

		Assert.notNull(id, "Cannot pass null id to constructor");
		Assert.notNull(principal, "Cannot pass null principal to constructor");
		Assert.isTrue(!("".equals(id) || "".equals(principal)), "Cannot pass empty id or subject to constructor");

		this.id = id;
		this.principal = principal;
		super.setAuthenticated(true); // must use super, as we override
	}

	/**
	 * Override the default implementation to prevent seting of authenticated state externally.
	 * Authenticated tokens are created only by constructor above.
	 *
	 * @param isAuthenticated Authenticated state
	 *
	 * @throws IllegalArgumentException On attempts to set authenticated state to true
	 */
	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated)
			throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		super.setAuthenticated(false);
	}

	/**
	 * JWT does not carry user credentials.
	 * Always returns an empty <code>String</code>.
	 *
	 * @return an empty String
	 */
	@Override
	public Object getCredentials() {
		return "";
	}
}

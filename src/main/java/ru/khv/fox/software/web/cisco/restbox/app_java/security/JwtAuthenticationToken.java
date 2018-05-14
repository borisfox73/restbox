/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(callSuper = false)
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	@Nullable private final Object id;
	@Nullable private final Object principal;


	/**
	 * Constructor used by request header converter.
	 * Creates object to be verified by authentication manager.
	 *
	 * @param claims Object holding values of parsed JWT claims
	 */
	JwtAuthenticationToken(@Nullable final JwtClaims claims) {
		super(extractAuthorities(claims));

		if (claims != null) {
			this.id = claims.getId();
			this.principal = claims.getSubject();
			setDetails(claims);
		} else
			this.id = this.principal = null;
	}

	/**
	 * Constructor used by JWT authentication manager to create authenticated token.
	 *
	 * @param id          Token ID (based on jti claim)
	 * @param principal   Principal (usually a {@link org.springframework.security.core.userdetails.UserDetails} instance based on subject claim)
	 * @param authorities Granted authorities extracted from the JWT authorities claim
	 */
	JwtAuthenticationToken(@Nullable final Object id, @NonNull final Object principal,
	                       @Nullable final Collection<? extends GrantedAuthority> authorities) {
		super(authorities);

		Assert.notNull(id, "Cannot pass null id to constructor");
		Assert.notNull(principal, "Cannot pass null principal to constructor");
		Assert.isTrue(!("".equals(id) || "".equals(principal)), "Cannot pass empty id or subject to constructor");

		this.id = id;
		this.principal = principal;
		super.setAuthenticated(true); // must use super, as we override
	}

	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated)
			throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		super.setAuthenticated(false);
	}

	/**
	 * Always returns an empty <code>String</code>
	 *
	 * @return an empty String
	 */
	@Override
	public Object getCredentials() {
		return "";
	}

	boolean hasIdentity() {
		return getDetails() != null && ((JwtClaims) getDetails()).hasIdentity();
	}

	private static Collection<SimpleGrantedAuthority> extractAuthorities(@Nullable final JwtClaims jwtClaims) {
		if (jwtClaims == null || jwtClaims.getAuthorities() == null || jwtClaims.getAuthorities().length == 0)
			return null;
		return Arrays.stream(jwtClaims.getAuthorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}
}

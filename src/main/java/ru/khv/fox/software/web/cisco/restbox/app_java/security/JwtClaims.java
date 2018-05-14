/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.Value;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.UUID;

// Container for persed JWT claims.
// Attached to unauthenticated JwtAuthenticationToken as details
@Value
class JwtClaims {
	@Nullable private final UUID id;
	@Nullable private final String subject;
	@Nullable private final String issuer;
	@Nullable private final String[] authorities;

	JwtClaims(@Nullable final String id, @Nullable final String subject,
	          @Nullable final String issuer, @Nullable final Collection<?> authorities) {
		this.id = id != null ? UUID.fromString(id) : null;
		this.subject = subject;
		this.issuer = issuer;
		this.authorities = authorities != null ? authorities.stream().map(Object::toString).toArray(String[]::new) : null;
	}

	boolean hasIdentity() {
		return id != null && StringUtils.hasText(subject);
	}
}

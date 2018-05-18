/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

// Container for persed JWT claims.
// Attached to unauthenticated JwtAuthenticationToken as details
@Value
class JwtClaims {
	@Nullable private final UUID id;
	@Nullable private final String subject;
	@Nullable private final String issuer;
	@NonNull private final Collection<String> authorityNames;

	JwtClaims(@Nullable final String id, @Nullable final String subject,
	          @Nullable final String issuer, @Nullable final Collection<?> authorityNames) {
		this.id = id != null ? UUID.fromString(id) : null;
		this.subject = subject;
		this.issuer = issuer;
		this.authorityNames = authorityNames != null ? authorityNames.stream().map(Object::toString).collect(Collectors.toList()) : Collections.emptyList();
	}

	boolean hasIdentity() {
		return id != null && StringUtils.hasText(subject);
	}
}

/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {
	@JsonProperty
	@JsonPropertyDescription("JWT authorization token")
	@NonNull
	private final String token;


	// Token string is in the principal field
	public static LoginResponse from(@NonNull final Authentication authentication) {
		return new LoginResponse(authentication.getName());
	}
}

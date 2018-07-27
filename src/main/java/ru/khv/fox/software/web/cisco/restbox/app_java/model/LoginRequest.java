/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Value
public class LoginRequest {
	@JsonProperty
	@JsonPropertyDescription("User login name")
	@NonNull
	private final String username;
	@JsonProperty
	@JsonPropertyDescription("User password")
	@NonNull
	private final String password;


	// TODO can't find a way to tell Jackson to fail on null properties with creator method
	// see https://github.com/FasterXML/jackson-databind/issues/2024
	@JsonCreator
	private LoginRequest(@JsonProperty(value = "username", required = true) @NonNull final String username,
	                     @JsonProperty(value = "password", required = true) @NonNull final String password) {
		Assert.notNull(username, "Username cannot be null");
		Assert.notNull(password, "Password cannot be null");
		this.username = username;
		this.password = password;
	}
}

/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
public class LoginRequest {
	@NotEmpty
	@JsonProperty
	@JsonPropertyDescription("User login name")
	String username;
	@NotEmpty
	@JsonProperty
	@JsonPropertyDescription("User password")
	String password;


	@JsonCreator
	private static LoginRequest creator(@JsonProperty(value = "username", required = true) final String username,
	                                    @JsonProperty(value = "password", required = true) final String password) {
		return new LoginRequest(username, password);
	}
}

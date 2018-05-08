/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
	@JsonProperty
	@JsonPropertyDescription("User name")
	@NotBlank
	private String username;
	@JsonProperty
	@JsonPropertyDescription("Password")
	@NotBlank
	private String password;
}

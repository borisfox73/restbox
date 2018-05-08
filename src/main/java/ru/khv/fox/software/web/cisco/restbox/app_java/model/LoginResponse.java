/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.lang.NonNull;

// TODO сделать универсальный ответ типа Map со строковым ключом и значением типа Object
@Value
@AllArgsConstructor(staticName = "of")
public class LoginResponse {
	//@JsonProperty("token")
	@JsonProperty
	@JsonPropertyDescription("JWT authorization token")
	@NonNull
	private final String token;
	// TODO сменить тип со String на какой-нибудь JWT ?
}

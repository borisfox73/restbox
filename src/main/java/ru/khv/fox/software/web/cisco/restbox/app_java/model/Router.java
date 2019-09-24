/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

/**
 * Router definition and runtime state.
 * Keyed by id.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Router {
	// Mostly resembles router definition in application properties
	@JsonIgnore
	String id;
	@JsonProperty
	@JsonPropertyDescription("Router name")
	String name;
	@JsonProperty
	@JsonPropertyDescription("Router host")
	String host;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user name")
	String username;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user password")
	String password;
	@JsonProperty
	@JsonPropertyDescription("Router type")
	RouterType type;
	// runtime state
	@JsonProperty
	@JsonPropertyDescription("Authentication token")
	@Nullable
	@NonFinal
	String token;


	public static Router getInstance(final String id, final AppProperties.RouterProperties routerProperties) {
		return new Router(id,
		                  routerProperties.getName(), routerProperties.getHost(),
		                  routerProperties.getUsername(), routerProperties.getPassword(),
		                  routerProperties.getType());
	}
}

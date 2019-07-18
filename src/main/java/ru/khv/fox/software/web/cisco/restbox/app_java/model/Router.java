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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

/**
 * Router definition and runtime state.
 * Keyed by id.
 * TODO verify json representation
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Router {
	// parameters mostly resembles properties
	// TODO should wrap other properties with value of this one
	//@JsonProperty
	//@JsonPropertyDescription("Router id")
	@JsonIgnore
	@NonNull
	final String id;
	@JsonProperty
	@JsonPropertyDescription("Router name")
	@NonNull
	final String name;
	@JsonProperty
	@JsonPropertyDescription("Router host")
	@NonNull
	final String host;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user name")
	@NonNull
	final String username;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user password")
	@NonNull
	final String password;
	@JsonProperty
	@JsonPropertyDescription("Router type")
	@NonNull
	final RouterType type;
	// runtime state
	@JsonProperty
	@JsonPropertyDescription("Authentication token")
	@Nullable
	String token;   // TODO may be initialize token to empty string. Check with front-end.


	public static Router getInstance(@NonNull final String id, @NonNull final AppProperties.RouterProperties routerProperties) {
		return new Router(id,
		                  routerProperties.getName(), routerProperties.getHost(),
		                  routerProperties.getUsername(), routerProperties.getPassword(),
		                  routerProperties.getType());
	}
}

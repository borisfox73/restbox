/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import org.springframework.lang.NonNull;
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
public class Router {
	// parameters mostly resembles properties
	// TODO should wrap other properties with value of this one
	//@JsonProperty
	//@JsonPropertyDescription("Router id")
	@NonNull
	private final String id;
	@JsonProperty
	@JsonPropertyDescription("Router name")
	@NonNull
	private final String name;
	@JsonProperty
	@JsonPropertyDescription("Router host")
	@NonNull
	private final String host;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user name")
	@NonNull
	private final String username;
	@JsonProperty
	@JsonPropertyDescription("Router authentication user password")
	@NonNull
	private final String password;
	@JsonProperty
	@JsonPropertyDescription("Router type")
	@NonNull
	private final RouterType type;
	// runtime state


	public static Router getInstance(@NonNull final String id, @NonNull final AppProperties.RouterProperties routerProperties) {
		return new Router(id,
		                  routerProperties.getName(), routerProperties.getHost(),
		                  routerProperties.getUsername(), routerProperties.getPassword(),
		                  routerProperties.getType());
	}
}

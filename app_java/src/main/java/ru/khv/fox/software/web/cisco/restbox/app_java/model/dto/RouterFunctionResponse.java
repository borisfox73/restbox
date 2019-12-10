/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouterFunctionResponse {
	@JsonProperty
	@JsonPropertyDescription("Router function name")
	String name;
	@JsonProperty
	@JsonPropertyDescription("Router function description")
	@Nullable
	String descr;


	public static RouterFunctionResponse from(final RouterFunction<?, ?, ?> routerFunction) {
		return new RouterFunctionResponse(routerFunction.getName(), routerFunction.getDescr());
	}
}

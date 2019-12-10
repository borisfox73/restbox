/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.ApiResponseSerializer;

/**
 * Common API response with single message field.
 */
@Value
public class ApiResponse {
	@JsonProperty
	@JsonPropertyDescription("Response message")
	@JsonSerialize(using = ApiResponseSerializer.class)
	Object message;
}

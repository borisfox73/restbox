/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;

// Common API response with single message field.
@Getter
@ToString
public class CommonResponse {
	@JsonProperty
	@JsonPropertyDescription("Response message")
	@NonNull
//	private final String message;
	private final Object message;

	public CommonResponse(@NonNull final Object payload) {
		this.message = payload;
	}

/*
	public static CommonResponse with(@NonNull final String message) {
		return new CommonResponse(message);
	}
*/
}

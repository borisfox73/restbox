/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic request of key-value type.
 */
@Getter
@ToString
public class RestApiUniversalDTO implements RestApiDTO {
	@JsonAnySetter
//	@JsonValue
	@NonNull
	private final Map<String, Object> body = new HashMap<>();


/*
	private RestApiUniversalDTO(@NonNull final Map<String,Object> body) {
		this.body.putAll(body);
	}
*/

	// TODO needed?
/*
	public static RestApiUniversalDTO of(@NonNull final Map<String,Object> body) {
		return new RestApiUniversalDTO(body);
	}
*/
}

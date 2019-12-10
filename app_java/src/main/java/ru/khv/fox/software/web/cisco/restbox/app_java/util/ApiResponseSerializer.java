/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.val;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serializer for API response to encode JSON object value as a string, to meet frontend requirements.
 */
public class ApiResponseSerializer extends JsonSerializer<Object> {

	@Override
	public void serialize(final Object value,
	                      final JsonGenerator jsonGenerator,
	                      final SerializerProvider serializerProvider) throws IOException {
		// Do not stringify primitive types
		if (value == null ||
		    value instanceof String ||
		    value instanceof Number ||
		    value instanceof byte[] ||
		    value instanceof Boolean ||
		    value instanceof AtomicBoolean ||
		    value instanceof Enum)
			jsonGenerator.writeObject(value);
		else {
			val codec = jsonGenerator.getCodec();
			if (!(codec instanceof ObjectMapper))
				throw new IllegalStateException("No ObjectMapper codec defined for the generator");
			jsonGenerator.writeString(((ObjectMapper) codec).writeValueAsString(value));
		}
	}
}

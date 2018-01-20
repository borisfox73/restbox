/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Configurable applicatiob properties.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.config")
public class AppProperties {
	// Fields corresponds to properties file keys
	// Test scalar value
	@NotNull
	private String teststr;
	@NotNull
	private Integer testint;
	// Routers map
	private final Map<String, RouterProperties> routers = new HashMap<>();


	/**
	 * Router properties.
	 */
	@Data
	public static class RouterProperties {
		public enum RouterTypes {CSRV, ASR}

		@NotEmpty
		private String name;
		@NotEmpty
		private String host;
		@NotEmpty
		private String username;
		@NotEmpty
		private String password;
		@NotNull
		private RouterTypes type;

/* not needed - default StringToEnum converter is capable to perform case-insensitive matching.
		@Component
		@ConfigurationPropertiesBinding
		public class RouterTypesConverter implements Converter<String, RouterTypes> {
			@Nullable
			@Override
			public RouterTypes convert(@Nullable final String source) {
				return source != null ? RouterTypes.valueOf(source.toUpperCase()) : null;
			}
		}
*/
	}
}

/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Configurable application properties.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.config")
public class AppProperties {
	/**
	 * JSON Web Ticket parameters
	 */
	private JwtProperties jwt;
	/**
	 * Routers
	 */
	private Map<String, RouterProperties> routers = new HashMap<>();
	/**
	 * Users
	 */
	private List<UserProperties> users = new ArrayList<>();


	/*
	 * Router properties.
	 */
	@Data
	public static class RouterProperties {
		public enum RouterTypes {CSRV, @SuppressWarnings("unused") ASR}

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

	/*
	 * User properties.
	 */
	@Data
	public static class UserProperties {
		@NotBlank
		private String username;
		@NotBlank
		private String password;
		@NotBlank
		private String roles;   // comma-delimited list

		/**
		 * Convert comma-delimited roles string to array.
		 *
		 * @return Roles array
		 */
		public String[] getRoles() {
			return Arrays.stream(roles.split(","))
			             .map(String::trim)
			             .filter(Strings::isNotEmpty)
			             .toArray(String[]::new);
		}
	}

	/*
	 * JSON Web Token parameters.
	 */
	@Data
	public static class JwtProperties {
		@Nullable
		private String audience;
		@NotBlank
		private String secret;
		@NotNull
		@DurationMin(minutes = 1)
		private Duration timeToLive = Duration.of(1L, ChronoUnit.HOURS);

		@NonNull
		public Optional<String> getAudience() {
			return Optional.ofNullable(audience).filter(StringUtils::hasText);
		}
	}
}

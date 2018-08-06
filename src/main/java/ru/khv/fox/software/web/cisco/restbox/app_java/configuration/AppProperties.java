/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.validation.ValidBoxControl;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
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
	@SuppressWarnings("NullableProblems")
	@NotNull
	private JwtProperties jwt;
	/**
	 * Routers
	 */
	@NotEmpty
	private Map<String, RouterProperties> routers = new HashMap<>();
	/**
	 * Users
	 */
	@NotEmpty
	private List<UserProperties> users = new ArrayList<>();

	/**
	 * Boxes
	 */
	@NotEmpty
	private List<BoxProperties> boxcontrol = new ArrayList<>();


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
		@SuppressWarnings("NullableProblems")
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
		private String issuer;
		@Nullable
		private String audience;
		@NotBlank
		private String secret;
		@NotNull
		@DurationMin(minutes = 1)
		private Duration timeToLive = Duration.of(1L, ChronoUnit.HOURS);

		@NonNull
		public Optional<String> getIssuer() {
			return Optional.ofNullable(issuer).filter(StringUtils::hasText);
		}

		@NonNull
		public Optional<String> getAudience() {
			return Optional.ofNullable(audience).filter(StringUtils::hasText);
		}
	}

	// TODO refactor into properties and model classes
	/*
	 * Box controls, sensors and indicators.
	 */
	@Data
	@ValidBoxControl
	public static class BoxControlProperties {
		@RequiredArgsConstructor
		public enum ControlTypes {
			SWITCH(ControlKinds.SENSOR), BUTTON(ControlKinds.SENSOR), USONIC(ControlKinds.SENSOR), LED(ControlKinds.INDICATOR);

			private enum ControlKinds {SENSOR, INDICATOR}

			private final ControlKinds kind;

			public boolean isSensor() {
				return kind == ControlKinds.SENSOR;
			}

			public boolean isIndicator() {
				return kind == ControlKinds.INDICATOR;
			}
		}

		// type should be SWITCH, BUTTON or USONIC
		public enum OnOffFunctions {
			ANONE
		}

		// type should be LED
		public enum RFunctions {
			RNONE
		}

		@SuppressWarnings("NullableProblems")
		@NotNull
		private ControlTypes type;
		@PositiveOrZero
		private int id;
		@PositiveOrZero
		private int status;
		@Nullable
		private String descr;
		@Nullable
		private OnOffFunctions onFunc;
		@Nullable
		private OnOffFunctions offFunc;
		@Nullable
		private RFunctions rFunc;
	}

	/*
	 * Box descriptor.
	 */
	@Data
	private static class BoxProperties {
		@NotBlank
		private String name;
		@NotBlank
		private String secret;
		@PositiveOrZero
		private int ready;
		@NotEmpty
		private List<BoxControlProperties> boxes = new ArrayList<>();
	}
}

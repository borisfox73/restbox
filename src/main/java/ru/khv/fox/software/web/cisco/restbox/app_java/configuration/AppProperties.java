/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.validation.ValidBoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlOnOffFunctions;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlRFunctions;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;

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
	private Set<UserProperties> users = new HashSet<>();

	/**
	 * Boxes (intermediate configuration objects)
	 */
	@Getter(AccessLevel.PACKAGE)
//	@Getter(AccessLevel.NONE)
	@NotEmpty
	private Set<BoxProperties> boxcontrol = new HashSet<>();

//	/**
//	 * Boxes dynamic objects
//	 */
//	@Getter(AccessLevel.PACKAGE)
//	@Setter(AccessLevel.NONE)
//	private Set<Box> boxes;
//
//
//	@PostConstruct
//	void postConstruct() {
//		// Instantiate dynamic objects with state from configuration properties
//		boxes = boxcontrol.stream().map(Box::getInstance).collect(Collectors.toSet());
//	}


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

		// Uniqueness on username
		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final UserProperties that = (UserProperties) o;
			return Objects.equals(username, that.username);
		}

		@Override
		public int hashCode() {
			return Objects.hash(username);
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
		@SuppressWarnings("NullableProblems")
		@NotNull
		private BoxControlType type;
		@PositiveOrZero
		private int id;
		@Nullable
		private String descr;
		// runtime state fields are allowed in configuration but ignored
		//@PositiveOrZero
		private int status;
		//@Nullable
		private BoxControlOnOffFunctions onFunc;
		//@Nullable
		private BoxControlOnOffFunctions offFunc;
		//@Nullable
		private BoxControlRFunctions rFunc;
	}

	/*
	 * Box descriptor.
	 */
	@Data
	public static class BoxProperties {
		@NotBlank
		private String name;
		@NotBlank
		private String secret;
		// runtime state fields are allowed in configuration but ignored
		//@PositiveOrZero
		private int ready;
		@NotEmpty
		private List<BoxControlProperties> boxes = new ArrayList<>();

		// Uniqueness on box name
		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final BoxProperties that = (BoxProperties) o;
			return Objects.equals(name, that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}
}

/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.validation.ValidBoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterType;
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
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppProperties {
	/**
	 * WebClient customization properties.
	 */
	@NotNull
	@NestedConfigurationProperty
	private WebClientProperties webClient = new WebClientProperties();
	/**
	 * JSON Web Ticket parameters
	 */
	@NotNull
	@NestedConfigurationProperty
	private JwtProperties jwt;
	/**
	 * Routers
	 */
	// @Getter(AccessLevel.PACKAGE) // TODO to be accessible in tests
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
//	@Getter(AccessLevel.PACKAGE)    // access level restriction causes nested properties type missing from configuration metadata
	@NotEmpty
	private Set<BoxProperties> boxcontrol = new HashSet<>();

	/**
	 * Period of polling resources tied to box indicators.
	 */
	@NotNull
	@DurationMin(seconds = 5)
	private Duration indicatorPollInterval = Duration.ofSeconds(30);


	// Poller scheduling delay in milliseconds
	public long getIndicatorPollDelay() {
		return indicatorPollInterval.toMillis();
	}


	/*
	 * WebClient properties.
	 */
	@SuppressWarnings("WeakerAccess")
	@Data
	public static class WebClientProperties {
		/**
		 * Trace WebClient requests.
		 */
		private boolean traceWebClientRequests;
		/**
		 * Ignore SSL certificate issuer and host name validation for REST API client.
		 */
		private boolean sslIgnoreValidation;
	}

	/*
	 * Router properties.
	 */
	@Data
	public static class RouterProperties {
		@NotEmpty
		private String name;
		@NotEmpty
		private String host;
		@NotEmpty
		private String username;
		@NotEmpty
		private String password;
		@NotNull
		private RouterType type;
	}

	/*
	 * User properties.
	 */
	@Data
	@JsonAutoDetect(
			fieldVisibility = JsonAutoDetect.Visibility.NONE,
			setterVisibility = JsonAutoDetect.Visibility.NONE,
			getterVisibility = JsonAutoDetect.Visibility.NONE,
			isGetterVisibility = JsonAutoDetect.Visibility.NONE,
			creatorVisibility = JsonAutoDetect.Visibility.NONE
	)
	public static class UserProperties {
		@JsonProperty("login")
		@JsonPropertyDescription("Login")
		@NotBlank
		private String username;
		@JsonProperty
		@JsonPropertyDescription("Password")
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

	/*
	 * Box controls, sensors and indicators.
	 */
	@Data
	@ValidBoxControl
	public static class BoxControlProperties {
		@NotNull
		private BoxControlType type;
		@PositiveOrZero
		private int id;
		@Nullable
		private String descr;
		// runtime state fields are allowed in configuration but ignored
		//@PositiveOrZero
		private int status;
		// TODO validate by router functions set
		//@Nullable
		private String onFunc;
		//@Nullable
		private String offFunc;
		//@Nullable
		private String rFunc;
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

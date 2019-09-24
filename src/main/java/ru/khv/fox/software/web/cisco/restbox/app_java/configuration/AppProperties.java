/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated
@Component
@ConfigurationProperties(prefix = "app.config")
public class AppProperties {
	/**
	 * WebClient customization properties.
	 */
	@NotNull
	@NestedConfigurationProperty
	WebClientProperties webClient = new WebClientProperties();
	/**
	 * JSON Web Ticket parameters
	 */
	@NotNull
	@NestedConfigurationProperty
	JwtProperties jwt;
	/**
	 * Routers
	 */
	// @Getter(AccessLevel.PACKAGE) // getter is need to be accessible in tests
	@NotEmpty
	Map<String, RouterProperties> routers = new HashMap<>();
	/**
	 * Users
	 */
	@NotEmpty
	Set<UserProperties> users = new HashSet<>();

	/**
	 * Boxes (intermediate configuration objects)
	 */
//	@Getter(AccessLevel.PACKAGE)    // access level restriction causes nested properties type missing from configuration metadata
	@NotEmpty
	Set<BoxProperties> boxcontrol = new HashSet<>();

	/**
	 * Period of polling resources tied to box indicators.
	 */
	@NotNull
	@DurationMin(seconds = 5)
	Duration indicatorPollInterval = Duration.ofSeconds(30);

	@NotEmpty
	String ciscoRestApiUriTemplate = "https://{hostname}:55443/api/v1/";


	// Poller scheduling delay in milliseconds
	public long getIndicatorPollDelay() {
		return indicatorPollInterval.toMillis();
	}


	/*
	 * WebClient properties.
	 */
	@SuppressWarnings("WeakerAccess")
	@Data
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class WebClientProperties {
		/**
		 * Trace WebClient requests.
		 */
		boolean traceWebClientRequests;
		/**
		 * Ignore SSL certificate issuer and host name validation for REST API client.
		 */
		boolean sslIgnoreValidation;
	}

	/*
	 * Router properties.
	 */
	@Data
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class RouterProperties {
		@NotEmpty
		String name;
		@NotEmpty
		String host;
		@NotEmpty
		String username;
		@NotEmpty
		String password;
		@NotNull
		RouterType type;
	}

	/*
	 * User properties.
	 */
	@Data
	@EqualsAndHashCode(of = "username")
	@FieldDefaults(level = AccessLevel.PRIVATE)
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
		String username;
		@JsonProperty
		@JsonPropertyDescription("Password")
		@NotBlank
		String password;
		@NotBlank
		String roles;   // comma-delimited list

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
	@ToString(doNotUseGetters = true)
	@EqualsAndHashCode(doNotUseGetters = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class JwtProperties {
		@Nullable
		String issuer;
		@Nullable
		String audience;
		@NotBlank
		String secret;
		@NotNull
		@DurationMin(minutes = 1)
		Duration timeToLive = Duration.of(1L, ChronoUnit.HOURS);

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
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@ValidBoxControl
	public static class BoxControlProperties {
		@NotNull
		BoxControlType type;
		@PositiveOrZero
		int id;
		@Nullable
		String descr;
		// runtime state fields are allowed in configuration but ignored
		int status;
		// TODO validate by router functions set
		//@Nullable
		String onFunc;
		//@Nullable
		String offFunc;
		//@Nullable
		String rFunc;
	}

	/*
	 * Box descriptor.
	 */
	@Data
	@EqualsAndHashCode(of = "name")
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class BoxProperties {
		@NotBlank
		String name;
		@NotBlank
		String secret;
		// runtime state fields are allowed in configuration but ignored
		int ready;
		@NotEmpty
		List<BoxControlProperties> boxes = new ArrayList<>();
	}
}

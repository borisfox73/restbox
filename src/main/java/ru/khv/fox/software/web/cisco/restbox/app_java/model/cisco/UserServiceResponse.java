/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Cisco RESTful API Hostname service response.
 * <pre>
 * {
 *     "kind" : "object#host-name",
 *     "host-name": "{string}"
 * }
 * </pre>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserServiceResponse extends DTOBase {
	@NonNull
	private String username;
	@Nullable
	private String password;
	@Nullable
	private Integer privilegeLevel;
	@Nullable
	private Integer passwordType;


	@JsonCreator
	UserServiceResponse(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                    @JsonProperty(value = "username", required = true) @NonNull final String username,
	                    @JsonProperty(value = "password") @Nullable final String password,
	                    @JsonProperty(value = "privilege") @Nullable final Integer privilegeLevel,
	                    @JsonProperty(value = "pw-type") @Nullable final Integer passwordType) {
		super(kind);
		this.username = username;
		this.password = password;
		this.privilegeLevel = privilegeLevel;
		this.passwordType = passwordType;
	}
}

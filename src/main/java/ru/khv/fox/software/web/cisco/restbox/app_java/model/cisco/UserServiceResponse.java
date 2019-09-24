/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.Nullable;

/**
 * Cisco RESTful API user service response.
 * <pre>
 * {
 *     "kind" : "object#local-user",
 *     "username": "{string}"
 *     "password": "{string}"
 *     "privilege": {number}
 *     "pw-type": {number}
 * }
 * </pre>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserServiceResponse extends DTOBase {
	@JsonPropertyDescription("Name of the user")
	String username;
	@Nullable
	@JsonPropertyDescription("Password")
	String password;
	@Nullable
	@JsonPropertyDescription("Privilege level 0-15")
	Integer privilegeLevel;
	@Nullable
	@JsonPropertyDescription("IOS password type (0 or 7)")
	Integer passwordType;


	@JsonCreator
	UserServiceResponse(@JsonProperty(value = "kind", required = true) final String kind,
	                    @JsonProperty(value = "username", required = true) final String username,
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

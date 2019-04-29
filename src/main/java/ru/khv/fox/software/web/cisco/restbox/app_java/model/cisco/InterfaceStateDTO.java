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
import org.springframework.lang.NonNull;

/**
 * Cisco RESTful API Interface State Resource Data Transfer Object for request and response.
 * <pre>
 * {
 *  "kind" : "object#interface-state",
 *  "if-name" : "gigabitEthernet1",
 *  "enabled" : true
 * }
 * </pre>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfaceStateDTO extends DTOBase {
	@JsonPropertyDescription("Interface Name")
	@NonNull
	private String ifName;
	@JsonPropertyDescription("Interface State (true = up, false = down")
	private boolean enabled;


	@JsonCreator
	InterfaceStateDTO(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                  @JsonProperty(value = "if-name", required = true) @NonNull final String ifName,
	                  @JsonProperty(value = "enabled", required = true) final boolean enabled) {
		super(kind);
		this.ifName = ifName;
		this.enabled = enabled;
	}

	/**
	 * Create DTO instance for request.
	 *
	 * @param ifName  Interface name
	 * @param enabled Desired interface state
	 *
	 * @return DTO instance
	 */
	public static InterfaceStateDTO create(@NonNull final String ifName, final boolean enabled) {
		// kind field value is of no matter because it's not serialized
		return new InterfaceStateDTO("object#interface-state", ifName, enabled);
	}
}

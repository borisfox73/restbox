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
public class HostnameServiceResponse extends DTOBase {
	@JsonPropertyDescription("Host name")
	String hostName;


	@JsonCreator
	HostnameServiceResponse(@JsonProperty(value = "kind", required = true) final String kind,
	                        @JsonProperty(value = "host-name", required = true) final String hostName) {
		super(kind);
		this.hostName = hostName;
	}
}

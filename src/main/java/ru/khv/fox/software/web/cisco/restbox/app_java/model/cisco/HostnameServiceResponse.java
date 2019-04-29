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
	@NonNull
	private String hostName;


	@JsonCreator
	HostnameServiceResponse(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                        @JsonProperty(value = "host-name", required = true) @NonNull final String hostName) {
		super(kind);
		this.hostName = hostName;
	}
}

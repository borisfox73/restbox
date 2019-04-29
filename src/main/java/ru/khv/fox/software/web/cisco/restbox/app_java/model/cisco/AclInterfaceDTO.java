/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.jsonwebtoken.lang.Assert;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * Cisco RESTful API ACL Associated with an Interface Resource Data Transfer Object for request and response.
 * <pre>
 * {
 *  "kind" : "object#acl-interface"
 *  "if-id" : "{string}",
 *  "direction" : "{string}"
 * }
 * </pre>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclInterfaceDTO extends DTOBase {
	@JsonPropertyDescription("Interface name")
	@NonNull
	private String ifId;
	@JsonPropertyDescription("ACL Traffic direction (inside or outside")
	@NonNull
	private String direction;


	@JsonCreator
	AclInterfaceDTO(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                @JsonProperty(value = "if-id", required = true) @NonNull final String ifId,
	                @JsonProperty(value = "direction", required = true) @NonNull final String direction) {
		super(kind);
		this.ifId = ifId;
		this.direction = direction;
	}

	/**
	 * Create DTO instance for request.
	 *
	 * @param ifId      Interface to which the ACL is applied
	 * @param direction Direction of traffic to which the ACL is applied. Valid
	 *                  values are "inside" and "outside".
	 *
	 * @return DTO instance
	 */
	public static AclInterfaceDTO create(@NonNull final String ifId, @NonNull final String direction) {
		Assert.isTrue("inside".equals(direction) || "outside".equals(direction), "Unsupported 'direction' value \"" + direction + "\"");
		// kind field value is of no matter because it's not serialized
		return new AclInterfaceDTO("object#acl-interface", ifId, direction);
	}
}

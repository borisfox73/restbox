/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.jsonwebtoken.lang.Assert;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
@JsonInclude(Include.NON_NULL)
public class AclInterfaceDTO extends DTOBase {
	@JsonPropertyDescription("Access List name")
	@Nullable
	private String aclId;
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
		this.aclId = null;
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

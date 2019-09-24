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

import java.util.Collection;

/**
 * Cisco RESTful API users service response.
 * <pre>
 *  {
 *      "kind": "collection#local-user"
 *      "users": [
 *          {
 *              "kind" : "object#local-user",
 *              "username" : "jtod",
 *              "pw-type" : 7,
 *              "privilege": 15
 *          },
 *          {
 *              "kind": "object#local-user",
 *              "username" : "marym",
 *              "pw-type" : 7,
 *              "privilege": 7
 *          }
 *      ]
 * }
 * </pre>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersServiceResponse extends DTOBase {
	@JsonPropertyDescription("Array of users")
	Collection<UserServiceResponse> users;


	@JsonCreator
	UsersServiceResponse(@JsonProperty(value = "kind", required = true) final String kind,
	                     @JsonProperty(value = "users", required = true) final Collection<UserServiceResponse> users) {
		super(kind);
		this.users = users;
	}
}

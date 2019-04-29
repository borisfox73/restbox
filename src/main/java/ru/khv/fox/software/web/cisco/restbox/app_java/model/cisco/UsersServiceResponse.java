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

import java.util.Collection;

/**
 * Cisco RESTful API Hostname service response.
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
	@NonNull
	private Collection<UserServiceResponse> users;


	@JsonCreator
	UsersServiceResponse(@JsonProperty(value = "kind", required = true) @NonNull final String kind,
	                     @JsonProperty(value = "users", required = true) @NonNull final Collection<UserServiceResponse> users) {
		super(kind);
		this.users = users;
	}
}

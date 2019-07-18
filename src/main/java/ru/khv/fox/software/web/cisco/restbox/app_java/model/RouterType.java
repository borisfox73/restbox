/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported Cisco router types
 */
public enum RouterType {
	CSRV, ASR;

	@JsonValue
	String toJson() {
		return this.name().toLowerCase();
	}
}

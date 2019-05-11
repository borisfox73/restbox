/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utilities {

	// Check if String is not null and not empty
	public boolean nonEmpty(final String s) {
		return s != null && !s.isEmpty();
	}
}

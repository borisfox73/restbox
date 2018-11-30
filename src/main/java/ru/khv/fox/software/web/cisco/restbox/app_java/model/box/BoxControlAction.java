/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

/**
 * Actions on box sensor control state change.
 * Depends on sensor type, reported state, and thresholds.
 */
public enum BoxControlAction {
	NOOP, ON, OFF;
}

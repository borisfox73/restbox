/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;

@SuppressWarnings("WeakerAccess")
@Value
@AllArgsConstructor(staticName = "of", access = AccessLevel.PACKAGE)
public class ExecFunctionResultPair<T extends RestApiDTO, V> {
	private T dto;
	private V boxValue;
}

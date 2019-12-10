/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorDTO;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(doNotUseGetters = true)
public class ExecFunctionResultPair<T extends RestApiDTO, E extends RestApiErrorDTO, V> {
	T successDto;
	E errorDto;
	@Nullable
	V boxValue;


	// Wrap in Optional to handle possible null value in reactive streams
	public Optional<V> getBoxValue() {
		return Optional.ofNullable(boxValue);
	}

	static <T extends RestApiDTO, E extends RestApiErrorDTO, V> ExecFunctionResultPair<T, E, V> of(final T successDto, @Nullable final V boxValue) {
		return new ExecFunctionResultPair<>(successDto, null, boxValue);
	}

	static <T extends RestApiDTO, E extends RestApiErrorDTO, V> ExecFunctionResultPair<T, E, V> of(final E errorDto, @Nullable final V boxValue) {
		return new ExecFunctionResultPair<>(null, errorDto, boxValue);
	}
}

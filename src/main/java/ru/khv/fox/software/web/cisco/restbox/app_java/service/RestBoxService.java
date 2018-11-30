/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.*;

import java.util.Collection;

public interface RestBoxService {

	Mono<Box> checkAccess(@NonNull final String boxName, @NonNull final String secret);

	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready);

	Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready);

	@NonNull
	Collection<Box> getConf();

	Mono<Void> putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func);

	Mono<Void> putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func);

	Mono<Void> putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlRFunctions func);
}

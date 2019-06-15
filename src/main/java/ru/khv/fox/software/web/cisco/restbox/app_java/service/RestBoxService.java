/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;

import java.util.Collection;

public interface RestBoxService {

	@NonNull
	Mono<Box> checkAccess(@NonNull final String boxName, @NonNull final String secret);

	@NonNull
	Mono<BoxControl> getBoxControl(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId);

	@NonNull
	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	@NonNull
	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	@NonNull
	Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready);

	@NonNull
	Collection<Box> getConf();

	// TODO cleanup
/*
	@NonNull
	Mono<Void> putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);

	@NonNull
	Mono<Void> putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);

	@NonNull
	Mono<Void> putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);
*/
}

/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlOnOffFunctions;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlRFunctions;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;

import java.util.Collection;

public interface RestBoxService {

	boolean checkAccess(@NonNull final String boxName, @NonNull final String secret);

	void putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	int getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready);

	@NonNull
	Collection<Box> getConf();

	void putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func);

	void putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func);

	void putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlRFunctions func);
}

/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString(callSuper = true)
public class BoxControlIndicator extends BoxControl {
	// state
	@Nullable
	private BoxControlRFunctions rFunc = BoxControlRFunctions.RNONE;

	BoxControlIndicator(@NonNull final BoxControlTypes type, final int id, @Nullable final String descr) {
		super(type, id, descr);
	}
}

/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/*
 Box Control is a mutable object.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"type", "id"})
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BoxControl {
	// parameters
	@NonNull
	private final BoxControlType type;
	private final int id;
	@Nullable
	private final String descr;
	// state
	private int status;

	// TODO add stub methods to be overriden in childs

	@Nullable
	public BoxControlOnOffFunctions getOnFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public BoxControlOnOffFunctions getOffFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Nullable
	public BoxControlRFunctions getRFunc() {
		throw new UnsupportedOperationException("Method is not implemented");
	}
}

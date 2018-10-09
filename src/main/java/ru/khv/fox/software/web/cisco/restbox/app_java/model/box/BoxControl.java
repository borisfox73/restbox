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
@EqualsAndHashCode(of = {"type", "descr"})
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BoxControl {
	// parameters
	@NonNull
	private final BoxControlTypes type;
	private final int id;
	@Nullable
	private final String descr;
	// state
	private int status;

	// TODO add factory method to compose a concrete type from app properties
}

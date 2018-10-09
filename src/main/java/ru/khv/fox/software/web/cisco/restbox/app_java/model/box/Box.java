/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import lombok.*;
import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.util.Set;
import java.util.stream.Collectors;

/*
Box is a mutable object.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Box {
	// parameters
	@NonNull
	private final String name;
	@NonNull
	private final String secret;
	@NonNull
	private final Set<BoxControl> controls;
	// state
	private int ready;


	public static Box getInstance(@NonNull final AppProperties.BoxProperties boxProperties) {
		val boxControls = boxProperties.getBoxes()
		                               .stream()
		                               .map(bc -> bc.getType().getInstance(bc.getId(), bc.getDescr()))
		                               .collect(Collectors.toSet());
		return new Box(boxProperties.getName(), boxProperties.getSecret(), boxControls);
	}
}

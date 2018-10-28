/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import lombok.*;
import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Box is a mutable object.
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
	//	@NonNull
//	private final Set<BoxControl> controls; // TODO is it really needed ?
	@NonNull
	private final Map<SimpleEntry<BoxControlType, Integer>, BoxControl> controlsMap;  // to lookup by type and id pair
	// state
	private int ready;


	public static Box getInstance(@NonNull final AppProperties.BoxProperties boxProperties) {
		// TODO cleanup
/*
		val boxControls = boxProperties.getBoxes()
		                               .stream()
		                               .map(bc -> bc.getType().getInstance(bc.getId(), bc.getDescr()))
		                               .collect(Collectors.toSet());
*/
		val boxControlsMap = boxProperties.getBoxes()
		                                  .stream()
		                                  .map(bc -> bc.getType().getInstance(bc.getId(), bc.getDescr()))
		                                  .collect(Collectors.toMap(bc -> new SimpleEntry<>(bc.getType(), bc.getId()), bc -> bc));
//		return new Box(boxProperties.getName(), boxProperties.getSecret(), boxControls);
		return new Box(boxProperties.getName(), boxProperties.getSecret(), boxControlsMap);
	}

	public Optional<BoxControl> getControlByTypeAndId(@NonNull final BoxControlType boxControlType, final int boxControlId) {
//		return controls.stream().filter(c -> c.getType() == boxControlType && c.getId() == boxControlId).findAny();
		return Optional.ofNullable(controlsMap.get(new SimpleEntry<>(boxControlType, boxControlId)));
	}

	public void incrementReady() {
		if (ready++ > 99999)
			ready = 0;
	}
}

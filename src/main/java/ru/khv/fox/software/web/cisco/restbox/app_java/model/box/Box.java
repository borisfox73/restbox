/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Box is a mutable object.
 */
@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Box {
	// parameters
	@JsonProperty
	@JsonPropertyDescription("Box name")
	@NonNull
	private final String name;
	@JsonProperty
	@JsonPropertyDescription("Box secret")
	@NonNull
	private final String secret;
	@JsonIgnore
	@NonNull
	private final Map<SimpleEntry<BoxControlType, Integer>, BoxControl> controlsMap;  // to lookup by type and id pair
	// state
	@JsonProperty
	@JsonPropertyDescription("Box state")
	private int ready;


	public static Box getInstance(@NonNull final AppProperties.BoxProperties boxProperties) {
		val boxControlsMap = boxProperties.getBoxes()
		                                  .stream()
		                                  .map(bc -> bc.getType().getInstance(bc.getId(), bc.getDescr(), bc.getRFunc(), bc.getOnFunc(), bc.getOffFunc()))
		                                  .collect(Collectors.toMap(bc -> new SimpleEntry<>(bc.getType(), bc.getId()), bc -> bc));
		return new Box(boxProperties.getName(), boxProperties.getSecret(), boxControlsMap);
	}

	public Optional<BoxControl> getControlByTypeAndId(@NonNull final BoxControlType boxControlType, final int boxControlId) {
		return Optional.ofNullable(controlsMap.get(new SimpleEntry<>(boxControlType, boxControlId)));
	}

	public void incrementReady(@Nullable final Boolean ready) {
		if (ready != null && ready) {
			if (this.ready++ > 99999)
				this.ready = 0;
			log.trace("ready = {}", this.ready);
		}
	}

	@JsonProperty("boxes")
	@JsonPropertyDescription("Box controls")
	@NonNull
	public Collection<BoxControl> getBoxControls() {
		return controlsMap.values();
	}
}

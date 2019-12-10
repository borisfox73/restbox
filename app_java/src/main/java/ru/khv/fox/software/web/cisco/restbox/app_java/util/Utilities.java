/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@UtilityClass
public class Utilities {

	public <T> Function<Mono<Optional<T>>, Publisher<T>> getIfPresent() {
		return x -> x.filter(Optional::isPresent).map(Optional::get);
	}
}

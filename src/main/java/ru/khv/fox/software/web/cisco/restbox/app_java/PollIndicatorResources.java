/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlIndicator;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.ExecFunctionResultPair;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;

import java.util.Collection;
import java.util.Objects;

/**
 * Poll the router resources tied to the boxes indicators (LEDs) state,
 * and set indicators state according to resources state.
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Profile("!test")
public class PollIndicatorResources<Q extends RestApiDTO, T extends RestApiDTO> {
	@NonNull
	private final CiscoRestfulService<Q, T, Integer> ciscoService;
	@NonNull
	private final RestBoxService boxService;
	@NonNull
	private final Collection<Box> boxes;


	// Poll cycle
	@Scheduled(fixedDelayString = "#{appProperties.indicatorPollDelay}")
	public void pollCycle() {
		log.debug("indicators poll cycle started");
		Flux.fromIterable(boxes)
		    .parallel()
		    .runOn(Schedulers.parallel())
		    .doOnNext(box -> log.trace("polling box {}", box))
		    .flatMap(box ->
				             Flux.just(box)
				                 .flatMapIterable(Box::getBoxControls)
				                 .ofType(BoxControlIndicator.class)
				                 .parallel()
				                 .runOn(Schedulers.parallel())
				                 .doOnNext(boxControl -> log.trace("polling box indicator {}", boxControl))
				                 .flatMap(boxControl ->
						                          Mono.just(boxControl)
						                              .map(BoxControlIndicator::getRFunc)
						                              .doOnNext(rFunction -> log.trace("exec rFunction {}", rFunction))
						                              .flatMap(ciscoService::execFunction)
						                              .doOnNext(resultPair -> log.trace("exec result pair {}", resultPair))
						                              .map(ExecFunctionResultPair::getBoxValue)
						                              .filter(Objects::nonNull)
						                              .flatMap(boxStatus -> boxService.putStatus(box.getName(), boxControl.getType(), boxControl.getId(), false, boxStatus))
						                              .doOnNext(boxControl1 -> log.trace("box control after {}", boxControl1))
				                         )
		            )
		    .subscribe();
		// TODO error recovery
	}
}

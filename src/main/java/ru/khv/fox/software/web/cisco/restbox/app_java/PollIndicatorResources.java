/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlIndicator;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoServiceException;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.Utilities;

import java.util.Collection;

/**
 * Poll the router resources tied to the boxes indicators (LEDs) state,
 * and set indicators state according to corresponding resources state.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
public class PollIndicatorResources<Q extends RestApiDTO, T extends RestApiDTO> {
	@NonNull
	private final CiscoRestfulService<Q, T, Integer> ciscoService;
	@NonNull
	private final RestBoxService boxService;
	@NonNull
	private final Collection<Box> boxes;


	// Helper class to format poll results for logging purposes
	private static final class PollResult {
		private final StringBuilder sb = new StringBuilder("Poll result of ");
		private final String result;
		private final boolean error;

		private PollResult(@NonNull final Box box, @NonNull final BoxControl boxControl) {
			this.result = header(box, boxControl).append("SUCCESS (status = ")
			                                     .append(boxControl.getStatus())
			                                     .append(")")
			                                     .toString();
			this.error = false;
		}

		private PollResult(@NonNull final BoxControl boxControl, @NonNull final Box box) {
			this.result = header(box, boxControl).append("NO-OP").toString();
			this.error = false;
		}

		private PollResult(@NonNull final Box box, @NonNull final BoxControl boxControl, @NonNull final Throwable e) {
			header(box, boxControl).append("ERROR (");
			if (e instanceof CiscoServiceException) {
				val ex = (CiscoServiceException) e;
				sb.append(ex.getErrorMessage())
				  .append(": ")
				  .append(ex.getReason());
			} else
				sb.append(e.getLocalizedMessage());
			this.result = sb.append(")").toString();
			this.error = true;
		}

		@NonNull
		private static PollResult of(@NonNull final Box box, @NonNull final BoxControl boxControl) {
			return new PollResult(box, boxControl);
		}

		@NonNull
		private static Mono<PollResult> ofError(@NonNull final Box box, @NonNull final BoxControl boxControl, @NonNull final Throwable e) {
			return Mono.just(new PollResult(box, boxControl, e));
		}

		@NonNull
		private static Mono<PollResult> ofEmpty(@NonNull final Box box, @NonNull final BoxControl boxControl) {
			return Mono.just(new PollResult(boxControl, box));
		}

		@NonNull
		private StringBuilder header(@NonNull final Box box, @NonNull final BoxControl boxControl) {
			return sb.append("Box ")
			         .append(box.getName())
			         .append(" ")
			         .append(boxControl.getType())
			         .append(" #")
			         .append(boxControl.getId())
			         .append(": ");
		}

		private void log() {
			if (error)
				log.error(result);
			else
				log.debug(result);
		}
	}

	// Poll cycle
	@Scheduled(fixedDelayString = "#{appProperties.indicatorPollDelay}")
	void pollCycle() {
		log.debug("indicators poll cycle started");
		pollFlux().subscribe(PollResult::log);
	}

	// public for tests
	public ParallelFlux<PollResult> pollFlux() {
		return Flux.fromIterable(boxes)
		           .parallel()
		           .runOn(Schedulers.parallel())
		           .doOnNext(box -> log.trace("polling box {}", box))
		           .flatMap(box -> Flux.just(box)
		                               .flatMapIterable(Box::getBoxControls)
		                               .ofType(BoxControlIndicator.class)
		                               .parallel()
		                               .runOn(Schedulers.parallel())
		                               .flatMap(boxControl -> pollBoxControl(box, boxControl)
				                               .map(bc -> PollResult.of(box, bc))
				                               .switchIfEmpty(PollResult.ofEmpty(box, boxControl))
				                               .onErrorResume(t -> PollResult.ofError(box, boxControl, t)))
		                   );
	}

	// public for tests
	@NonNull
	public Mono<BoxControl> pollBoxControl(@NonNull final Box box, @NonNull final BoxControl boxControl) {
		return Mono.just(boxControl)
		           .doOnNext(bci -> log.trace("polling box indicator {}", bci))
		           .map(BoxControl::getRouterFunc)
		           .filter(Utilities::nonEmpty)
		           .doOnNext(rFunction -> log.trace("exec rFunction {}", rFunction))
		           .flatMap(ciscoService::execFunction)
		           .doOnNext(resultPair -> log.trace("exec result pair {}", resultPair))
				.<Integer>handle((resultPair, sink) -> {
					val boxValue = resultPair.getBoxValue();
					if (boxValue != null)
						sink.next(boxValue);
				})
				.flatMap(boxStatus -> boxService.putStatus(box.getName(), boxControl.getType(), boxControl.getId(), false, boxStatus))
				.doOnNext(boxControl1 -> log.trace("box control after {}", boxControl1));
	}
}

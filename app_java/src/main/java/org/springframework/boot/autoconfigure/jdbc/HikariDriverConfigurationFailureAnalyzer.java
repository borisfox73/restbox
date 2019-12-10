/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * A stub class to override the one packaged in Spring jars to spare this application
 * from unnecessary org.springframework.jdbc.CannotGetJdbcConnectionException dependency.
 * <p>
 * Unfortunately Spring Boot FailureAnalyzer autoconfiguration doesn't allow exclusion of particular implementations
 * from packaged spring.factories.
 */
class HikariDriverConfigurationFailureAnalyzer extends AbstractFailureAnalyzer<DummyException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, DummyException cause) {
		return null;
	}

}

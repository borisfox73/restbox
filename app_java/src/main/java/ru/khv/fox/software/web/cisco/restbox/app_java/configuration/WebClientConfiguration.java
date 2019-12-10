/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.function.Consumer;

/**
 * Autoconfigured Web Client Builder customization.
 */
@Slf4j
@Configuration
class WebClientConfiguration {

	@Bean
	WebClientCustomizer configureWebclient(@NonNull final AppProperties appProperties) {
		val webClientProperties = appProperties.getWebClient();

		log.debug("Web Client properties: {}", webClientProperties);
		return (@NonNull final WebClient.Builder webClientBuilder) -> {
			val tcpClient = TcpClient.create()
			                         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
			                         .option(ChannelOption.TCP_NODELAY, true)
			                         .option(ChannelOption.SO_KEEPALIVE, true)
			                         .doOnConnected(connection ->
					                                        connection.addHandlerLast(new ReadTimeoutHandler(30))       // to wait for the response
					                                                  .addHandlerLast(new WriteTimeoutHandler(10)));    // to send the request
			var httpClient = HttpClient.from(tcpClient);
			if (webClientProperties.isSslIgnoreValidation()) {
				try {
					val sslContext = SslContextBuilder.forClient()
					                                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
					                                  .build();
					httpClient = httpClient.secure(t -> t.sslContext(sslContext));
				} catch (SSLException e) {
					throw new RuntimeException(e);
				}
			}
			if (webClientProperties.isTraceWebClientRequests()) {
				httpClient = httpClient.wiretap(true);
				final Consumer<ClientCodecConfigurer> consumer = configurer ->
						configurer.defaultCodecs().enableLoggingRequestDetails(true);
				webClientBuilder.exchangeStrategies(ExchangeStrategies.builder().codecs(consumer).build());
			}
			webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
		};
	}
}

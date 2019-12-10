/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterType

import java.time.Duration

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue
import static com.spotify.hamcrest.pojo.IsPojo.pojo
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class IT10Properties {
    @Autowired
    private AppProperties properties

    @Test
    void 'properties loads'() {
        /*
          test: value
          routers:
            testcsr1:
              name: CSR-WAN
              host: 10.1.2.3
              username: rest
              password: restpwd
              type: csrv
            testcsr2:
              name: CSR-WAN2
              host: 10.1.2.4
              username: rest2
              password: restpwd2
              type: asr
         */
        println "loaded properties = " + properties

        assertThat(properties, is(pojo(AppProperties.class)
		        .withProperty("indicatorPollInterval", is(equalTo(Duration.ofMinutes(1))))
		        .withProperty("ciscoRestApiUriTemplate", is(equalTo("https://{hostname}:55443/api/v1/")))
                .withProperty("jwt", is(pojo(AppProperties.JwtProperties.class)
		                .withProperty("issuer", is(optionalWithValue(equalTo("http://localhost"))))
		                .withProperty("audience", is(optionalWithValue(equalTo("restbox_java"))))
		                .withProperty("secret", is("testsecret"))
		                .withProperty("timeToLive", is(equalTo(Duration.ofMinutes(5))))))))

        assertThat(properties, hasProperty("routers", hasEntry(equalTo("testcsr1"), pojo(AppProperties.RouterProperties.class)
                .withProperty("name", is("CSR-WAN"))
		        .withProperty("host", is("192.168.70.70"))
                .withProperty("username", is("rest"))
                .withProperty("password", is("restpwd"))
		        .withProperty("type", is(RouterType.CSRV)))))

	    assertThat(properties, hasProperty("routers", hasEntry(equalTo("testcsr2"), pojo(AppProperties.RouterProperties.class)
			    .withProperty("name", is("CSR-AWS"))
			    .withProperty("host", is("192.168.70.71"))
			    .withProperty("username", is("rest2"))
			    .withProperty("password", is("restpwd2"))
			    .withProperty("type", is(RouterType.ASR)))))

	    assertThat(properties, hasProperty("users", hasItem(pojo(AppProperties.UserProperties.class)
			    .withProperty("username", is("testuser"))
			    .withProperty("password", is("testpass"))
                .withProperty("roles", arrayContaining("USER")))))
    }
}

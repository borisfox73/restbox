/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties

import java.time.Duration
import java.time.temporal.ChronoUnit

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue
import static com.spotify.hamcrest.pojo.IsPojo.pojo
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

@RunWith(SpringRunner.class)
@SpringBootTest
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
                .withProperty("jwt", is(pojo(AppProperties.JwtProperties.class)
                .withProperty("audience", is(optionalWithValue(equalTo("restbox_java"))))
                .withProperty("secret", is("qweasdzxc123"))
                .withProperty("timeToLive", is(equalTo(Duration.of(5, ChronoUnit.MINUTES))))))))

        assertThat(properties, hasProperty("routers", hasEntry(equalTo("testcsr1"), pojo(AppProperties.RouterProperties.class)
                .withProperty("name", is("CSR-WAN"))
                .withProperty("host", is("10.1.2.3"))
                .withProperty("username", is("rest"))
                .withProperty("password", is("restpwd"))
                .withProperty("type", is(AppProperties.RouterProperties.RouterTypes.CSRV)))))

        assertThat(properties, hasProperty("users", hasItem(pojo(AppProperties.UserProperties.class)
                .withProperty("username", is("user1"))
                .withProperty("password", is("user123"))
                .withProperty("roles", arrayContaining("USER")))))
    }
}
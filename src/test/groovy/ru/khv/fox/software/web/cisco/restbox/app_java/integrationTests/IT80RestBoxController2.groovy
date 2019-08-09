/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests


import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IT80RestBoxController2 {

    @LocalServerPort
    private int serverPort

    private static boolean initdone


    @Before
    void init() {
        if (!initdone) {
            IT80RestBoxController1.initRestAssured(serverPort)
            // Set interface state for b1 led 0 rfunction
            IT80RestBoxController1.putStatus('b1', 'cisco123', 'switch', 1, 0)
            // Set acl state for b2 led 1
            IT80RestBoxController1.putStatus('b2', 'cisco456', 'button', 0, 1)
            Thread.sleep(10 * 1000L) // let interface status settle
            initdone = true
        }
    }

    // TODO mock cisco api service to test actions

    @Test
    void 'get status b1 switch 1'() {
        IT80RestBoxController1.getStatus('b1', 'cisco123', 'switch', 1, 0)
    }

	@Test
	void 'get status b2 button 0'() {
        IT80RestBoxController1.getStatus('b2', 'cisco456', 'button', 0, 1)
    }

    @Test
    void 'get status b1 led 0'() {
        // rfunc1
        // inline
        IT80RestBoxController1.getStatus('b1', 'cisco123', 'led', 0, 0, true)
        // stored
        IT80RestBoxController1.getStatus('b1', 'cisco123', 'led', 0, 0)
    }

    @Test
    void 'get status b1 led 1'() {
        // rfunc2
        // inline
        IT80RestBoxController1.getStatus('b1', 'cisco123', 'led', 1, 1, true)
        // stored
        IT80RestBoxController1.getStatus('b1', 'cisco123', 'led', 1, 1)
    }

    @Test
    void 'get status b2 led 0'() {
        // rfunc3
        // inline
        IT80RestBoxController1.getStatus('b2', 'cisco456', 'led', 0, 1, true)
        // stored
        IT80RestBoxController1.getStatus('b2', 'cisco456', 'led', 0, 1)
    }

    @Test
    void 'get status b2 led 1'() {
        // rfunc4
        // inline
        IT80RestBoxController1.getStatus('b2', 'cisco456', 'led', 1, 1, true)
        // stored
        IT80RestBoxController1.getStatus('b2', 'cisco456', 'led', 1, 1)
    }
}

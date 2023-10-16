package com.health.base;

import com.solacesystems.jcsmp.FlowEvent;
import com.solacesystems.jcsmp.FlowEventArgs;
import com.solacesystems.jcsmp.SessionEvent;
import com.solacesystems.jcsmp.SessionEventArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

class SolaceHealthIndicatorTest {

	private SolaceHealthIndicator solaceHealthIndicator;

	@BeforeEach
	void setUp() {
		this.solaceHealthIndicator = new SolaceHealthIndicator();
	}

	@Test
	void healthUp() {
		this.solaceHealthIndicator.healthUp();
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.up().build());
	}

	@Test
	void healthReconnecting() {
		this.solaceHealthIndicator.healthReconnecting(null);
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.status("RECONNECTING").build());
	}

	@Test
	void healthDown() {
		this.solaceHealthIndicator.healthDown(null);
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.down().build());
	}

	@Test
	void addFlowEventDetails() {
		// as SessionEventArgs constructor has package level access modifier, we have to test with FlowEventArgs only
		FlowEventArgs flowEventArgs = new FlowEventArgs(FlowEvent.FLOW_DOWN, "String_infoStr",
				new Exception("Test Exception"), 500);
		Health health = this.solaceHealthIndicator.addEventDetails(Health.down(),flowEventArgs).build();

		assertEquals(health.getStatus(), Status.DOWN);
		assertEquals(health.getDetails().get("error"), "java.lang.Exception: Test Exception");
		assertEquals(health.getDetails().get("responseCode"), 500);
	}

	@Test
	void getHealth() {
		this.solaceHealthIndicator.setHealth(Health.up().build());
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.up().build());
	}

	@Test
	void setHealth() {
		this.solaceHealthIndicator.setHealth(Health.down().build());
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.down().build());
	}
}
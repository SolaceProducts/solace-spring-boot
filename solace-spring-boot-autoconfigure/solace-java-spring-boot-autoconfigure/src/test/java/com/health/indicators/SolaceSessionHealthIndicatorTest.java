package com.health.indicators;

import com.solacesystems.jcsmp.FlowEvent;
import com.solacesystems.jcsmp.FlowEventArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

class SolaceSessionHealthIndicatorTest {

	private SolaceSessionHealthIndicator solaceHealthIndicator;

	@BeforeEach
	void setUp() {
		this.solaceHealthIndicator = new SolaceSessionHealthIndicator();
	}

	@Test
	void up() {
		this.solaceHealthIndicator.up();
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.up().build());
	}

	@Test
	void reconnecting() {
		this.solaceHealthIndicator.reconnecting(null);
		assertEquals(this.solaceHealthIndicator.getHealth().getStatus(), Health.status("RECONNECTING").build().getStatus());
	}

	@Test
	void down() {
		this.solaceHealthIndicator.down(null);
		assertEquals(this.solaceHealthIndicator.getHealth().getStatus(), Status.DOWN);
	}
}
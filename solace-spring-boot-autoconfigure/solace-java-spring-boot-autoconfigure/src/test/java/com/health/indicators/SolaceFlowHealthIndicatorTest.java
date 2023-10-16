package com.health.indicators;

import com.health.base.SolaceHealthIndicator;
import com.solacesystems.jcsmp.FlowEvent;
import com.solacesystems.jcsmp.FlowEventArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SolaceFlowHealthIndicatorTest {

	private SolaceFlowHealthIndicator solaceHealthIndicator;

	@BeforeEach
	void setUp() {
		this.solaceHealthIndicator = new SolaceFlowHealthIndicator();
	}

	@Test
	void up() {
		this.solaceHealthIndicator.up();
		assertEquals(this.solaceHealthIndicator.getHealth(), Health.up().build());
	}

	@Test
	void reconnecting() {
		FlowEventArgs flowEventArgs = new FlowEventArgs(FlowEvent.FLOW_RECONNECTING, "String_infoStr",
				new Exception("Test Exception"), 500);
		this.solaceHealthIndicator.reconnecting(flowEventArgs);
		assertEquals(this.solaceHealthIndicator.getHealth().getStatus(), Health.status("RECONNECTING").build().getStatus());
		assertEquals(this.solaceHealthIndicator.getHealth().getDetails().get("error"), "java.lang.Exception: Test Exception");
		assertEquals(this.solaceHealthIndicator.getHealth().getDetails().get("responseCode"), 500);

	}

	@Test
	void down() {
		FlowEventArgs flowEventArgs = new FlowEventArgs(FlowEvent.FLOW_DOWN, "String_infoStr",
				new Exception("Test Exception"), 500);
		this.solaceHealthIndicator.down(flowEventArgs);
		assertEquals(this.solaceHealthIndicator.getHealth().getStatus(), Status.DOWN);
		assertEquals(this.solaceHealthIndicator.getHealth().getDetails().get("error"), "java.lang.Exception: Test Exception");
		assertEquals(this.solaceHealthIndicator.getHealth().getDetails().get("responseCode"), 500);
	}
}
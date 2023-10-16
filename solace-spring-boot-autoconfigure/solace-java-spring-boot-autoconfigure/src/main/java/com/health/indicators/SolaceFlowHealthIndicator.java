package com.health.indicators;

import com.health.base.SolaceHealthIndicator;
import com.solacesystems.jcsmp.FlowEventArgs;
import org.springframework.lang.Nullable;

public class SolaceFlowHealthIndicator extends SolaceHealthIndicator {
	public void up() {
		super.healthUp();
	}

	public void reconnecting(@Nullable FlowEventArgs eventArgs) {
		super.healthReconnecting(eventArgs);
	}

	public void down(@Nullable FlowEventArgs eventArgs) {
		super.healthDown(eventArgs);
	}

	@Deprecated
	public void down(@Nullable FlowEventArgs eventArgs, boolean resetReconnectCount) {
		super.healthDown(eventArgs);
	}

}

package com.health.indicators;

import com.health.base.SolaceHealthIndicator;
import com.solacesystems.jcsmp.SessionEventArgs;
import org.springframework.lang.Nullable;

public class SolaceSessionHealthIndicator extends SolaceHealthIndicator {
	public void up() {
		super.healthUp();
	}

	public void reconnecting(@Nullable SessionEventArgs eventArgs) {
		super.healthReconnecting(eventArgs);
	}

	public void down(@Nullable SessionEventArgs eventArgs) {
		super.healthDown(eventArgs);
	}

	@Deprecated
	public void down(@Nullable SessionEventArgs eventArgs, boolean resetReconnectCount) {
		super.healthDown(eventArgs);
	}
}

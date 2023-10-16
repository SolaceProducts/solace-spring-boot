package com.health.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@NoArgsConstructor
public class SolaceHealthIndicator implements HealthIndicator {
	private static final String STATUS_RECONNECTING = "RECONNECTING";
	private static final String INFO = "info";
	private static final String RESPONSE_CODE = "responseCode";
	private volatile Health health;
	private final ReentrantLock writeLock = new ReentrantLock();
	private static final Log logger = LogFactory.getLog(SolaceHealthIndicator.class);

	private static void logDebugStatus(String status) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Solace connection/flow status is %s", status));
		}
	}
	protected void healthUp() {
		try {
			writeLock.lock();
			health = Health.up().build();
			logDebugStatus(String.valueOf(Status.UP));
		} finally {
			writeLock.unlock();
		}
	}
	protected <T> void healthReconnecting(@Nullable T eventArgs) {
		try {
			writeLock.lock();
			health = addEventDetails(Health.status(STATUS_RECONNECTING), eventArgs).build();
			logDebugStatus(STATUS_RECONNECTING);
		} finally {
			writeLock.unlock();
		}
	}

	protected <T> void healthDown(@Nullable T eventArgs) {
		try {
			writeLock.lock();
			health = addEventDetails(Health.down(), eventArgs).build();
			logDebugStatus(String.valueOf(Status.DOWN));
		} finally {
			writeLock.unlock();
		}
	}

	public <T> Health.Builder addEventDetails(Health.Builder builder, @Nullable T eventArgs) {
		if (eventArgs == null) {
			return builder;
		}

		try {
			Optional.ofNullable(eventArgs.getClass().getMethod("getException").invoke(eventArgs))
					.ifPresent(ex -> builder.withException((Throwable) ex));
			Optional.of(eventArgs.getClass().getMethod("getResponseCode").invoke(eventArgs))
					.filter(c -> ((int) c) != 0)
					.ifPresent(c -> builder.withDetail(RESPONSE_CODE, c));
			Optional.ofNullable(eventArgs.getClass().getMethod("getInfo").invoke(eventArgs))
					.filter(t -> String.valueOf(t).isBlank())
					.ifPresent(info -> builder.withDetail(INFO, info));
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		return builder;
	}

	@Override
	public Health health() {
		return health;
	}
}
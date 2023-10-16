package com.health.contributors;

import com.health.indicators.SolaceSessionHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolaceBinderHealthContributor implements CompositeHealthContributor {
	private final SolaceSessionHealthIndicator solaceSessionHealthIndicator;
	private final SolaceBindingsHealthContributor solaceBindingsHealthContributor;

	private static final String CONNECTION = "connection";
	private static final String BINDINGS = "bindings";

	public SolaceBinderHealthContributor(SolaceSessionHealthIndicator solaceSessionHealthIndicator,
	                                     SolaceBindingsHealthContributor solaceBindingsHealthContributor) {
		this.solaceSessionHealthIndicator = solaceSessionHealthIndicator;
		this.solaceBindingsHealthContributor = solaceBindingsHealthContributor;
	}

	@Override
	public HealthContributor getContributor(String name) {
		return switch (name) {
			case CONNECTION -> solaceSessionHealthIndicator;
			case BINDINGS -> solaceBindingsHealthContributor;
			default -> null;
		};
	}

	public SolaceSessionHealthIndicator getSolaceSessionHealthIndicator() {
		return solaceSessionHealthIndicator;
	}

	public SolaceBindingsHealthContributor getSolaceBindingsHealthContributor() {
		return solaceBindingsHealthContributor;
	}

	@Override
	public Iterator<NamedContributor<HealthContributor>> iterator() {
		List<NamedContributor<HealthContributor>> contributors = new ArrayList<>();
		contributors.add(NamedContributor.of(CONNECTION, solaceSessionHealthIndicator));
		contributors.add(NamedContributor.of(BINDINGS, solaceBindingsHealthContributor));
		return contributors.iterator();
	}
}

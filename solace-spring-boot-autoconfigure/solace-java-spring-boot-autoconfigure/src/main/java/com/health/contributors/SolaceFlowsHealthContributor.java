package com.health.contributors;

import com.health.indicators.SolaceFlowHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SolaceFlowsHealthContributor implements CompositeHealthContributor {
	private final Map<String, SolaceFlowHealthIndicator> flowHealthContributor = new HashMap<>();

	public void addFlowContributor(String bindingName, SolaceFlowHealthIndicator flowHealthIndicator) {
		flowHealthContributor.put(bindingName, flowHealthIndicator);
	}

	public void removeFlowContributor(String bindingName) {
		flowHealthContributor.remove(bindingName);
	}

	@Override
	public HealthContributor getContributor(String name) {
		return flowHealthContributor.get(name);
	}

	@Override
	public Iterator<NamedContributor<HealthContributor>> iterator() {
		return flowHealthContributor.entrySet()
				.stream()
				.map((entry) -> NamedContributor.of(entry.getKey(), (HealthContributor) entry.getValue()))
				.iterator();
	}
}

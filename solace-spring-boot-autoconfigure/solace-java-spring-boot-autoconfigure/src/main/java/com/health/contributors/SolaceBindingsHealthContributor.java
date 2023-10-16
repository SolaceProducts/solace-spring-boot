package com.health.contributors;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SolaceBindingsHealthContributor implements CompositeHealthContributor {
	private final Map<String, SolaceBindingHealthContributor> bindingHealthContributor = new HashMap<>();

	public void addBindingContributor(String bindingName, SolaceBindingHealthContributor solaceBindingHealthContributor) {
		this.bindingHealthContributor.put(bindingName, solaceBindingHealthContributor);
	}

	public void removeBindingContributor(String bindingName) {
		bindingHealthContributor.remove(bindingName);
	}

	@Override
	public HealthContributor getContributor(String bindingName) {
		return bindingHealthContributor.get(bindingName);
	}

	@Override
	public Iterator<NamedContributor<HealthContributor>> iterator() {
		return bindingHealthContributor.entrySet()
				.stream()
				.map((entry) -> NamedContributor.of(entry.getKey(), (HealthContributor) entry.getValue()))
				.iterator();
	}
}

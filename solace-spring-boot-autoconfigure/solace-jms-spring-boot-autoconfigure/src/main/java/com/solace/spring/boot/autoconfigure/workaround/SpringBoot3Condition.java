package com.solace.spring.boot.autoconfigure.workaround;

import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that matches for Spring Boot version 3.0.
 */
public class SpringBoot3Condition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return SpringBootVersion.getVersion().startsWith("3.0.");
	}
}

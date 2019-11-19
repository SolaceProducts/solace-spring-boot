package com.solace.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@ConditionalOnCloudPlatform(value = CloudPlatform.CLOUD_FOUNDRY)
public class CloudCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata annoMetaData) {
      Environment env =  context.getEnvironment();
      String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
      if ( VCAP_APPLICATION != null  ) {
          String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
          return VCAP_SERVICES != null && VCAP_SERVICES.contains("\"solace-pubsub\"");
      }
      return false;
    }
}

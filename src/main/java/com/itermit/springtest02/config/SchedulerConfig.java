package com.itermit.springtest02.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {


}

package net.ketone.accrptgen.task.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"net.ketone.accrptgen.task"})
@ConfigurationPropertiesScan("net.ketone.accrptgen.task.config.properties")
public class TaskConfiguration {
}

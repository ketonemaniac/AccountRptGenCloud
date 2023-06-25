package net.ketone.accrptgen.task.config;

import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.functions.DateDifFunc;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"net.ketone.accrptgen.task"})
@ConfigurationPropertiesScan("net.ketone.accrptgen.task.config.properties")
public class TaskConfiguration {

    static {
        try {
            FunctionEval.registerFunction("DATEDIF", new DateDifFunc());
        } catch (IllegalArgumentException e) {
            // skip error: POI already implememts DATEDIF for duplicate registers in the JVM
        }
    }

}

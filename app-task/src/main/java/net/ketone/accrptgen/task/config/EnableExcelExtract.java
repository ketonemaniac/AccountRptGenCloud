package net.ketone.accrptgen.task.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {TaskConfiguration.class})
public @interface EnableExcelExtract {
}

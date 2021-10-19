package net.ketone.accrptgen.common.credentials;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {SettingsConfig.class})
public @interface EnableSettings {
}

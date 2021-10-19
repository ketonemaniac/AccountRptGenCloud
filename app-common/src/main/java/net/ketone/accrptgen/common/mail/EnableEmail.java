package net.ketone.accrptgen.common.mail;


import net.ketone.accrptgen.common.credentials.SettingsConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {MockEmailService.class, SendgridEmailService.class})
public @interface EnableEmail {
}

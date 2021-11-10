package net.ketone.accrptgen.common.store;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {CloudStorageConfig.class, LocalStorageConfig.class})
public @interface EnableStorage {
}

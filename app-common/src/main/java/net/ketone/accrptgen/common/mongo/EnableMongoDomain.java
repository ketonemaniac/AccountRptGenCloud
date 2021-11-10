package net.ketone.accrptgen.common.mongo;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {MongoConfig.class})
public @interface EnableMongoDomain {
}

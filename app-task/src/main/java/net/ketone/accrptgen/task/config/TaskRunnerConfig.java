package net.ketone.accrptgen.task.config;

import net.ketone.accrptgen.common.domain.user.UserServiceImpl;
import net.ketone.accrptgen.common.encryption.EncryptionConfig;
import net.ketone.accrptgen.common.mail.EnableEmail;
import net.ketone.accrptgen.common.mongo.EnableMongoDomain;
import net.ketone.accrptgen.common.store.EnableStorage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableMongoDomain
@EnableEmail
@EnableStorage
@Configuration
@ComponentScan(basePackageClasses = {UserServiceImpl.class})
@Import(value = {EncryptionConfig.class})
public class TaskRunnerConfig {
}

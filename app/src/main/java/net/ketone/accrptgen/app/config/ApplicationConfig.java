package net.ketone.accrptgen.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.ketone.accrptgen.common.encryption.EncryptionConfig;
import net.ketone.accrptgen.common.mail.EnableEmail;
import net.ketone.accrptgen.common.mongo.EnableMongoDomain;
import net.ketone.accrptgen.task.config.EnableTasks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMongoDomain
@EnableEmail
@Import(value = {EncryptionConfig.class, MvcConfig.class})
@EnableTasks
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}

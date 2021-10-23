package net.ketone.accrptgen.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.ketone.accrptgen.common.mail.EnableEmail;
import net.ketone.accrptgen.common.mongo.EnableMongoDomain;
import net.ketone.accrptgen.task.config.EnableExcelExtract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongoDomain
@EnableEmail
@EnableExcelExtract
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}

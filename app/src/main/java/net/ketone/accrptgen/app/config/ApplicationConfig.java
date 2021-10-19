package net.ketone.accrptgen.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.ketone.accrptgen.common.mail.EnableEmail;
import net.ketone.accrptgen.common.mongo.EnableMongoDomain;
import net.ketone.accrptgen.task.excelextract.config.EnableExcelExtract;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongoDomain
@EnableExcelExtract
@EnableEmail
@ConfigurationPropertiesScan("net.ketone.accrptgen.app.config.properties")
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}

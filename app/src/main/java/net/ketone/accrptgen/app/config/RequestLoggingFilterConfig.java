package net.ketone.accrptgen.app.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * This is very convenient for request debugging
 * https://www.baeldung.com/spring-http-logging
 * https://stackoverflow.com/questions/35198604/how-to-register-a-filter-to-one-requestmapping-method-only
 * To use this you must enable logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter: DEBUG
 */
@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    @Bean
    public FilterRegistrationBean loggingFilterRegistration(final CommonsRequestLoggingFilter logFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(logFilter);
        registration.addUrlPatterns("/api/admin/*");
        return registration;
    }

}

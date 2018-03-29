package net.ketone.accrptgen.config;

import com.google.api.client.auth.oauth2.Credential;
import net.ketone.accrptgen.admin.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, CredentialsService credentialsService) throws Exception {
        String pwd = credentialsService.getCredentials().getProperty("admin.pwd");
        auth.inMemoryAuthentication()
                .withUser("admin").password(pwd)
                .authorities("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").permitAll()
                .antMatchers("/admin.html").authenticated()
                .anyRequest().permitAll()
                .and().httpBasic()
                .and().csrf().disable();
    }
}
package net.ketone.accrptgen.config;

import com.google.api.client.auth.oauth2.Credential;
import net.ketone.accrptgen.admin.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import static net.ketone.accrptgen.config.Constants.GEN_QUEUE_ENDPOINT;
import static net.ketone.accrptgen.config.Constants.STATUS_QUEUE_ENDPOINT;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/_ah/**").permitAll()
                .antMatchers(GEN_QUEUE_ENDPOINT).permitAll()
                .antMatchers(STATUS_QUEUE_ENDPOINT).permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/api/user/encode/**").permitAll()

//                .anyRequest().permitAll()
                .antMatchers("/api/admin/user/**").hasAuthority("Admin") // .authenticated()
                .anyRequest().hasAuthority("User")  //.authenticated()
                .and()
                .formLogin()
                .defaultSuccessUrl("/", true)
                .and()
                .logout()
                .deleteCookies("JSESSIONID");
    }


    @Bean
    public SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter() {
        return new SecurityContextHolderAwareRequestFilter();
    }
}
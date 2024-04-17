package net.ketone.accrptgen.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import static net.ketone.accrptgen.common.constants.Constants.GEN_QUEUE_ENDPOINT;
import static net.ketone.accrptgen.common.constants.Constants.STATUS_QUEUE_ENDPOINT;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Value("${security.enable:true}")
    private boolean enableSecurity;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        var secu = http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/_ah/**").permitAll()
                .antMatchers(GEN_QUEUE_ENDPOINT).permitAll()
                .antMatchers(STATUS_QUEUE_ENDPOINT).permitAll()
                .antMatchers("/task/**").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/api/user/encode/**").permitAll()
                .antMatchers("/swagger-ui.html","/swagger-ui/**", "/v3/api-docs/**").permitAll()
                ;

        if(!enableSecurity) {
            secu.anyRequest().permitAll();
        } else {
            secu
                    .antMatchers("/api/admin/user/**").hasAuthority("Admin")
                    .antMatchers("/api/settings/**").hasAuthority("Admin")
                    // for login page
                    .antMatchers("/static/js/**").permitAll()
                    .antMatchers("/static/css/**").permitAll()
                    .antMatchers("/static/media/**").permitAll()
                    // default
                    .anyRequest().hasAuthority("User")
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/perform_login")
                    .failureUrl("/login?error=true")
                    .defaultSuccessUrl("/", true)
                    .and()
                    .logout()
                    .deleteCookies("JSESSIONID");
        }
    }


    @Bean
    public SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter() {
        return new SecurityContextHolderAwareRequestFilter();
    }
}
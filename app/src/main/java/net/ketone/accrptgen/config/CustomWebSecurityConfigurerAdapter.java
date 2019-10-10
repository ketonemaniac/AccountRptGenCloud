package net.ketone.accrptgen.config;

import com.google.api.client.auth.oauth2.Credential;
import net.ketone.accrptgen.admin.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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

//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth, CredentialsService credentialsService) throws Exception {
//        String pwd = credentialsService.getCredentials().getProperty("admin.pwd");
//        auth.inMemoryAuthentication()
//                .withUser("admin").password(pwd)
//                .authorities("ROLE_USER");
//    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
//                .antMatchers("/login").permitAll()
//                .antMatchers("/doLogin").permitAll()
//                .antMatchers("/admin.html").authenticated()
                .antMatchers("/_ah/**").permitAll()
                .antMatchers(GEN_QUEUE_ENDPOINT).permitAll()
                .antMatchers(STATUS_QUEUE_ENDPOINT).permitAll()

//                .anyRequest().permitAll()
                .anyRequest().authenticated()

                .and()
                .formLogin()
//                .loginPage("/login")
//                .loginProcessingUrl("/doLogin")
                .defaultSuccessUrl("/", true)
                //.failureUrl("/login.html?error=true")
//                .failureHandler(authenticationFailureHandler())
                .and()
                .logout()
//                .logoutUrl("/doLogout")
                .deleteCookies("JSESSIONID")
//                .logoutSuccessHandler(logoutSuccessHandler());
        ;
    }
}
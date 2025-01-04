package net.ketone.accrptgen.app.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import java.util.HashMap;
import java.util.Map;

import static net.ketone.accrptgen.common.constants.Constants.GEN_QUEUE_ENDPOINT;
import static net.ketone.accrptgen.common.constants.Constants.STATUS_QUEUE_ENDPOINT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class CustomWebSecurityConfigurerAdapter {

    @Value("${security.enable:true}")
    private boolean enableSecurity;

//    @Autowired
//    private UserDetailsService userDetailsService;

//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;

//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
//    }

//    @Bean
//    public UserDetailsService userDetailsService() {
////        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        UserDetails user = User.withUsername("user")
//                .password("{noop}password")
//                .authorities("Admin", "User")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }

    //password Encoder
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) -> {
                    try {
                        var secu = authz
                                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                                .requestMatchers(antMatcher("/static/**")).permitAll()
                                .requestMatchers("/main.bundle.js").permitAll()
                                .requestMatchers("/_ah/**").permitAll()
                                .requestMatchers(GEN_QUEUE_ENDPOINT).permitAll()
                                .requestMatchers(STATUS_QUEUE_ENDPOINT).permitAll()
                                .requestMatchers("/task/**").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/api/user/encode/**").permitAll()
                                .requestMatchers("/swagger-ui.html","/swagger-ui/**",
                                        "/swagger-resources/**",
                                        "/v3/api-docs/**", "/v2/api-docs").permitAll();

                        if(!enableSecurity) {
                            secu.anyRequest().permitAll();
                        } else {
                            secu
                                    .requestMatchers("/api/admin/user/**").hasAuthority("Admin")
                                    .requestMatchers("/api/settings/**").hasAuthority("Admin")
                                    // for login page
                                    .requestMatchers("/static/js/**").permitAll()
                                    .requestMatchers("/static/css/**").permitAll()
                                    .requestMatchers("/static/media/**").permitAll()
                                    // default
                                    .anyRequest().hasAuthority("User");
                        };
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
               })
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/perform_login")
                                .failureUrl("/login?error=true")
                                .defaultSuccessUrl("/", true)
                                .permitAll()
                )
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.deleteCookies("JSESSIONID"))
               .build();
    }


    @Bean
    public SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter() {
        return new SecurityContextHolderAwareRequestFilter();
    }
}
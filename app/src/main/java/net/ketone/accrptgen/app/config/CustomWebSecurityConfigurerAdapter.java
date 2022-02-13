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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static net.ketone.accrptgen.common.constants.Constants.GEN_QUEUE_ENDPOINT;
import static net.ketone.accrptgen.common.constants.Constants.STATUS_QUEUE_ENDPOINT;

/**
 *
 * CSRF exploit
 * https://portswigger.net/web-security/csrf
 * Typically, the attacker will place the malicious HTML onto a web site that they control,
 * and then induce victims to visit that web site. This might be done by feeding the user a link to the web site,
 * via an email or social media message
 * The following is fired from your browser:
 * - Origin: bad-site
 * - Target: normal site which you are authenticated to (hence with cookies)
 *
 * On the other hand, XSS attacks which scripts are run automatically ON THE SAME SITE may compromise CSRF tokens.
 *   https://www.synopsys.com/glossary/what-is-cross-site-scripting.html
 * so, NO UNKNOWN SCRIPTS should ever be allowed to run on the ORIGIN site.
 *
 *
 *
 *
 * Get CSRF tokens:
 * 1. Cookie way
 * https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/csrf.html
 * There can be cases where users will want to persist the CsrfToken in a cookie. By default the CookieCsrfTokenRepository will write to a cookie named XSRF-TOKEN and read it from a header named X-XSRF-TOKEN
 *
 * https://www.information-age.com/how-to-fix-the-csrf-vulnerability-in-popular-web-frameworks-123484913/
 * Why does it work? All web browsers implement a feature named “same-origin policy”. It restricts a website to read the cookies saved by another website or create custom request headers for another website. So, it prevents other sites to read the token set by a website. That’s the reason this method works.
 * https://stackoverflow.com/questions/20504846/why-is-it-common-to-put-csrf-prevention-tokens-in-cookies
 * The attacker will be able to cause a request to the server with both the auth token cookie and the CSRF cookie in the request headers. But the server is not looking for the CSRF token as a cookie in the request headers, it's looking in the payload of the request.
 *
 *
 *
 * 2. Call way (seems this is the modern way)
 * https://www.stackhawk.com/blog/react-csrf-protection-guide-examples-and-how-to-enable-it/
 *     const response = await axios.get('/getCSRFToken');
 *     axios.defaults.headers.post['X-CSRF-Token'] = response.data.CSRFToken;
 *
 * https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/mvc.html#mvc-csrf-resolver
 * Spring Security provides CsrfTokenArgumentResolver which can automatically resolve the current CsrfToken for Spring MVC arguments. By using @EnableWebSecurity you will automatically have this added to your Spring MVC configuration
 *    @RequestMapping("/csrf")
 *    public CsrfToken csrf(CsrfToken token) {
 * 		return token;
 *    }
 *
 * Seems the call way is safe according to this thread:
 * https://github.com/pillarjs/understanding-csrf/issues/6
 * The CSRF call must not enable CORS.
 * CORS (Cross-Origin Resource Sharing) and SOP (Same-Origin Policy) are server-side configurations that browsers decide to enforce or not.
 * All browsers
 * - restrict all ajax scripts doing cross site calls unless CORS enabled
 * - do a preflight request for non-simple calls (but checks/restricts depending on the response anyway)
 * CORS example:
 * http://janodvarko.cz/tests/bugzilla/1376253/
 *
 *
 * CORS: how to enable cross site origin sharing in a browser:
 *  https://spring.io/guides/gs/rest-service-cors/
 *  public void addCorsMappings(CorsRegistry registry) {
 * 				registry.addMapping("/greeting-javaconfig").allowedOrigins("http://localhost:8080");
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
 * https://stackoverflow.com/questions/36250615/cors-with-postman
 *
 *
 *
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Value("${security.enable:true}")
    private boolean enableSecurity;

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
        var secu = http
//                .csrf().disable()
                .csrf().ignoringAntMatchers("/login")
                .ignoringAntMatchers("/perform_login")
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeRequests()
                .antMatchers("/_ah/**").permitAll()
                .antMatchers(GEN_QUEUE_ENDPOINT).permitAll()
                .antMatchers(STATUS_QUEUE_ENDPOINT).permitAll()
                .antMatchers("/task/**").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/csrf").permitAll()
                .antMatchers("/api/user/encode/**").permitAll()
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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }
}
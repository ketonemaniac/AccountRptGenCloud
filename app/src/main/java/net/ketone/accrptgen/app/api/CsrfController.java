package net.ketone.accrptgen.app.api;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/mvc.html#mvc-csrf-resolver
 */
@RestController
public class CsrfController {

    @RequestMapping("/csrf")
     public CsrfToken csrf(CsrfToken token) {
  		return token;
     }

}

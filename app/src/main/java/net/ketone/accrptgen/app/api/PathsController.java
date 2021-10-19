package net.ketone.accrptgen.app.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Forward would not alter the URL, while redirect: will
 */
@Controller
public class PathsController {

    @RequestMapping( method = {RequestMethod.OPTIONS, RequestMethod.GET}, path = {"/app/**", "/",
            "/login"} )
    public String forwardReactPaths() {
        return "forward:/index.html";
    }
}
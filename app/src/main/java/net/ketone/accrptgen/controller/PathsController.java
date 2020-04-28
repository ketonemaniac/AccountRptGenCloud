package net.ketone.accrptgen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PathsController {

    @RequestMapping( method = {RequestMethod.OPTIONS, RequestMethod.GET}, path = {"/app/**", "/"} )
    public String forwardReactPaths() {
        return "forward:/index.html";
    }
}
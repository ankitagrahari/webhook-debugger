package org.backendbrilliance.uiservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SPAController {

    @RequestMapping(value = {
            "/", "/login", "/register", "/dashboard/**", "/upgrade"
    })
    public String spa() {
        return "forward:/index.html";
    }
}

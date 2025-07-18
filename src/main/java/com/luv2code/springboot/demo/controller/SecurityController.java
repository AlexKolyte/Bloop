package com.luv2code.springboot.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {
    @GetMapping("/loginPage")
    public String home()
    {
        return "index";
    }
}

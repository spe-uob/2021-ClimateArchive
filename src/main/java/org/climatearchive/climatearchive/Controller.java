package org.climatearchive.climatearchive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping("/")
    @ResponseBody
    public String test() {
        return "Welcome to our server";
    }

    @GetMapping("/stuff")
    @ResponseBody
    public String test2() {
        return "Welcome to our server 2.0";
    }
}

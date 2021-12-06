package org.climatearchive.climatearchive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(@RequestParam("model") String model, @RequestParam("lat") int lat, @RequestParam("lon") int lon) {

        return "Welcome to our server";
    }

    @GetMapping("/stuff")
    @ResponseBody
    public String test2() {
        return "Welcome to our server 2.0";
    }
}

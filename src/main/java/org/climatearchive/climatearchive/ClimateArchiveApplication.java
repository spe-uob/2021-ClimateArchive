package org.climatearchive.climatearchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
public class ClimateArchiveApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ClimateArchiveApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ClimateArchiveApplication.class, args);
    }

    @GetMapping("/")
    @ResponseBody
    public String test() {
        return "Welcome to our server";
    }

}

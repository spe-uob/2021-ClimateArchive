package org.climatearchive.climatearchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Arrays;

@SpringBootApplication
public class ClimateArchiveApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ClimateArchiveApplication.class);
    }

    public static void main(String[] args) {
        int adminIndex = Arrays.asList(args).indexOf("--add_models");
        if (adminIndex != -1) {
            if (Arrays.stream(args).noneMatch(arg -> arg.startsWith("--models="))) {
                System.out.println("No models listed to be added\nUse --models=<models to add seperated by \",\">");
                return;
            }
            String[] newArgs = new String[args.length + 2];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = "--logging.level.root=WARN"; // disable info logs
            newArgs[args.length + 1] = "--spring.main.web-application-type=none";
            args = newArgs;
        }
        SpringApplication.run(ClimateArchiveApplication.class, args);
    }

}

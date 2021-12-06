package org.climatearchive.climatearchive;

import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(
//            @RequestParam("model") String model, @RequestParam("lat") int lat, @RequestParam("lon") int lon
    ) {
        try (NetcdfFile ncfile = NetcdfFiles.open("./data/tEyea/climate/teyeaa.pdclann.nc")) {
            Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
            Variable rain = ncfile.findVariable(("precip_mm_srf"));
            if (temp == null || rain == null) {
                return "Invalid variable name";
            }
            System.out.println(temp.read());
        } catch (IOException e) {
            System.out.println(e);
            return "Model not found";
        }
        return "Welcome to our server";
    }

    @GetMapping("/stuff")
    @ResponseBody
    public String test2() {
        return "Welcome to our server 2.0";
    }
}

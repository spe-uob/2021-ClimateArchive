package org.climatearchive.climatearchive;

import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GridCoordinate2D;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@RestController
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(
//            @RequestParam("model") String model,
//            @RequestParam("lat") int lat, @RequestParam("lon") int lon
    ) {
        try (NetcdfFile ncfile = NetcdfFiles.open("./data/tEyea/climate/teyeaa.pdclann.nc")) {
            Variable lats = ncfile.findVariable("latitude");
            Variable lons = ncfile.findVariable("longitude");
            Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
            Variable rain = ncfile.findVariable(("precip_mm_srf"));
            if (temp == null || rain == null || lats == null || lons == null) {
                return "Invalid variable name";
            }
//            Point lookup = findClosestPoint(lat, lon, lats, lons);
            return temp.read().reduce().toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "Model not found";
        }
    }


    Point findClosestPoint(int lat, int lon, Variable lats, Variable lons) {
        return new Point(0, 0);
    }

}

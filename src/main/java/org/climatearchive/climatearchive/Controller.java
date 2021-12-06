package org.climatearchive.climatearchive;

import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GridCoordinate2D;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(
//            @RequestParam("model") String model,
            @RequestParam("lat") float lat, @RequestParam("lon") float lon
    ) {
        try (NetcdfFile ncfile = NetcdfFiles.open("./data/tEyea/climate/teyeaa.pdclann.nc")) {
            Variable lats = ncfile.findVariable("latitude");
            Variable lons = ncfile.findVariable("longitude");
            Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
            Variable rain = ncfile.findVariable(("precip_mm_srf"));
            if (temp == null || rain == null || lats == null || lons == null) {
                return "Invalid variable name";
            }
            Point lookup = findClosestPoint(lat, lon, (float[]) lats.read().copyTo1DJavaArray(), (float[]) lons.read().copyTo1DJavaArray());
            float[][] tempData = (float[][]) temp.read().reduce().copyToNDJavaArray();
            float[][] rainData = (float[][]) rain.read().reduce().copyToNDJavaArray();
            return tempData[lookup.x][lookup.y] + "," + rainData[lookup.x][lookup.y];
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "Model not found";
        }
    }


    Point findClosestPoint(float lat, float lon, float[] lats, float[] lons) {
        int x = indexOfClosest(lat, lats);
        int y = indexOfClosest(lon, lons);
        return new Point(x, y);
    }

    int indexOfClosest(float target, float[] arr) {
        float smallestDiff = Float.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < arr.length; i++) {
            float diff = Math.abs(arr[i] - target);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

}


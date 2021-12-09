package org.climatearchive.climatearchive;

import org.springframework.web.bind.annotation.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.IOException;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(
            @RequestParam("model") String model,
            @RequestParam("lat") float lat, @RequestParam("lon") float lon
    ) {
        StringBuilder result = new StringBuilder("field,temp,rain");
        for (String field: new String[]{"ann","jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"}) {
            try (NetcdfFile ncfile = NetcdfFiles.open("./data/" + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
                Variable lats = ncfile.findVariable("latitude");
                Variable lons = ncfile.findVariable("longitude");
                Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
                Variable rain = ncfile.findVariable(("precip_mm_srf"));
                if (temp == null || rain == null || lats == null || lons == null) {
                    continue;
                }
                Point lookup = findClosestPoint(lat, lon, (float[]) lats.read().copyTo1DJavaArray(), (float[]) lons.read().copyTo1DJavaArray());
                float[][] tempData = (float[][]) temp.read().reduce().copyToNDJavaArray();
                float[][] rainData = (float[][]) rain.read().reduce().copyToNDJavaArray();
                result.append("\n").append(field).append(",").append(tempData[lookup.x][lookup.y]).append(",").append(rainData[lookup.x][lookup.y]);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return result.toString();
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


package org.climatearchive.climatearchive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller {
    @GetMapping("/getData")
    @ResponseBody
    public String test(
            @RequestParam("model") String model,
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon
    ) {
        StringBuilder result = new StringBuilder("field,temp,rain");
        for (String field: new String[]{"ann","jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"}) {
            try (NetcdfFile ncfile = NetcdfFiles.open("./data/" + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
                float[] lats = (float[]) Objects.requireNonNull(ncfile.findVariable("latitude")).read().copyTo1DJavaArray();
                float[] lons = (float[]) Objects.requireNonNull(ncfile.findVariable("longitude")).read().copyTo1DJavaArray();
                if (lat > lats[0] || lat < lats[lats.length-1]) {
                    return ("Please select a value for latitude between " + lats[lats.length-1] + " & " + lats[0]);
                }
                if (lon < lons[0] || lon > lons[lons.length-1]) {
                    return ("Please select a value for longitude between " + lons[0] +  " & " + lons[lons.length-1]);
                }
                Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
                Variable rain = ncfile.findVariable(("precip_mm_srf"));
                if (temp == null || rain == null || lats == null || lons == null) {
                    continue;
                }
                Point lookup = findClosestPoint(lat, lon, lats, lons);
                float[][] tempData = (float[][]) temp.read().reduce().copyToNDJavaArray();
                float[][] rainData = (float[][]) rain.read().reduce().copyToNDJavaArray();
                result.append("\n").append(field).append(",").append(tempData[lookup.x][lookup.y]).append(",").append(rainData[lookup.x][lookup.y]);
                result.append("\n").append(tempData[lookup.x][lookup.y]).append(",").append(rainData[lookup.x][lookup.y]);
            } catch (FileNotFoundException e) {
                return "Model " + '"' + model + '"' + " not found";
            } catch (NullPointerException e) {
                return "Missing data";
            } catch (IOException e) {
                return e.getMessage();
            }
        }
        return result.toString();
    }


    Point findClosestPoint(float lat, float lon, float[] lats, float[] lons) {
        int x = indexOfClosest(lat, lats);
        int y = indexOfClosest(lon, lons);
        return new Point(x, y);
    }

    int indexOfClosest(float target, float @NotNull [] arr) {
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(@NotNull MissingServletRequestParameterException e) {
        return ("Parameter " + '"' + e.getParameterName() + '"' + " not provided");
    }

}


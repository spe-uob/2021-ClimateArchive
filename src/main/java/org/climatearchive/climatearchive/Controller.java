package org.climatearchive.climatearchive;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller {

    Pattern modelFormat = Pattern.compile("^[a-z,A-Z]{5}$");

    @GetMapping("/getData")
    @ResponseBody
    public ResponseEntity<Object> test(
            @RequestParam("model") String model,
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon
    ) {
        if (modelFormat.matcher(model).find()) {
            StringBuilder result = new StringBuilder("field,temp,rain");
            for (String field : new String[]{"ann", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"}) {
                try (NetcdfFile ncfile = NetcdfFiles.open("./data/" + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
                    float[] lats = (float[]) Objects.requireNonNull(ncfile.findVariable("latitude")).read().copyTo1DJavaArray();
                    float[] lons = (float[]) Objects.requireNonNull(ncfile.findVariable("longitude")).read().copyTo1DJavaArray();
                    if (lat > lats[0] || lat < lats[lats.length - 1]) { //todo change lat to mod so that all values are accepted
                        return new ResponseEntity<>("Please select a value for latitude between " + lats[lats.length - 1] + " & " + lats[0], HttpStatus.BAD_REQUEST);
                    }
                    if (lon < lons[0] || lon > lons[lons.length - 1]) { //todo change lon to mod so that all values are accepted
                        return new ResponseEntity<>("Please select a value for longitude between " + lons[0] + " & " + lons[lons.length - 1], HttpStatus.BAD_REQUEST);
                    }
                    Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
                    Variable rain = ncfile.findVariable(("precip_mm_srf"));
                    if (temp == null || rain == null) {
                        continue;
                    }
                    Point lookup = findClosestPoint(lat, lon, lats, lons);
                    float[][] tempData = (float[][]) temp.read().reduce().copyToNDJavaArray();
                    float[][] rainData = (float[][]) rain.read().reduce().copyToNDJavaArray();
                    result.append("\n").append(field).append(",").append(tempData[lookup.x][lookup.y]).append(",").append(rainData[lookup.x][lookup.y]);
                } catch (NullPointerException e) {
                    return new ResponseEntity<>("Missing data.", HttpStatus.BAD_REQUEST);
                } catch (IOException e) {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Model " + '"'  + model + '"' + " not found.", HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<String> handleMissingParams(@NotNull MissingServletRequestParameterException e) {
        return new ResponseEntity<>("Parameter " + '"' + e.getParameterName() + '"' + " not provided.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleArgumentTypeMismatch(@NotNull MethodArgumentTypeMismatchException e) {
        return new ResponseEntity<>("Parameter " + '"' + e.getParameter().getParameterName() + '"' + " needs to be of type " + e.getRequiredType(), HttpStatus.BAD_REQUEST);
    }


}


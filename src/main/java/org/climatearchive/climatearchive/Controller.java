package org.climatearchive.climatearchive;

import org.climatearchive.climatearchive.datasources.DataSource;
import org.climatearchive.climatearchive.datasources.GriddedData;
import org.climatearchive.climatearchive.modeldb.Model;
import org.climatearchive.climatearchive.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class Controller {

    public final static String getModelSQL = "SELECT * FROM model_data WHERE model_name = ?";

    private final JdbcTemplate modelDataBase;

    @Value("${data_location}")
    private String data_location;

    @Autowired
    public Controller(JdbcTemplate modelDataBase) {
        this.modelDataBase = modelDataBase;
    }

    @GetMapping("/getData")
    @ResponseBody
    public ResponseEntity<Object> test(
            @RequestParam("model") String model,
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon
    ) {
        Model r = getModelData(model);
        if (r != null) {
//            StringBuilder result = new StringBuilder("field,temp,rain");
//            for (String field : new String[]{"ann", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"}) {
//                try (NetcdfFile ncfile = NetcdfFiles.open(data_location + '/' + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
//                    float[] lats = (float[]) Objects.requireNonNull(ncfile.findVariable(r.getLatitude_value())).read().copyTo1DJavaArray();
//                    float[] lons = (float[]) Objects.requireNonNull(ncfile.findVariable(r.getLongitude_value())).read().copyTo1DJavaArray();
//                    Variable temp = ncfile.findVariable(("temp_mm_1_5m"));
//                    Variable rain = ncfile.findVariable(("precip_mm_srf"));
//                    if (temp == null || rain == null) {
//                        continue;
//                    }
//                    Point lookup = findClosestPoint(lat, lon, lats, lons);
//                    float[][] tempData = (float[][]) temp.read().reduce().copyToNDJavaArray();
//                    float[][] rainData = (float[][]) rain.read().reduce().copyToNDJavaArray();
//                    result.append("\n").append(field).append(",").append(tempData[lookup.x][lookup.y]).append(",").append(rainData[lookup.x][lookup.y]);
//                } catch (NullPointerException e) {
//                    return new ResponseEntity<>("Missing data.", HttpStatus.BAD_REQUEST);
//                } catch (IOException e) {
//                    return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//                }
//            }
//            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
            List<String> fields = List.of("ann", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");
            List<String> variables = List.of("temp_mm_1_5m", "precip_mm_srf");
            DataSource dataSource = new GriddedData(r);
            Pair<String, List<Float[]>> data = dataSource.getClosest2DPointData(fields, variables, lat, lon, data_location);
            StringBuilder result = new StringBuilder("field,").append(String.join(",", variables)).append("\n"); // header of csv
            for (int i = 0; i < fields.size(); i++) {
                result.append(fields.get(i)).append(",");
                result.append(Arrays.stream(data.getSecond().get(i)).map(String::valueOf).collect(Collectors.joining(",")));
                result.append("\n");
            }
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Model " + '"'  + model + '"' + " not found.", HttpStatus.BAD_REQUEST);
        }
    }

    Model getModelData(String model) {
        try {
            return modelDataBase.queryForObject(getModelSQL, (rs, rowNum) -> new Model(
                    rs.getString("model_name"),
                    rs.getString("latitude_value"),
                    rs.getString("longitude_value"),
                    rs.getString("model_path_template")
            ), model);
        } catch (DataAccessException e) {
            return null;
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


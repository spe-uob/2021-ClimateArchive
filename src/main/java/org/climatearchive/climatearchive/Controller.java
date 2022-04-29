package org.climatearchive.climatearchive;

import org.climatearchive.climatearchive.datasources.DataSource;
import org.climatearchive.climatearchive.datasources.GriddedData;
import org.climatearchive.climatearchive.modeldb.Model;
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
            List<String> fields = List.of("ann", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");
            List<String> variables = List.of("temp_mm_1_5m", "precip_mm_srf");
            DataSource dataSource = new GriddedData(r);
            List<Float[]> data = dataSource.getClosest2DPointData(fields, variables, lat, lon, data_location);
            StringBuilder result = new StringBuilder("field,").append(String.join(",", variables)).append("\n"); // header of csv
            for (int i = 0; i < fields.size(); i++) {
                result.append(fields.get(i)).append(",");
                result.append(Arrays.stream(data.get(i)).map(String::valueOf).collect(Collectors.joining(",")));
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(@NotNull MissingServletRequestParameterException e) {
        return new ResponseEntity<>("Parameter " + '"' + e.getParameterName() + '"' + " not provided.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleArgumentTypeMismatch(@NotNull MethodArgumentTypeMismatchException e) {
        return new ResponseEntity<>("Parameter " + '"' + e.getParameter().getParameterName() + '"' + " needs to be of type " + e.getRequiredType(), HttpStatus.BAD_REQUEST);
    }


}


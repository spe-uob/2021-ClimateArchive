package org.climatearchive.climatearchive;

import org.climatearchive.climatearchive.modeldb.Model;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AdminController {

    public static final String getModelSQL = "SELECT * FROM model_data WHERE model_name = ?";
    private final static String addModelSQL = "INSERT OR IGNORE INTO model_data VALUES (?, ?, ?)";

    private final JdbcTemplate modelDataBase;

    @Autowired
    public AdminController(JdbcTemplate modelDataBase) {
        this.modelDataBase = modelDataBase;
    }

    @PostMapping("/admin")
    public ResponseEntity<Object> addModels(@RequestBody @NotNull List<String> models) { // add models to sqlite db
//    public ResponseEntity<Object> addModels() { // add models to sqlite db
        List<String> failedModels = new ArrayList<>();
        for (String m: models) {
            // add model m to sql database
            int success = modelDataBase.update(addModelSQL, "test3", "lat3", "lon3");
            if (success == 0) {
                failedModels.add(m);
            }
        }
        if (failedModels.isEmpty()) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else if (failedModels.size() != models.size()) {
            return new ResponseEntity<>("Failed to add:\n - " + String.join("\n - ", failedModels), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Couldn't add any models to DB", HttpStatus.EXPECTATION_FAILED);
        }
//        String modelID = "test3";
//        Model r = modelDataBase.queryForObject(getModelSQL, (rs, rowNum) -> new Model(
//                rs.getString("model_name"),
//                rs.getString("latitude_value"),
//                rs.getString("longitude_value")
//        ), modelID);
//        return new ResponseEntity<>(r, HttpStatus.OK);
    }
}

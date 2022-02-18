package org.climatearchive.climatearchive;

import org.climatearchive.climatearchive.modeldb.Model;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
public class AdminController {

    public final static String getModelSQL = "SELECT * FROM model_data WHERE model_name = ?";
    private final static String addModelSQL = "INSERT OR IGNORE INTO model_data VALUES (?, ?, ?)";

    Pattern modelFormat = Pattern.compile("^[a-z,A-Z]{5}$");
    private final static String[] possibleLatValues = new String[]{"lat","latitude"};
    private final static String[] possibleLonValues = new String[]{"lon","longitude"};

    private final JdbcTemplate modelDataBase;

    @Autowired
    public AdminController(JdbcTemplate modelDataBase) {
        this.modelDataBase = modelDataBase;
    }

    private String[] extractModelInformation(NetcdfFile ncfile) {
        String[] result = new String[2];
        for (String pLat : possibleLatValues) {
            try {
                Variable v = ncfile.findVariable(pLat);
                if (v == null) continue;
                Array data = v.read();
                if (data.getShape().length != 1 && data.getDataType() != DataType.FLOAT) continue;
                result[0] = pLat;
                break;
            } catch (IOException ignored) {}
        }
        if (result[0] == null) return null;
        for (String pLon: possibleLonValues) {
            try {
                Variable v = ncfile.findVariable(pLon);
                if (v == null) continue;
                Array data = v.read();
                if (data.getShape().length != 1 && data.getDataType() != DataType.FLOAT) continue;
                result[1] = pLon;
                break;
            } catch (IOException ignored) {}
        }
        if (result[1] == null) return null;
        return result;
    }

    private String[] getModelInformation(String model) {
        for (String field : new String[]{"ann", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"}) {
            try (NetcdfFile ncfile = NetcdfFiles.open("./data/" + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
                String[] info = extractModelInformation(ncfile);
                if (info != null) {
                    return info;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    @PostMapping("/admin")
    public ResponseEntity<Object> addModels(@RequestBody @NotNull List<String> models) { // add models to sqlite db
        List<String> failedModels = new ArrayList<>();
        for (String m: models) {
            if (modelFormat.matcher(m).matches()) {
                String[] info = getModelInformation(m);
                if (info != null) {
                    int success = modelDataBase.update(addModelSQL, m, info[0], info[1]);
                    if (success == 0) {
                        failedModels.add(m);
                    }
                } else {
                    failedModels.add(m);
                }
            } else {
                failedModels.add(m);
            }
        }
        if (failedModels.isEmpty()) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else if (failedModels.size() != models.size()) {
            return new ResponseEntity<>("Failed to add:\n - " + String.join("\n - ", failedModels), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Couldn't add any models to DB", HttpStatus.OK);
        }
//        var m = models.get(0);
//        try {
//            Model r = modelDataBase.queryForObject(getModelSQL, (rs, rowNum) -> new Model(
//                    rs.getString("model_name"),
//                    rs.getString("latitude_value"),
//                    rs.getString("longitude_value")
//            ), m);
//            return new ResponseEntity<>(r, HttpStatus.OK);
//        } catch (EmptyResultDataAccessException e) {
//            return new ResponseEntity<>("model: " + m + " does not exist", HttpStatus.BAD_REQUEST);
//        } catch (DataAccessException e) {
//            return new ResponseEntity<>("error fetching with database", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }
}

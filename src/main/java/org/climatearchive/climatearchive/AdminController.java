package org.climatearchive.climatearchive;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "add_models")
public class AdminController {

    private final static String addModelSQL = "INSERT OR IGNORE INTO model_data VALUES (?, ?, ?, ?)";
    private final static String createTable = "CREATE TABLE IF NOT EXISTS model_data( model_name String not null constraint model_data_pk primary key, latitude_value String not null, longitude_value String not null, model_path_template String not null)";

    Pattern modelFormat = Pattern.compile("^[a-z,A-Z]{5}$");
    private final static String[] possibleLatValues = new String[]{"lat","latitude"};
    private final static String[] possibleLonValues = new String[]{"lon","longitude"};

    private final JdbcTemplate modelDataBase;

    @Value("${data_location}")
    private String data_location;

    @Value("${models}")
    private String new_models;

    @Value("${model_sep}")
    private String model_sep;

    @Value("${model_templates}")
    private String model_templates;

    @Value(("${model_templates_sep}"))
    private String model_templates_sep;

    @Autowired
    public AdminController(JdbcTemplate modelDataBase) {
        this.modelDataBase = modelDataBase;
        modelDataBase.execute(createTable);
    }

    @PostConstruct
    public void configureModels() {
        List<String> models = new ArrayList<>();
        if (new_models == null) {
            System.out.println("\n\nNo models to be added\n---------------------\n\n");
            return;
        }
        System.out.println("\nAdding new models\n-----------------");
        for (String m : new_models.split(model_sep)) {
            if (modelFormat.matcher(m).matches()) {
                models.add(m);
            } else {
                System.out.println("Couldn't add model: " + m + " as it's not formatted correctly");
            }
        }
        addModels(models);
        System.out.println("Starting server. This will fail if the server is already running");
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
            try (NetcdfFile ncfile = NetcdfFiles.open(data_location + '/' + model + "/climate/" + model.toLowerCase() + "a.pdcl" + field + ".nc")) {
                String[] info = extractModelInformation(ncfile);
                if (info != null) {
                    return info;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void addModels(@RequestBody @NotNull List<String> models) { // add models to sqlite db
        List<String> failedModels = new ArrayList<>();
        for (String m: models) {
            String[] info = getModelInformation(m);
            if (info != null) {
                int success = modelDataBase.update(addModelSQL, m, info[0], info[1], ""); // todo replace "" with model path template
                if (success == 0) {
                    failedModels.add(m + " - couldn't add to database (it might already exist)");
                } else {
                    System.out.println(" - " + m);
                }
            } else {
                failedModels.add(m + " - issue reading file");
            }
        }
        if (!failedModels.isEmpty()) {
            System.out.println("\nFailed to add models\n--------------------");
            failedModels.forEach(s -> System.out.println(" - " + s));
            System.out.println("\n");
        }
    }
}

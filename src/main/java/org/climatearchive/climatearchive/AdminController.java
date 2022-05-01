package org.climatearchive.climatearchive;

import org.climatearchive.climatearchive.modeldb.Model;
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

    private final static Pattern modelFormat = Pattern.compile("^[a-z,A-Z]{5}$");
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

    private String[] model_templates_list = null;

    @Value("${fields}")
    private String fields;

    @Value("${fields_sep}")
    private String fields_sep;

    private List<String> fieldsList = null;

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

    public void addModels(@RequestBody @NotNull List<String> models) { // add models to sqlite db
        List<String> failedModels = new ArrayList<>();
        for (String m: models) {
            String[] info = getModelInformation(m);
            if (info != null) {
                int success = modelDataBase.update(addModelSQL, m, info[0], info[1], info[2]);
                if (success == 0) {
                    failedModels.add(m + " - couldn't add to database (it might already exist)");
                } else {
                    System.out.println(" - " + m);
                }
            } else {
                failedModels.add(m + " - issue reading model. There may not be a matching template");
            }
        }
        if (!failedModels.isEmpty()) {
            System.out.println("\nFailed to add models\n--------------------");
            failedModels.forEach(s -> System.out.println(" - " + s));
            System.out.println("\n");
        }
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
        int maxFulfilledFields = 0;
        String[] maxInformation = null;
        for (String model_template : getModel_templates_list()) {
            int templateTotal = 0;
            String[] information = null;
            for (String field : getFieldsList()) {
                String filePath = data_location + "/" + Model.getModel_path(model_template, model, field);
                try (NetcdfFile ncfile = NetcdfFiles.open(filePath)) {
                    String[] info = extractModelInformation(ncfile);
                    if (info != null) {
                        templateTotal += 1;
                        if (information == null) {
                            information = info;
                        } else if (!information[0].equals(info[0]) || !information[1].equals(info[1])) {
                            System.out.println("Inconsistent lat and lon values");
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (templateTotal > maxFulfilledFields) {
                maxFulfilledFields = templateTotal;
                maxInformation = new String[]{information[0], information[1], model_template};
            }
        }
        return maxInformation;
    }

    private String[] getModel_templates_list() {
        if (model_templates_list == null) {
            model_templates_list = model_templates.split(model_templates_sep);
        }
        return  model_templates_list;
    }

    List<String> getFieldsList() {
        if (fieldsList == null) {
            fieldsList = List.of(fields.split(fields_sep));
            return fieldsList;
        }
        return fieldsList;
    }
}

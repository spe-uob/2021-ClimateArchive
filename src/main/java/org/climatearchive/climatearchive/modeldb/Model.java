package org.climatearchive.climatearchive.modeldb;

public class Model {
    private final String model_name;
    private final String latitude_value;
    private final String longitude_value;
    private final String model_path_template;

    public Model(String model_name, String latitude_value, String longitude_value, String model_path_template) {
        this.model_name = model_name;
        this.latitude_value = latitude_value;
        this.longitude_value = longitude_value;
        this.model_path_template = model_path_template;
    }

    public String getModel_name() {
        return model_name;
    }

    public String getLatitude_value() {
        return latitude_value;
    }

    public String getLongitude_value() {
        return longitude_value;
    }

    public String getModel_path(String field) {
        String model_path = this.model_path_template;
        // inject model name
        if (model_path.contains("<CLIMATE_ARCHIVE_MODEL_TEMPLATE>")) {
            model_path = model_path.replace("<CLIMATE_ARCHIVE_MODEL_TEMPLATE>", model_name);
        } else if (model_path.contains("<climate_archive_model_template>")) {
            model_path = model_path.replace("<climate_archive_model_template>", model_name.toLowerCase());
        } else if (model_path.contains("<Climate_Archive_Model_Template>")) {
            model_path = model_path.replace("<Climate_Archive_Model_Template>", model_name);
        }
        // inject field
        if (model_path.contains("<CLIMATE_ARCHIVE_FIELD_TEMPLATE>")) {
            model_path = model_path.replace("<CLIMATE_ARCHIVE_FIELD_TEMPLATE>", field.toUpperCase());
        } else if (model_path.contains("<climate_archive_field_template>")) {
            model_path = model_path.replace("<climate_archive_field_template>", field.toLowerCase());
        } else if (model_path.contains("<Climate_Archive_Field_Template>")) {
            model_path = model_path.replace("<Climate_Archive_Field_Template>", field);
        }
        return model_path;
    }
}

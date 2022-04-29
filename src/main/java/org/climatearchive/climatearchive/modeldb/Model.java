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
        return getModel_path(model_path_template, model_name, field);
    }

    public static String getModel_path(String model_template, String model_name, String field) {
        // inject model name
        if (model_template.contains("<CLIMATE_ARCHIVE_MODEL_TEMPLATE>")) {
            model_template = model_template.replace("<CLIMATE_ARCHIVE_MODEL_TEMPLATE>", model_name.toUpperCase());
        }
        if (model_template.contains("<climate_archive_model_template>")) {
            model_template = model_template.replace("<climate_archive_model_template>", model_name.toLowerCase());
        }
        if (model_template.contains("<Climate_Archive_Model_Template>")) {
            model_template = model_template.replace("<Climate_Archive_Model_Template>", model_name);
        }
        // inject field
        if (model_template.contains("<CLIMATE_ARCHIVE_FIELD_TEMPLATE>")) {
            model_template = model_template.replace("<CLIMATE_ARCHIVE_FIELD_TEMPLATE>", field.toUpperCase());
        }
        if (model_template.contains("<climate_archive_field_template>")) {
            model_template = model_template.replace("<climate_archive_field_template>", field.toLowerCase());
        }
        if (model_template.contains("<Climate_Archive_Field_Template>")) {
            model_template = model_template.replace("<Climate_Archive_Field_Template>", field);
        }
        return model_template;
    }
}

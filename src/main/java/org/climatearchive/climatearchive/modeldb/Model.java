package org.climatearchive.climatearchive.modeldb;

public class Model {
    private final String model_name;
    private final String latitude_value;
    private final String longitude_value;

    public Model(String model_name, String latitude_value, String longitude_value) {
        this.model_name = model_name;
        this.latitude_value = latitude_value;
        this.longitude_value = longitude_value;
    }

    public Model(String model_name) {
        this.model_name = model_name;
        this.latitude_value = "";
        this.longitude_value = "";
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
}

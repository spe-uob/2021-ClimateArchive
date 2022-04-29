package org.climatearchive.climatearchive.datasources;

import java.util.List;
import org.climatearchive.climatearchive.util.Pair;

// a class can be created for each type of .nc file and implement this interface to work with the server
public interface DataSource {
    String getModelName();
    String getModelType();

    // take lists of fields, variables and x y point and location of the data
    // returns string of data fetched info, list of results
    List<Float[]> getClosest2DPointData(List<String> fields, List<String> variables, float lat, float lon, String data_location);
}

package org.climatearchive.climatearchive.datasources;

import org.climatearchive.climatearchive.modeldb.Model;
import org.climatearchive.climatearchive.util.Pair;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GriddedData implements DataSource{

    private final Model model;

    public GriddedData(Model model) {
        this.model = model;
    }

    @Override
    public String getModelName() {
        return model.getModel_name();
    }

    @Override
    public String getModelType() {
        return "gridded";
    }

    @Override
    public Pair<String, List<Float[]>> getClosest2DPointData(List<String> fields, List<String> variables, float lat, float lon, String data_location) {
        List<Float[]> result = new ArrayList<>();
        Float[] errorList = new Float[variables.size()];
        Arrays.fill(errorList, null);
        for (String field : fields) {
            try (NetcdfFile ncfile = NetcdfFiles.open(data_location + "/" + this.model.getModel_path(field))) {
                float[] lats = (float[]) Objects.requireNonNull(ncfile.findVariable(this.model.getLatitude_value())).read().copyTo1DJavaArray();
                float[] lons = (float[]) Objects.requireNonNull(ncfile.findVariable(this.model.getLongitude_value())).read().copyTo1DJavaArray();

                List<Variable> variableList = variables.stream().map(ncfile::findVariable).collect(Collectors.toList());

                Pair<Integer, Integer> lookup = findClosestPoint(lat, lon, lats, lons);

                Float[] fieldValues = new Float[variableList.size()];

                for (int i = 0; i < variableList.size(); i++) {
                    Variable v = variableList.get(i);
                    Float dataPoint = extractVariableData(v, lookup.getFirst(), lookup.getSecond());
                    fieldValues[i] = dataPoint;
                }
                result.add(fieldValues);

            } catch (Exception e) {
                result.add(errorList);
            }
        }
        return new Pair<>("", result);
    }

    // calculates index of the closest point
    private Pair<Integer, Integer> findClosestPoint(float lat, float lon, float[] lats, float[] lons) {
        List<Integer> xs = indexOfClosest2(lat, lats);
        List<Integer> ys = indexOfClosest2(lon, lons);
        double smallestDistance = Double.MAX_VALUE;
        Pair<Integer, Integer> closestPoint = new Pair<>(-1, -1);
        for (Integer x : xs) {
            for (Integer y : ys) {
                double d = distance(lat, lon, lats[x], lons[y]);
                if (smallestDistance > d) {
                    smallestDistance = d;
                    closestPoint = new Pair<>(x, y);
                }
            }
        }
        return closestPoint;
    }

    public List<Integer> indexOfClosest2(float key, float[] sortedArray) {
        int closestIndex = 0;
        float closestDif = Float.MAX_VALUE;
        boolean tooBig = false;
        for (int i = 0; i < sortedArray.length; i++) {
            if (key == sortedArray[i]) return List.of(i);
            float d = sortedArray[i] - key;
            if (Math.abs(d) < closestDif) {
                closestIndex = i;
                closestDif = Math.abs(d);
                tooBig = (d > 0);
            }
        }
        if (tooBig) { // return index i and i - 1
            return List.of(closestIndex, (closestIndex + 1) % sortedArray.length);
        } else { // return index i and i + 1
            return List.of(closestIndex, (closestIndex - 1 + sortedArray.length) % sortedArray.length);
        }
    }

    private Float extractVariableData(Variable variable, int xIndex, int yIndex) {
        try {
            float[][] data = (float[][]) variable.read().reduce().copyToNDJavaArray();
            return data[xIndex][yIndex];
        } catch (Exception e) {
            return null;
        }
    }

    // calculates the closest distance using haversine
    private double distance(float lat1, float  lon1, float  lat2, float  lon2) {
        double p = 0.017453292519943295; // pi / 180
        double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p))/2;
        return Math.asin(Math.sqrt(a)); // not scaled as the smallest distance is the same
    }
}

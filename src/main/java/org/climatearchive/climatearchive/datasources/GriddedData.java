package org.climatearchive.climatearchive.datasources;

import org.climatearchive.climatearchive.modeldb.Model;
import org.climatearchive.climatearchive.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.util.*;
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
    public List<Float[]> getClosest2DPointData(@NotNull List<String> fields, @NotNull List<String> variables, float lat, float lon, String data_location) {
        List<Float[]> result = new ArrayList<>();
        Float[] errorList = new Float[variables.size()];
        Arrays.fill(errorList, null);
        for (String field : fields) {
            try (NetcdfFile ncfile = NetcdfFiles.open(data_location + "/" + this.model.getModel_path(field))) {
                float[] lats = (float[]) Objects.requireNonNull(ncfile.findVariable(this.model.getLatitude_value())).read().copyTo1DJavaArray();
                float[] lons = (float[]) Objects.requireNonNull(ncfile.findVariable(this.model.getLongitude_value())).read().copyTo1DJavaArray();

                List<Variable> variableList = variables.stream().map(ncfile::findVariable).collect(Collectors.toList());

                Pair<Integer, Integer> closestPoint = findClosestPoint(lat, lon, lats, lons);

                Float[] fieldValues = new Float[variableList.size()];

                for (int i = 0; i < variableList.size(); i++) {
                    Variable v = variableList.get(i);
                    Float dataPoint = extractVariableData(v, closestPoint.getFirst(), closestPoint.getSecond());
                    fieldValues[i] = dataPoint;
                }
                result.add(fieldValues);

            } catch (Exception e) {
                result.add(errorList);
            }
        }
        return result;
    }

    // calculates x, y index of the closest point
    // checks 4 corners of square for closest to account for projection
    private Pair<Integer, Integer> findClosestPoint(float lat, float lon, float[] lats, float[] lons) {
        List<Integer> xs = indexOfClosest(lat, lats);
        List<Integer> ys = indexOfClosest(lon, lons);
        List<Pair<Integer, Integer>> corners = new ArrayList<>();
        xs.forEach(x -> ys.forEach(y -> corners.add(new Pair<>(x, y))));
        return Collections.min(corners, Comparator.comparing(point -> distance(lat, lon, point.getFirst(), point.getSecond())));
    }

    // returns the two closest indexes to the point key, or one if sortedArray[index] == key
    public List<Integer> indexOfClosest(float key, float @NotNull [] sortedArray) {
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

    private @Nullable Float extractVariableData(Variable variable, int xIndex, int yIndex) {
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
        return Math.asin(Math.sqrt(a)); // not scaled as the smallest distance is the same either way
    }
}

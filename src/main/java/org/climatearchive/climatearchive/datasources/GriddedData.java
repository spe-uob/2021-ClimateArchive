package org.climatearchive.climatearchive.datasources;

import org.climatearchive.climatearchive.modeldb.Model;
import org.climatearchive.climatearchive.util.Pair;
import org.jetbrains.annotations.NotNull;
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
        for (String field : fields) {
            try (NetcdfFile ncfile = NetcdfFiles.open(data_location + '/' + getModelName() + "/climate/" + getModelName().toLowerCase() + "a.pdcl" + field + ".nc")) {
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
                result.add(null);
            }
        }
        System.out.println(Arrays.toString(result.get(0)));
        return new Pair<>("", result);
    }

    private Pair<Integer, Integer> findClosestPoint(float lat, float lon, float[] lats, float[] lons) {
        int x = indexOfClosest(lat, lats);
        int y = indexOfClosest(lon, lons);
        return new Pair<>(x, y);
    }

    private int indexOfClosest(float target, float @NotNull [] arr) {
        float smallestDiff = Float.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < arr.length; i++) {
            float diff = Math.abs(arr[i] - target);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private Float extractVariableData(Variable variable, int xIndex, int yIndex) {
        try {
            float[][] data = (float[][]) variable.read().reduce().copyToNDJavaArray();
            return data[xIndex][yIndex];
        } catch (Exception e) {
            return null;
        }
    }
}

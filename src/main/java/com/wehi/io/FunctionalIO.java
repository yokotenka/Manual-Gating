package com.wehi.io;

import com.wehi.ManualGatingWindow;
import com.wehi.table.entry.FunctionalMarkerEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Class for saving the functional marker classifiers
 */
public class FunctionalIO extends AbstractIO{
    // Title for this class
    private static final String TITLE = "Classifier Saver";



    private static final String ABOVE = "above";
    private static final String BELOW = "below";
    private static final String NAME = "name";
    private static final String THRESHOLD = "threshold";
    private static final String MARKER = "markerName";
    private static final String MEASUREMENT = "measurementName";
    private static final String FUNCTIONAL_MARKERS = "functionalMarkers";
    private static final String EMPTY = "";



    /**
     * For loading the saved classifiers
     * @param baseDirectory
     * @param fileName
     * @param markers List of markers
     * @param measurements List of measurements
     * @param cells cells in the image
     * @param stage the main stage
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ObservableList<FunctionalMarkerEntry> loadOptions(
            File baseDirectory,
            String fileName,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Collection<PathObject> cells, Stage stage
        ) throws IOException, JSONException {
//
//        fileName = fileName + SIGNATURE + ".json";
//        // location of the file
        ObservableList<FunctionalMarkerEntry> children = FXCollections.observableArrayList();
        File fullFileName = new File(baseDirectory, fileName);

        // Read the content of the file
        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);

        // Get the classifiers
        JSONArray subPhenotypes;

        subPhenotypes = (JSONArray) jsonObject.get(FUNCTIONAL_MARKERS);
        // Iterate through the child phenotypes
        for (int i=0; i < subPhenotypes.length(); i++) {
            JSONObject o = (JSONObject) subPhenotypes.get(i);

            children.add(loadFunctionalMarker(o, markers, measurements, cells, stage));
        }
        return children;
    }

    // Load individual classifiers
    private static FunctionalMarkerEntry loadFunctionalMarker(
            JSONObject jsonObject,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Collection<PathObject> cells,
            Stage stage
        ) throws JSONException {

        String measurement = (String) jsonObject.get(MEASUREMENT);
        double threshold = (Double) jsonObject.get(THRESHOLD);

        String marker = (String) jsonObject.get(MARKER);

        String above = (String) jsonObject.get(ABOVE);

        String below = (String) jsonObject.get(BELOW);


        // Create a new table entry
        FunctionalMarkerEntry newEntry = new FunctionalMarkerEntry(
                cells,
                marker,
                markers,
                measurements,
                stage
                );

        newEntry.setThreshold(threshold);
        newEntry.setMarker(marker);
        newEntry.setMeasurement(measurement);
        newEntry.updateFunctionalChildTable();
        if (!above.equals(EMPTY)){
            newEntry.setAboveThreshold(above);
            newEntry.selectAbove();
        }
        if (!below.equals(EMPTY)){
            newEntry.setBelowThreshold(below);
            newEntry.selectBelow();
        }
        newEntry.createPhenotypes();
        newEntry.plotChart();

        return newEntry;
    }


    public static void saveToExistingPhenotypeFile(
            File baseDirectory,
            String fileName,
            ObservableList<TreeItem<FunctionalMarkerEntry>> list
        ) throws IOException, JSONException {

        File fullFileName = new File(baseDirectory, fileName);
        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);

        ArrayList<JSONObject> functionalMarkers = new ArrayList<>();

        for (TreeItem<FunctionalMarkerEntry> item : list){
            functionalMarkers.add(formatFunctionalMarkers(item.getValue()));
        }
        jsonObject.put(FUNCTIONAL_MARKERS, functionalMarkers);
        writeToJsonFile(jsonObject, fullFileName);
    }

    public static JSONObject formatFunctionalMarkers(FunctionalMarkerEntry entry) throws JSONException {
        JSONObject json = new JSONObject();

        json.put(NAME, entry.getName());
        json.put(MEASUREMENT, entry.getMeasurement());
        json.put(MARKER, entry.getMarker());

        json.put(ABOVE, entry.getAboveThreshold());
        json.put(BELOW, entry.getBelowThreshold());

        json.put(THRESHOLD, entry.getThreshold());
        return json;
    }

    private static void writeToJsonFile(JSONObject jsonObject, File fullFileName){
        FileWriter file;
        try {
            file = new FileWriter(fullFileName);
            file.write(jsonObject.toString());
            file.flush();
            file.close();
            Dialogs.showInfoNotification(ManualGatingWindow.TITLE, "Saved options to:\n"+fullFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


package com.wehi;

import com.wehi.save.ClassifierWrapper;
import com.wehi.save.ProjectClassifierWrapper;
import com.wehi.table.entry.FunctionalMarkerEntry;
import com.wehi.table.wrapper.FunctionalMarkerTreeTableWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.classifiers.object.ObjectClassifiers;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.projects.Project;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;


/**
 * Class for saving the functional marker classifiers
 */
public class ClassifierSaver {
    // Title for this class
    private static final String title = "Classifier Saver";

    // Signature for classifiers which were created in this interface
    public static final String SIGNATURE = "hn7y2lF77x";

    /**
     * Save the classifier configured in the user interface
     * @param qupath The instance of qupath
     * @param classifierName The name of the classifier
     * @param list The list of the information for each functional marker and threshold
     */
    public static void saveClassifiers(QuPathGUI qupath, String classifierName, ObservableList<TreeItem<FunctionalMarkerEntry>> list) {
        // Get project
        var project = qupath.getProject();
        // Check if project is null
        if (project == null) {
            Dialogs.showErrorMessage(title, "You need a project to save this classifier!");
            return;
        }
        // Get the classifier name
        String name = GeneralTools.stripInvalidFilenameChars(classifierName);
        if (name.isBlank()) {
            Dialogs.showErrorMessage(title, "Please enter a name for the classifier!");
            return;
        }
        try {
            ObservableList<ClassifierWrapper<BufferedImage>> array = FXCollections.observableArrayList();
            for (TreeItem<FunctionalMarkerEntry> marker : list) {
                String markerClassifierName = makeClassifierName(name, marker.getValue().getMarker());

                var classifier = updateClassifier(marker.getValue());
                project.getObjectClassifiers().put(markerClassifierName, classifier);

                var wrap = wrapClassifier(qupath, markerClassifierName);
                if (!array.contains(wrap)) {
                    array.add(wrap);
                }
            }
            tryToSave(project, array, name);
        } catch (Exception e) {
            Dialogs.showErrorNotification(title, e);
        }
    }

    // Makes the classifier name for individual markers
    private static String makeClassifierName(String classifierName, String markerName) {
        return classifierName+ "_" + markerName;
    }

    // Initialises the classifier which will be saved
    private static ObjectClassifier<BufferedImage> updateClassifier(FunctionalMarkerEntry marker) {
        String measurement = marker.getFullMeasurementName();
        double threshold = marker.getThreshold();
        PathClass classAbove = PathClassFactory.getPathClass(marker.getAboveThreshold());
        PathClass classBelow = PathClassFactory.getPathClass(marker.getBelowThreshold());

        if (measurement == null || Double.isNaN(threshold))
            return null;

        return new ObjectClassifiers.ClassifyByMeasurementBuilder<BufferedImage>(measurement)
                .threshold(threshold)
                .above(classAbove)
                .equalTo(classAbove)
                .below(classBelow)
                .build();
    }

    // Wraps classifier in the ProjectClassifierWrapper so it can be saved via the QuPath API
    private static ProjectClassifierWrapper<BufferedImage> wrapClassifier(QuPathGUI qupath, String classifierName) {
        return new ProjectClassifierWrapper<>(qupath.getProject(), classifierName);
    }


    /*
     ******************** Code taken from Pete Bankhead's "CreateCompositeClassifier.java" *****************************
     * */

    // Builds the composite classifier
    private static ObjectClassifier<BufferedImage> tryToBuild(Collection<ClassifierWrapper<BufferedImage>> wrappers) throws
            IOException {
        var classifiers = new LinkedHashSet<ObjectClassifier<BufferedImage>>();
        for (var wrapper : wrappers) {
            classifiers.add(wrapper.getClassifier());
        }
        return ObjectClassifiers.createCompositeClassifier(classifiers);
    }

    // Prompts the user to save
    private static void tryToSave(Project<BufferedImage> project,
                                  Collection<ClassifierWrapper<BufferedImage>> wrappers,
                                  String name) {
        try {
            var composite = tryToBuild(wrappers);

            name = name == null ? null : GeneralTools.stripInvalidFilenameChars(name);
            name = name + ClassifierSaver.SIGNATURE;
            if (project != null && !name.isBlank()) {
                if (project.getObjectClassifiers().contains(name)) {
                    if (!Dialogs.showConfirmDialog(title, "Overwrite existing classifier called '" + name.substring(0, name.length()-1)))
                    {
                        return;
                    }
                }
                project.getObjectClassifiers().put(name, composite);
                Dialogs.showInfoNotification(title, "Classifier written to project as " + name.substring(0, name.length()-1));
            } else {
                var file = Dialogs.promptToSaveFile(title, null, name, "JSON", ".json");
                if (file != null) {
                    ObjectClassifiers.writeClassifier(composite, file.toPath());
                    Dialogs.showInfoNotification(title, "Classifier written to " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Dialogs.showErrorMessage(title, e);
        }

    }


    /**
     * For loading the saved classifiers
     * @param baseDirectory
     * @param fileName
     * @param markers List of markers
     * @param measurements List of measurements
     * @param cells cells in the image
     * @param stage the main stage
     * @param table the table with the list
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static FunctionalMarkerTreeTableWrapper loadOptions(
            File baseDirectory,
            String fileName,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Collection<PathObject> cells, Stage stage,
            FunctionalMarkerTreeTableWrapper table
        ) throws IOException, JSONException {

        fileName = fileName + SIGNATURE + ".json";
        // location of the file
        File fullFileName = new File(baseDirectory, fileName);

        // Read the content of the file
        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);

        // Get the classifiers
        JSONArray subPhenotypes = (JSONArray) jsonObject.get("classifiers");

        // Iterate through the child phenotypes
        for (int i=0; i < subPhenotypes.length(); i++){
            JSONObject o = (JSONObject) subPhenotypes.get(i);

            table.add(loadFunctionalMarker((JSONObject) o.get("function"), markers, measurements, cells, stage));
        }
        return table;
    }

    // Load individual classifiers
    private static FunctionalMarkerEntry loadFunctionalMarker(
            JSONObject jsonObject,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Collection<PathObject> cells,
            Stage stage
        ) throws JSONException {

        String fullMeasurementName = (String) jsonObject.get("measurement");
        double threshold = (Double) jsonObject.get("threshold");

        String marker = fullMeasurementName.split(": ")[0];
        String measurement = fullMeasurementName.split(": ")[1] + ": "+fullMeasurementName.split(": ")[2];

        JSONObject aboveJson = (JSONObject) jsonObject.get("pathClassAbove");
        String above = (String) aboveJson.get("name");

        JSONObject belowJson = (JSONObject) jsonObject.get("pathClassBelow");
        String below = (String) belowJson.get("name");

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
        if (!above.equals("")){
            newEntry.setAboveThreshold(above);
            newEntry.selectAbove();
        }
        if (!below.equals("")){
            newEntry.setBelowThreshold(below);
            newEntry.selectBelow();
        }
        newEntry.createPhenotypes();
        newEntry.plotChart();

        return newEntry;
    }
}


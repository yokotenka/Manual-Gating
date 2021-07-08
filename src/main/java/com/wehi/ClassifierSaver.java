package com.wehi;

import com.wehi.save.ClassifierWrapper;
import com.wehi.save.ProjectClassifierWrapper;
import com.wehi.table.entry.FunctionalMarkerEntry;
import com.wehi.table.wrapper.FunctionalPhenotypeListTableWrapper;
import com.wehi.table.wrapper.FunctionalPhenotypeOptionTableWrapper;
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
import java.util.function.Function;

public class ClassifierSaver {

    private static final String title = "Classifier Saver";

    // Save the classifiers
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
            if (list.size() > 1) {
                tryToSave(project, array, name);
            }
        } catch (Exception e) {
            Dialogs.showErrorNotification(title, e);
            return;
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
    private static ObjectClassifier<BufferedImage> tryToBuild(Collection<ClassifierWrapper<BufferedImage>> wrappers) throws
            IOException {
        var classifiers = new LinkedHashSet<ObjectClassifier<BufferedImage>>();
        for (var wrapper : wrappers) {
            classifiers.add(wrapper.getClassifier());
        }
        if (classifiers.size() < 2) {
            Dialogs.showErrorMessage(title,
                    "At least two different classifiers must be selected to create a composite!");
            return null;
        }
        return ObjectClassifiers.createCompositeClassifier(classifiers);
    }

    private static void tryToSave(Project<BufferedImage> project,
                                  Collection<ClassifierWrapper<BufferedImage>> wrappers,
                                  String name) {
        try {
            var composite = tryToBuild(wrappers);
            if (composite == null)
                return;

            name = name == null ? null : GeneralTools.stripInvalidFilenameChars(name);
            name = name + "*";
            if (project != null && name != null && !name.isBlank()) {
                if (project.getObjectClassifiers().contains(name)) {
                    if (!Dialogs.showConfirmDialog(title, "Overwrite existing classifier called '" + name + "'?"))
                        return;
                }
                project.getObjectClassifiers().put(name, composite);
                Dialogs.showInfoNotification(title, "Classifier written to project as " + name);
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


    public static FunctionalPhenotypeListTableWrapper loadOptions(File baseDirectory,
                                                                  String fileName,
                                                                  ObservableList<String> markers,
                                                                  ObservableList<String> measurements,
                                                                  Collection<PathObject> cells, Stage stage,
                                                                  FunctionalPhenotypeListTableWrapper table
                                                                  ) throws IOException, JSONException {

        File fullFileName = new File(baseDirectory, fileName);



        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);

//        Dialogs.showInfoNotification(title, content);



        JSONArray subPhenotypes = (JSONArray) jsonObject.get("classifiers");

        for (int i=0; i < subPhenotypes.length(); i++){
            JSONObject o = (JSONObject) subPhenotypes.get(i);

            table.add(loadFunctionalMarker((JSONObject) o.get("function"), markers, measurements, cells, stage));
        }
        return table;
    }

    public static FunctionalMarkerEntry loadFunctionalMarker(JSONObject jsonObject,
                                                      ObservableList<String> markers,
                                                      ObservableList<String> measurements,
                                                      Collection<PathObject> cells,
                                                      Stage stage) throws JSONException {



        String fullMeasurementName = (String) jsonObject.get("measurement");
        double threshold = (Double) jsonObject.get("threshold");

        String marker = fullMeasurementName.split(": ")[0];
        String measurement = fullMeasurementName.split(": ")[1] + ": "+fullMeasurementName.split(": ")[2];

        JSONObject aboveJson = (JSONObject) jsonObject.get("pathClassAbove");
        String above = (String) aboveJson.get("name");

        JSONObject belowJson = (JSONObject) jsonObject.get("pathClassBelow");
        String below = (String) belowJson.get("name");



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


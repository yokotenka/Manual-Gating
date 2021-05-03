package com.wehi;

import com.wehi.TableViewHelpers.PhenotypeCreationTableEntry;
import com.wehi.TableViewHelpers.PhenotypeEntry;
import com.wehi.TableViewHelpers.TreeTableCreator;
import groovyjarjarantlr4.runtime.tree.Tree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class JSONTreeSaver {
    public static final String FOLDER = "PhenotypeHierarchy";

    public static void writeTreeToJSON(TreeTableView<PhenotypeEntry> treeTable, File baseDirectory, String fileName) throws JSONException {
        TreeItem<PhenotypeEntry> root = treeTable.getRoot();
        JSONObject jsonObject = toJSON(root);

        File hierarchyFolder = new File(baseDirectory, FOLDER);
        if (!hierarchyFolder.exists()){
            hierarchyFolder.mkdir();
        }
        writeToJsonFile(jsonObject, new File(hierarchyFolder, fileName+".json"));

    }

    private static JSONObject toJSON(TreeItem<PhenotypeEntry> node) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("phenotypeName", node.getValue().getPhenotypeName());


        if (node.getValue().getXAxisMarkerName() == null || node.getValue().getYAxisMarkerName()==null ||
                node.getValue().getXAxisMeasurementName()==null || node.getValue().getYAxisMeasurementName() == null
        ) {
            json.put("xAxisMarker", "");
            json.put("yAxisMarker",  "");
            json.put("xAxisMeasurementName",  "");
            json.put("yAxisMeasurementName",  "");
        }
        else {
            json.put("xAxisMarker", node.getValue().getXAxisMarkerName());
            json.put("yAxisMarker", node.getValue().getYAxisMarkerName());
            json.put("xAxisMeasurementName", node.getValue().getXAxisMeasurementName());
            json.put("yAxisMeasurementName", node.getValue().getYAxisMeasurementName());
        }


        json.put("xAxisThreshold", String.valueOf(node.getValue().getXAxisThreshold()));
        json.put("yAxisThreshold", String.valueOf(node.getValue().getYAxisThreshold()));
        json.put("positiveMarkers",  node.getValue().getPositiveMarkers());
        json.put("negativeMarkers",  node.getValue().getNegativeMarkers());
        json.put("splitMarkerOne", node.getValue().getSplitMarkerOne());
        json.put("splitMarkerTwo", node.getValue().getSplitMarkerTwo());

        if (node.getValue().getCombination() == PhenotypeCreationTableEntry.MARKER_COMBINATION.ONE_OF_EACH){
            json.put("combination", "2");
        } else if(node.getValue().getCombination() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_NEGATIVE){
            json.put("combination", "1");
        }
        else if(node.getValue().getCombination() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
            json.put("combination", "0");
        }

        List<JSONObject> children = new ArrayList<>();
        for(TreeItem<PhenotypeEntry> subNode : node.getChildren()) {
            if(node.getChildren().contains(subNode)) {
                children.add(toJSON(subNode));
            }
        }
        json.put("subPhenotypes", children);
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

    public static TreeItem<PhenotypeEntry> readLoadOptions(File baseDirectory, String fileName, ObservableList<String> markers, ObservableList<String> measurements, Collection<PathObject> cells, Stage stage) throws IOException, JSONException {
        File fullFileName = new File(baseDirectory, fileName);
        String content = Files.readString(Path.of(fullFileName.getPath()));


        JSONObject jsonObject = new JSONObject(content);
        return createTree(jsonObject, markers, measurements, cells, stage);

    }

    private static TreeItem<PhenotypeEntry> createTree(JSONObject jsonObject,
                                                       ObservableList<String> markers, ObservableList<String> measurements, Collection<PathObject> cells, Stage stage) throws JSONException {
        TreeItem<PhenotypeEntry> node = createNewItem(jsonObject, markers, measurements, cells, stage);

        if (node == null){
            return null;
        }
        ObservableList<TreeItem<PhenotypeEntry>> subPhenotypesList = FXCollections.observableArrayList();

        JSONArray subPhenotypes = (JSONArray) jsonObject.get("subPhenotypes");
        for (int i = 0; i < subPhenotypes.length(); i++) {

            TreeItem<PhenotypeEntry> child = createTree((JSONObject) subPhenotypes.get(i), markers, measurements, cells, stage);
            if (child != null) {
                subPhenotypesList.add(child);
            }
        }
        node.getChildren().setAll(subPhenotypesList);
        node.getValue().updateNames(subPhenotypesList.stream().map(TreeItem::getValue).collect(Collectors.toCollection(ArrayList::new)));
        return node;
    }

    private static TreeItem<PhenotypeEntry> createNewItem(JSONObject jsonObject,
                                                          ObservableList<String> markers, ObservableList<String> measurements, Collection<PathObject> cells, Stage stage) throws JSONException {
        String phenotypeName = (String) jsonObject.get("phenotypeName");

        String markerOne = getEqualMarker(markers, (String) jsonObject.get("splitMarkerOne"));
        String markerTwo = getEqualMarker(markers, (String) jsonObject.get("splitMarkerTwo"));
        ArrayList<String> positiveMarkers = convertJSONArrayToArrayList((JSONArray) jsonObject.get("positiveMarkers"), markers);
        ArrayList<String> negativeMarkers = convertJSONArrayToArrayList((JSONArray) jsonObject.get("negativeMarkers"), markers);

//
//
        String yAxisMarkerName = getEqualMarker(markers,(String) jsonObject.get("yAxisMarker"));
        String xAxisMarkerName = getEqualMarker(markers,(String) jsonObject.get("xAxisMarker"));

        String yAxisMeasurementName = (String) jsonObject.get("yAxisMeasurementName");
        String xAxisMeasurementName = (String) jsonObject.get("xAxisMeasurementName");


        double unLoggedYThreshold= Double.parseDouble((String)jsonObject.get("yAxisThreshold"));
        double unLoggedXThreshold =  Double.parseDouble((String)jsonObject.get("xAxisThreshold"));

        int combination = Integer.parseInt( (String) jsonObject.get("combination"));
        PhenotypeCreationTableEntry.MARKER_COMBINATION actual;
        if (combination == 0){
            actual = PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_POSITIVE;
        } else if (combination == 1){
            actual = PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_NEGATIVE;
        } else {
            actual = PhenotypeCreationTableEntry.MARKER_COMBINATION.ONE_OF_EACH;
        }

        if (markerOne==null || markerTwo==null || positiveMarkers ==null || negativeMarkers == null){

            return null;
        }
        PhenotypeEntry phenotypeEntry = new PhenotypeEntry(
                cells,
                phenotypeName,
                positiveMarkers,
                negativeMarkers,
                markers,
                measurements,
                stage,
                markerOne,
                markerTwo,
                actual
        );
//
        phenotypeEntry.setXAxisMarkerName(xAxisMarkerName);
        phenotypeEntry.setXAxisMeasurementName(xAxisMeasurementName);


        phenotypeEntry.setYAxisMarkerName(yAxisMarkerName);
        phenotypeEntry.setYAxisMeasurementName(yAxisMeasurementName);

        phenotypeEntry.updatePhenotypeCreationCreator();

        phenotypeEntry.setYAxisThreshold(unLoggedYThreshold);
        phenotypeEntry.setXAxisThreshold(unLoggedXThreshold);
        phenotypeEntry.setFullMeasurementName();

        return new TreeItem<>(phenotypeEntry);
    }

    private static ArrayList<String> convertJSONArrayToArrayList(JSONArray jArray, ObservableList<String> markers) throws JSONException {
        ArrayList<String> arrayList = new ArrayList<>();
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                String temp = getEqualMarker(markers, jArray.getString(i));
                if (temp == null){
                    return null;
                }
                arrayList.add(temp);
            }
        }
        return arrayList;
    }

    private static String getEqualMarker(ObservableList<String> markers, String markerToBeMatched){
        if (markerToBeMatched.equals("")){
            return "";
        }

        for (String marker : markers){
            if (checkEqualMarker(marker, markerToBeMatched)){
                return marker;
            }
        }
        return null;
    }

    private static boolean checkEqualMarker(String marker1, String marker2){
        String temp1 = marker1.replaceAll("[-+^]*", "").toLowerCase();
        String temp2 = marker2.replaceAll("[-+^]*", "").toLowerCase();
        return temp1.equals(temp2);
    }
}

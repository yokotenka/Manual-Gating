package com.wehi.io;

import com.wehi.ManualGatingWindow;
import com.wehi.table.entry.ChildPhenotypeTableEntry;
import com.wehi.table.entry.PhenotypeEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.scene.control.TreeTableView;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GatingIO extends AbstractIO{



    //************************************** Save ************************************** //
    /**
     * Method to write the phenotype hierarchy to a json file
     * @param treeTable phenotype hierarchy
     * @param baseDirectory the folder which file is saved to
     * @param fileName file name
     * @throws JSONException might throw cheeky error
     */
    public static void writeTreeToJSON(TreeTableView<PhenotypeEntry> treeTable, File baseDirectory, String fileName) throws JSONException {
        TreeItem<PhenotypeEntry> root = treeTable.getRoot();
        JSONObject jsonObject = toJSON(root);

        File hierarchyFolder = new File(baseDirectory, FOLDER);
        if (!hierarchyFolder.exists()){
            hierarchyFolder.mkdir();
        }

        // This is for the functional marker thresholds which at this stage is still nothing
        jsonObject.put("FunctionalMarkers", "");
        writeToJsonFile(jsonObject, new File(hierarchyFolder, fileName+".json"));

    }

    /**
     * This will convert a tree item to a json object
     * @param node the tree item node
     * @return jsonObject
     * @throws JSONException might throw a cheeky error
     */
    public static JSONObject toJSON(TreeItem<PhenotypeEntry> node) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("phenotypeName", node.getValue().getName());


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

        if (node.getValue().getCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH){
            json.put("combination", "2");
        } else if(node.getValue().getCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE){
            json.put("combination", "1");
        }
        else if(node.getValue().getCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
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

    /**
     * Actually writes the file after it has been formatted
     * @param jsonObject the json to be written
     * @param fullFileName the name of the file to be saved
     */
    public static void writeToJsonFile(JSONObject jsonObject, File fullFileName){
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


    //************************************** Read ************************************** //

    /**
     * For loading in previously saved files
     * @param baseDirectory the base directory
     * @param fileName filename
     * @param markers markers which are in the image
     * @param measurements the measurements in the image
     * @param cells cells in the image
     * @param stage the main stage of the plugin
     * @return the root of the hierarchy
     * @throws IOException cheeky IO exception
     * @throws JSONException cheeky Json exception
     */
    public static PhenotypeEntry readLoadOptions(File baseDirectory,
                                                           String fileName,
                                                           ObservableList<String> markers,
                                                           ObservableList<String> measurements,
                                                           Collection<PathObject> cells, Stage stage) throws IOException, JSONException {

        File fullFileName = new File(baseDirectory, fileName);
        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);




        PhenotypeEntry root = createRoot(jsonObject, markers, measurements, cells, stage);

        PhenotypeEntry.updatePhenotypeTree(root);
        PhenotypeEntry.plotAllChartPhenotypeTree(root);
        return root;
    }

    /**
     * Creates the root of the tree
     * @param jsonObject json object
     * @param markers markers needed
     * @param measurements measurements needed
     * @param cells cells of the image
     * @param stage stage of the plugin
     * @return the root phenotype
     * @throws JSONException cheeky exception
     */
    public static PhenotypeEntry createRoot(JSONObject jsonObject,
                                                       ObservableList<String> markers,
                                                       ObservableList<String> measurements,
                                                       Collection<PathObject> cells,
                                                       Stage stage) throws JSONException {

        // Create the root phenotype
        PhenotypeEntry root = createNewItem(jsonObject, markers, measurements, cells, stage, true);
        if (root == null){
            return null;
        }
        constructTree(jsonObject, root, markers, measurements, cells, stage);
        return root;
    }

    /**
     * For construction of the rest of the tree
     * @param jsonObject json of parent
     * @param parentNode parent node
     * @param markers the markers
     * @param measurements the measurements
     * @param cells the cells
     * @param stage the stage of the plugin
     * @throws JSONException the cheeky exception
     */
    public static void constructTree(JSONObject jsonObject, PhenotypeEntry parentNode, ObservableList<String> markers,
                                      ObservableList<String> measurements,
                                      Collection<PathObject> cells,
                                      Stage stage) throws JSONException {

        JSONArray subPhenotypes = (JSONArray) jsonObject.get("subPhenotypes");

        for (int i = 0; i < subPhenotypes.length(); i++) {
            PhenotypeEntry child = createNewItem(
                    (JSONObject) subPhenotypes.get(i),
                    markers,
                    measurements,
                    cells,
                    stage,
                    false
            );
            if (child != null) {
                parentNode.addChild(child);
                constructTree((JSONObject) subPhenotypes.get(i), child, markers, measurements, cells, stage);
            }
        }
    }

    /**
     * Creates a new PhenotypeEntry
     * @param jsonObject the json Object to be read
     * @param markers the markers
     * @param measurements the measurements
     * @param cells the cells
     * @param stage the stage of the plugin
     * @param createTreeItem a boolean
     * @return new PhenotypeEntry
     * @throws JSONException cheeky json error
     */
    public static PhenotypeEntry createNewItem(
                                                JSONObject jsonObject,
                                                ObservableList<String> markers,
                                                ObservableList<String> measurements,
                                                Collection<PathObject> cells,
                                                Stage stage,
                                                boolean createTreeItem
                                            ) throws JSONException {

        String phenotypeName = (String) jsonObject.get("phenotypeName");

        String markerOne = getEqualMarker(markers, (String) jsonObject.get("splitMarkerOne"));
        String markerTwo = getEqualMarker(markers, (String) jsonObject.get("splitMarkerTwo"));
        ArrayList<String> positiveMarkers = convertJSONArrayToArrayList((JSONArray) jsonObject.get("positiveMarkers"), markers);
        ArrayList<String> negativeMarkers = convertJSONArrayToArrayList((JSONArray) jsonObject.get("negativeMarkers"), markers);

        String yAxisMarkerName = getEqualMarker(markers,(String) jsonObject.get("yAxisMarker"));
        String xAxisMarkerName = getEqualMarker(markers,(String) jsonObject.get("xAxisMarker"));

        String yAxisMeasurementName = (String) jsonObject.get("yAxisMeasurementName");
        String xAxisMeasurementName = (String) jsonObject.get("xAxisMeasurementName");


        double unLoggedYThreshold= Double.parseDouble((String)jsonObject.get("yAxisThreshold"));
        double unLoggedXThreshold =  Double.parseDouble((String)jsonObject.get("xAxisThreshold"));

        int combination = Integer.parseInt( (String) jsonObject.get("combination"));
        ChildPhenotypeTableEntry.MARKER_COMBINATION actual;
        if (combination == 0){
            actual = ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE;
        } else if (combination == 1){
            actual = ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE;
        } else {
            actual = ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH;
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
                actual,
                createTreeItem
        );

        phenotypeEntry.setXAxisMarkerName(xAxisMarkerName);
        phenotypeEntry.setXAxisMeasurementName(xAxisMeasurementName);

        phenotypeEntry.setYAxisMarkerName(yAxisMarkerName);
        phenotypeEntry.setYAxisMeasurementName(yAxisMeasurementName);

        phenotypeEntry.refreshChildPhenotypeTable();
        phenotypeEntry.setYAxisThreshold(unLoggedYThreshold);
        phenotypeEntry.setXAxisThreshold(unLoggedXThreshold);

        return phenotypeEntry;
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

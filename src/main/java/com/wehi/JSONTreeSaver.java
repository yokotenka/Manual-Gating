package com.wehi;

import com.wehi.TableViewHelpers.PhenotypeEntry;
import javafx.scene.control.TreeItem;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import qupath.lib.gui.dialogs.Dialogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        json.put("xAxisMarker", node.getValue().getXAxisMarkerName());
        json.put("yAxisMarker",  node.getValue().getYAxisMarkerName());
        json.put("xAxisMeasurementName",  node.getValue().getXAxisMeasurementName());
        json.put("yAxisMeasurementName",  node.getValue().getYAxisMeasurementName());
        json.put("xAxisThreshold",  node.getValue().getXAxisThreshold());
        json.put("yAxisThreshold",  node.getValue().getYAxisThreshold());
        json.put("positiveMarkers",  node.getValue().getPositiveMarkers());
        json.put("negativeMarkers",  node.getValue().getNegativeMarkers());

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
        FileWriter file = null;
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

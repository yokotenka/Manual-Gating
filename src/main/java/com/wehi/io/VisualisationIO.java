package com.wehi.io;

import com.wehi.ManualGatingWindow;
import com.wehi.table.entry.ActivityCellTypeEntry;
import com.wehi.table.entry.IVisualisable;
import com.wehi.table.entry.PhenotypeEntry;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class VisualisationIO extends AbstractIO {

    private static final String COLOUR = "Colour";
    private static final String SHOW = "Show";
    public static final String FOLDER = "VisualisationOptions";
    private static final String TITLE = "Visualisation IO";
    private static final String ACTIVITIES = "Activities";
    private static final String ACTIVITY_LIST = "Activity_List";

    public static void save(PhenotypeEntry root, File baseDir, String fileName) throws JSONException {
        JSONObject json = new JSONObject();
        savePhenotypeEntry(root, json);

        File hierarchyFolder = new File(baseDir, FOLDER);
        if (!hierarchyFolder.exists()){
            hierarchyFolder.mkdir();
        }
        writeToJsonFile(json,  new File(hierarchyFolder, fileName+".json"));
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

    public static void savePhenotypeEntry(PhenotypeEntry entry, JSONObject json) throws JSONException {

        JSONObject obj = new JSONObject();


        JSONArray arr = new JSONArray();
        for (ActivityCellTypeEntry activity : entry.getActivityCellTypeTableWrapper().getItems()){
            arr.put(saveActivities(activity));
        }
        obj.put(COLOUR, entry.getColor());
        obj.put(SHOW, entry.getShow().isSelected());
        obj.put(ACTIVITY_LIST, arr);
        json.put(entry.getName(), obj);

        for (PhenotypeEntry child : entry.getChildren()){
            savePhenotypeEntry(child, json);
        }

    }

    public static JSONObject saveActivities (ActivityCellTypeEntry entry) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(SHOW, entry.getShow().isSelected());
        data.put(COLOUR, entry.getColor());

        data.put(ACTIVITIES, entry.getActivities());

        return data;
    }



    public static void load(File baseDir, String fileName, PhenotypeEntry root) throws IOException, JSONException {

        File fullFolderName = new File(baseDir, FOLDER);
        File fullFileName = new File(fullFolderName, fileName);
        String content = Files.readString(Path.of(fullFileName.getPath()));
        JSONObject jsonObject = new JSONObject(content);

        readPhenotypeEntryOptions(root, jsonObject);

    }

    public static void readPhenotypeEntryOptions(PhenotypeEntry entry, JSONObject json){
        try {
            JSONObject data = (JSONObject) json.get(entry.getName());

//
            JSONArray activityList = (JSONArray) data.get(ACTIVITY_LIST);
            for (int i=0; i < activityList.length(); i++){
                createActivity((JSONObject) activityList.get(i), entry);
            }
            entry.setShow((boolean) data.get(SHOW));
            entry.setColor(Color.valueOf((String) data.get(COLOUR)));
            entry.setColorDownTree(Color.valueOf((String) data.get(COLOUR)));



        } catch (JSONException e) {
            Dialogs.showErrorMessage(TITLE, "Could not find " + entry.getName());
            return;
        }

        for (PhenotypeEntry child : entry.getChildren()){
            readPhenotypeEntryOptions(child, json);
        }

    }

    public static void createActivity(JSONObject json, PhenotypeEntry entry) throws JSONException {
        ArrayList<String> activities = convertJSONArrayToArrayList((JSONArray) json.get(ACTIVITIES));
        ActivityCellTypeEntry activity = new ActivityCellTypeEntry(entry, activities);

        activity.setShow((boolean) json.get(SHOW));
        activity.setColor(Color.valueOf((String) json.get(COLOUR)));
        activity.setColorDownTree(Color.valueOf((String) json.get(COLOUR)));

        entry.addActivity(activity);
    }

    private static ArrayList<String> convertJSONArrayToArrayList(JSONArray jArray) throws JSONException {
        ArrayList<String> arrayList = new ArrayList<>();
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                arrayList.add((String) jArray.get(i));
            }
        }
        return arrayList;
    }


}

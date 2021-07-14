package com.wehi.table.entry;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;

public class ActivityCellTypeEntry implements IVisualisable {
    private String name;

    private Button showButton;
    private Button hideButton;
    private ColorPicker colorPicker;

    private TreeItem<ActivityCellTypeEntry> treeItem;

    private Collection<PathObject> cells;

    private ArrayList<String> activities;

    public ActivityCellTypeEntry(Collection<PathObject> cells, String parentName, ArrayList<String> activities){
        this.name = parentName + ": " + activities.toString();
        this.cells = cells;
        this.activities = activities;

        colorPicker = new ColorPicker();
        showButton = new Button(">");
        hideButton = new Button("<");
        showButton.setAlignment(Pos.CENTER);
        hideButton.setAlignment(Pos.CENTER);

    }

    @Override
    public String getName() {
        return name;
    }

//    @Override
//    public boolean isShown(){
//        return showBox.isSelected();
//    }

    @Override
    public Button getShowButton(){
        return showButton;
    }

    @Override
    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    @Override
    public Color getColor(){
        return colorPicker.getValue();
    }

    @Override
    public void setColor(Color color){
        colorPicker.setValue(color);
    }

    @Override
    public Button getHideButton(){
        return hideButton;
    }
}

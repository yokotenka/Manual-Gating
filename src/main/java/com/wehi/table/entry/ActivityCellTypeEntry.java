package com.wehi.table.entry;

import com.wehi.pathclasshandler.PathClassHandler;
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

    private PhenotypeEntry parent;

    private CheckBox showBox;
//    private Button showButton;
//    private Button hideButton;
    private ColorPicker colorPicker;

    private TreeItem<ActivityCellTypeEntry> treeItem;

    private Collection<PathObject> cells;

    private ArrayList<String> activities;

    private ArrayList<String> labels;

    public ActivityCellTypeEntry(PhenotypeEntry parent, ArrayList<String> activities){
        this.parent = parent;
        this.name = parent.getName() + " (" + activities.toString().substring(1,activities.toString().length()-1 )+")";
        this.cells = parent.getCells();
        this.activities = activities;

        colorPicker = new ColorPicker();
        labels = new ArrayList<String> ();
        labels.addAll(activities);
        labels.add(parent.getName());
        showBox = new CheckBox();

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
    public CheckBox getShow(){
        return showBox;
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
    public void applyColor() {

    }

    public void show(){
        PathClassHandler.setPathClassVisibility(cells, true, labels);
    }

    public void hideButShowUpTree(){
        PathClassHandler.setPathClassVisibility(cells, false, labels);
        parent.getTreeItem().getParent().getValue().hideButShowUpTree();
    }

    public void setColorDownTree(Color color){
        if (!showBox.isSelected()) {
            PathClassHandler.setColor(cells, color, labels);
        } else{
            PathClassHandler.setColor(cells, colorPicker.getValue(), labels);
        }
    }
    //
//    @Override
//    public Button getHideButton(){
//        return hideButton;
//    }

    public ArrayList<String> getActivities(){
        return activities;
    }
}

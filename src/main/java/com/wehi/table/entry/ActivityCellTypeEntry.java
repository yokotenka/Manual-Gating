package com.wehi.table.entry;

import com.wehi.pathclasshandler.PathClassHandler;
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
    public void setShow(boolean show){
        showBox.setSelected(show);
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

    public void hide(){
//        PathClassHandler.setPathClassVisibility(cells, false, labels);
        if (parent != null)
            parent.hide();
    }

    public void setColorDownTree(Color color){
        parent.setColorDownTree(color);
    }
    //
//    @Override
//    public Button getHideButton(){
//        return hideButton;
//    }

    public ArrayList<String> getActivities(){
        return activities;
    }

    public void setColor(){
        if (showBox.isSelected())
            PathClassHandler.setColor(cells, colorPicker.getValue(), labels);
    }
}

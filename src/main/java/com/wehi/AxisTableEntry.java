package com.wehi;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class AxisTableEntry {
    private String axisLabelName;
    private ComboBox<String> markerBox;
    private ComboBox<String> measurementBox;

    public AxisTableEntry(String axisLabelName, ObservableList<String> markerList, ObservableList<String> measurementList){
        this.axisLabelName = axisLabelName;
        this.markerBox = new ComboBox(markerList);
        this.measurementBox = new ComboBox(measurementList);
    }


    public String getAxisLabelName() {
        return axisLabelName;
    }

    public ComboBox<String> getMarkerBox() {
        return markerBox;
    }

    public ComboBox<String> getMeasurementBox() {
        return measurementBox;
    }
}


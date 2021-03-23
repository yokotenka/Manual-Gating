package com.wehi.TableTreeViewHelpers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class AxisTableEntry {

    // Axis the marker is on
    private AxisValue axisValue;

    // Markers selection box
    private ComboBox<String> markersBox = new ComboBox();
    // Measurements selection box
    private ComboBox<String> measurementsBox = new ComboBox();

    // Threshold value
    private Double threshold = (double) 0;

    public AxisTableEntry(AxisValue axisValue, ObservableList<String> markers, ObservableList<String> measurements){
        this.axisValue = axisValue;
        this.markersBox.setItems(markers);
        this.measurementsBox.setItems(measurements);
    }

    public void setMeasurementName(ObservableList<String> measurements) {
        this.measurementsBox.setItems(measurements);
    }

    public void setMarkerName(ObservableList<String> markers){
        this.markersBox.setItems(markers);
    }

    public void setThreshold(Double threshold){
        this.threshold = threshold;
    }

    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    public ComboBox<String> getMarkersBox() {
        return markersBox;
    }

    public ComboBox<String> getMeasurementsBox() {
        return measurementsBox;
    }

    public String getMarkerName() {
        return markersBox.getValue();
    }

    public String getMeasurementName() {
        return measurementsBox.getValue();
    }

    public Double getThreshold() {
        return threshold;
    }

    public AxisValue getAxisValue() {
        return axisValue;
    }
}

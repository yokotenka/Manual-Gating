package com.wehi.TableTreeViewHelpers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AxisTableEntry {

    // Axis the marker is on
    private AxisValue axisValue;

    // Markers selection box
    private ComboBox<String> markersBox = new ComboBox();
    // Measurements selection box
    private ComboBox<String> measurementsBox = new ComboBox();

    private TextField logThresholdTextField = new TextField("0");

    private TextField thresholdTextField = new TextField((String.valueOf(Math.exp(0))));

    // Threshold value
    private double threshold = (double) 0;

    public AxisTableEntry(AxisValue axisValue, ObservableList<String> markers, ObservableList<String> measurements){
        this.axisValue = axisValue;
        this.markersBox.setItems(markers);
        this.measurementsBox.setItems(measurements);


        logThresholdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*([\\.]\\d*)?")) {
                logThresholdTextField.setText(oldValue);
            }
        });

        thresholdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.]\\d*)?")) {
                thresholdTextField.setText(oldValue);
            }
        });

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

    public double getThreshold() {
        return threshold;
    }

    public AxisValue getAxisValue() {
        return axisValue;
    }

    public void setThresholdTextFields(double logThreshold, double lowerBound) {
        this.logThresholdTextField.setText(String.valueOf(logThreshold));

        if (Double.compare(logThreshold, lowerBound) <= 0){
            this.thresholdTextField.setText(String.valueOf(0));
            this.logThresholdTextField.setText(String.valueOf(lowerBound));
            threshold = 0;
        } else if (Double.compare(Math.exp(logThreshold), 255) > 0){
            threshold = 255;
            this.thresholdTextField.setText(String.valueOf(255));
        } else {
            threshold = Math.exp(logThreshold);
            this.thresholdTextField.setText(String.valueOf(Math.exp(logThreshold)));
        }
    }

    public TextField getLogThresholdTextField() {
        return logThresholdTextField;
    }

    public TextField getThresholdTextField() {
        return thresholdTextField;
    }
}

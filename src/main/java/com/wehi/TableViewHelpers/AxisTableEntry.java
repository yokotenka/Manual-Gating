package com.wehi.TableViewHelpers;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Class for the rows of the table for initialising the axes on the scatter chart
 */
public class AxisTableEntry {

    // The Measurement Deliminator
    private static String MEASUREMENT_DELIMINATOR = ": ";

    // Axis the marker is on
    private AxisValue axisValue;

    // Markers selection box
    private ComboBox<String> markersBox = new ComboBox();
    // Measurements selection box
    private ComboBox<String> measurementsBox = new ComboBox();
    // TextField for the logged threshold
    private TextField logThresholdTextField = new TextField("0");
    // TextField for the true threshold
    private TextField thresholdTextField = new TextField((String.valueOf(Math.exp(0))));

    // Threshold value
    private double threshold = 0;

    /**
     * Constructor
     * @param axisValue the enum value telling which axis it is on
     * @param markers the markers in this image
     * @param measurements the measurements in this image
     */
    public AxisTableEntry(AxisValue axisValue, ObservableList<String> markers, ObservableList<String> measurements){
        this.axisValue = axisValue;
        this.markersBox.setItems(markers);
        this.measurementsBox.setItems(measurements);

        // Set so that the text field only accepts decimals
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

    /**
     * Getter for the markers comboBox
     * @return ComboBox of markers
     */
    public ComboBox<String> getMarkersBox() {
        return markersBox;
    }

    /**
     * Getter for the measurements comboBox
     * @return ComboBox of measurements
     */
    public ComboBox<String> getMeasurementsBox() {
        return measurementsBox;
    }

    /**
     * Getter for the marker which was selected
     * @return selected marker
     */
    public String getMarkerName() {
        return markersBox.getValue();
    }

    /**
     * Getter for the selected measurement
     * @return selected measurement
     */
    public String getMeasurementName() {
        return measurementsBox.getValue();
    }

    /**
     * Getter for the threshold value
     * @return threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Getter for the Axis value
     * @return the string telling us which axis
     */
    public String getAxisValue() {
        if (axisValue == AxisValue.xAxis){
            return "X Axis";
        } else {
            return "Y Axis";
        }
    }

    /**
     * Setter for the thresholds
     * @param logThreshold the new logged threshold value
     * @param lowerBound the lower bound so that the axis doesn't show outliers
     */
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

    /**
     * Getter for the logged threshold text field
     * @return TextField
     */
    public TextField getLogThresholdTextField() {
        return logThresholdTextField;
    }

    /**
     * Getter for the true threshold text field
     * @return TextField
     */
    public TextField getThresholdTextField() {
        return thresholdTextField;
    }

    /**
     * Getter for the full measurement name
     * @return String
     */
    public String getFullMeasurementName(){
        return getMarkerName()+ MEASUREMENT_DELIMINATOR +getMeasurementName();
    }
}

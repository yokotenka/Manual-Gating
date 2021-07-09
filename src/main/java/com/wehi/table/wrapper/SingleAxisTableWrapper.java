package com.wehi.table.wrapper;

import com.wehi.observer.Observer;
import com.wehi.observer.Subject;
import com.wehi.table.entry.AxisTableEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;

import java.util.ArrayList;

/**
 * The table for selecting the marker and the measurement.
 */
public class SingleAxisTableWrapper extends TableWrapper<AxisTableEntry> implements Subject {
    // The entry of the table
    private final AxisTableEntry xAxis;
    // List of observers
    private final ArrayList<Observer> observers;

    /**
     * Constructor
     * @param markers the markers in the image
     * @param measurements the measurements of each marker
     */
    public SingleAxisTableWrapper(ObservableList<String> markers, ObservableList<String> measurements){
        super(new TableView<>());
        this.addColumn("Markers", "markersBox", 0.2);
        this.addColumn("Measurements", "measurementsBox", 0.2 );
        this.addColumn("Log Threshold", "logThresholdTextField", 0.25 );
        this.addColumn("Raw Threshold", "thresholdTextField", 0.25 );

        // Initialise the entry
        xAxis = new AxisTableEntry(AxisTableEntry.AxisValue.xAxis, markers, measurements);
        this.addRow(xAxis);
        observers = new ArrayList<>();
    }

    /**
     * Adds observers
     * @param o the observer
     */
    public void addObservers(Observer o){
        observers.add(o);
    }

    /**
     * Creates the behaviour when the comboBox has been selected
     */
    public void createSetOnAction(){
        createSetOnAction(xAxis.getMarkersBox());
        createSetOnAction(xAxis.getMeasurementsBox());
        xAxis.createSetOnAction();
    }

    // Behaviour for each comboBox
    private void createSetOnAction(ComboBox<String> box){
        box.setOnAction(e -> {
            if (xAxis.getMeasurementsBox().getValue() != null &&
                    xAxis.getMarkersBox().getValue() != null) {
                notifyObservers();
            }
        });
    }

    /**
     * Notifies any observers with the newest marker name and measurement
     */
    @Override
    public void notifyObservers() {
        for (Observer o : observers){
            o.update(xAxis.getMarkerName(),xAxis.getFullMeasurementName());
        }
    }

    /**
     * Setter for the threhsold
     * @param threshold
     * @param lowerBound
     */
    public void setXThreshold(double threshold, double lowerBound){
        xAxis.setThresholdTextFields(threshold, lowerBound);
    }

    /**
     * Getter for the threshold
     * @return the threshold value
     */
    public double getThreshold(){
        return xAxis.getThreshold();
    }

    /**
     * Getter for the full measurement name
     * @return full measurement name string
     */
    public String getFullMeasurementName(){
        return xAxis.getFullMeasurementName();
    }

    /**
     * Getter for the marker
     * @return marker
     */
    public String getMarker(){
        return xAxis.getMarkerName();
    }

    /**
     * Getter for the x Axos
     * @return
     */
    public AxisTableEntry getAxis() {
        return xAxis;
    }

    /**
     * Setter for the marker
     * @param marker the String
     */
    public void setMarkerName(String marker){
        xAxis.setMarkerName(marker);
    }

    /**
     * Setter of the measurement name
     * @param measurement
     */
    public void setMeasurementName(String measurement){
        xAxis.setMeasurementName(measurement);
    }

    /**
     * Adds a slider to the axis
     * @param slider
     */
    public void addSlider(Slider slider){
        xAxis.addSliderListener(slider);
    }

    public void setThreshold(double threshold){
        xAxis.setUnLoggedThreshold(String.valueOf(threshold), -6);
    }
}

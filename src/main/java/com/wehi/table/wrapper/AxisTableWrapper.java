package com.wehi.table.wrapper;

//import com.wehi.AxisValue;
import com.wehi.table.entry.AxisTableEntry;
import com.wehi.table.entry.AxisTableEntry.AxisValue;
import com.wehi.observer.Observer;
import com.wehi.observer.Subject;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;

import java.util.ArrayList;

/**
 * Class for selecting marker and measurement which will be gated on.
 */
public class AxisTableWrapper extends TableWrapper<AxisTableEntry> implements Subject {
    private AxisTableEntry xAxis;
    private AxisTableEntry yAxis;

    private ArrayList<Observer> observers;

    public AxisTableWrapper(ObservableList<String> markers, ObservableList<String> measurements){
        super(new TableView<>());
        this.addColumn("Axis", "axisValue", 0.1);
        this.addColumn("Markers", "markersBox", 0.2);
        this.addColumn("Measurements", "measurementsBox", 0.2 );
        this.addColumn("Log Threshold", "logThresholdTextField", 0.25 );
        this.addColumn("Raw Threshold", "thresholdTextField", 0.25 );

        xAxis = new AxisTableEntry(AxisValue.xAxis, markers, measurements);
        yAxis = new AxisTableEntry(AxisValue.yAxis, markers, measurements);
        this.addRow(xAxis);
        this.addRow(yAxis);

        observers = new ArrayList<>();
    }

    public void addObservers(Observer o){
        observers.add(o);
    }

    public void createSetOnAction(){
        createSetOnAction(xAxis.getMarkersBox());
        createSetOnAction(yAxis.getMarkersBox());
        createSetOnAction(xAxis.getMeasurementsBox());
        createSetOnAction(yAxis.getMeasurementsBox());
        xAxis.createSetOnAction();
        yAxis.createSetOnAction();
    }

    private void createSetOnAction(ComboBox<String> box){
        box.setOnAction(e -> {
            setOnAction();
        });
    }

    public void setOnAction(){
        if (yAxis.getMarkersBox().getValue() != null &&
                yAxis.getMeasurementsBox().getValue() != null &&
                xAxis.getMeasurementsBox().getValue() != null &&
                xAxis.getMarkersBox().getValue() != null) {
            notifyObservers();
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers){
            o.update(xAxis.getMarkerName(), yAxis.getMarkerName(), xAxis.getFullMeasurementName(), yAxis.getFullMeasurementName());
        }
    }

    public void setXThreshold(double threshold, double lowerBound){
        xAxis.setThresholdTextFields(threshold, lowerBound);
    }

    public void setYThreshold(double threshold, double lowerBound){
        yAxis.setThresholdTextFields(threshold, lowerBound);
    }

    public double getXThreshold(){
        return xAxis.getThreshold();
    }

    public double getYThreshold(){
        return yAxis.getThreshold();
    }

    public String getXAxisFullMeasurementName(){
        return xAxis.getFullMeasurementName();
    }

    public String getYAxisFullMeasurementName(){
        return yAxis.getFullMeasurementName();
    }

    public void setXSliderListener(Slider slider){
        xAxis.addSliderListener(slider);
    }

    public void setYSliderListener(Slider slider){
        yAxis.addSliderListener(slider);
    }

    public AxisTableEntry getXAxis() {
        return xAxis;
    }

    public AxisTableEntry getYAxis() {
        return yAxis;
    }
}

package com.wehi.table.wrapper;

import com.wehi.observer.Observer;
import com.wehi.observer.Subject;
import com.wehi.table.entry.AxisTableEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;

import java.util.ArrayList;

public class SingleAxisTableWrapper extends TableWrapper<AxisTableEntry> implements Subject {


    private final AxisTableEntry xAxis;

    private final ArrayList<Observer> observers;

    public SingleAxisTableWrapper(ObservableList<String> markers, ObservableList<String> measurements){
        super(new TableView<>());
//        this.addColumn("Axis", "axisValue", 0.1);
        this.addColumn("Markers", "markersBox", 0.2);
        this.addColumn("Measurements", "measurementsBox", 0.2 );
        this.addColumn("Log Threshold", "logThresholdTextField", 0.25 );
        this.addColumn("Raw Threshold", "thresholdTextField", 0.25 );

        xAxis = new AxisTableEntry(AxisTableEntry.AxisValue.xAxis, markers, measurements);
        this.addRow(xAxis);
        observers = new ArrayList<>();
    }

    public void addObservers(Observer o){
        observers.add(o);
    }

    public void createSetOnAction(){
        createSetOnAction(xAxis.getMarkersBox());
        createSetOnAction(xAxis.getMeasurementsBox());
        xAxis.createSetOnAction();
    }

    private void createSetOnAction(ComboBox<String> box){
        box.setOnAction(e -> {
            setOnAction();
        });
    }

    public void setOnAction(){
        if (xAxis.getMeasurementsBox().getValue() != null &&
                xAxis.getMarkersBox().getValue() != null) {
            notifyObservers();
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers){
            o.update(xAxis.getMarkerName(),xAxis.getFullMeasurementName());
        }
    }
    public void setXThreshold(double threshold, double lowerBound){
        xAxis.setThresholdTextFields(threshold, lowerBound);
    }
    public double getXThreshold(){
        return xAxis.getThreshold();
    }


    public String getXAxisFullMeasurementName(){
        return xAxis.getFullMeasurementName();
    }

    public String getMarker(){
        return xAxis.getMarkerName();
    }

    public void setXSliderListener(Slider slider){
        xAxis.addSliderListener(slider);
    }


    public AxisTableEntry getXAxis() {
        return xAxis;
    }
}

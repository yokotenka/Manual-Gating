package com.wehi.table.wrapper;

import com.wehi.table.entry.ChildPhenotypeTableEntry;
import com.wehi.table.observer.Observer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class ChildPhenotypeTableWrapper extends TableWrapper<ChildPhenotypeTableEntry> implements Observer {

    public ChildPhenotypeTableWrapper(){
        super(new TableView<>());

        this.addColumn("Marker Combination", "markerCombinationString", 0.3);
        this.addColumn("Phenotype Name", "phenotypeTextField", 0.5);
        this.addColumn("Create as Child", "selectedAsChildCheckBox", 0.2);
    }

    @Override
    public void update(String xAxisMarkerName, String yAxisMarkerName, String xAxisMeasurementName, String yAxisMeasurementName) {
        ObservableList<ChildPhenotypeTableEntry> list = FXCollections.observableArrayList();
        list.add(new ChildPhenotypeTableEntry(yAxisMarkerName,xAxisMarkerName, ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE,
                        yAxisMeasurementName,xAxisMeasurementName));
        list.add(new ChildPhenotypeTableEntry(xAxisMarkerName,yAxisMarkerName, ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH,
                    xAxisMeasurementName,yAxisMeasurementName));
        list.add(new ChildPhenotypeTableEntry(yAxisMarkerName,xAxisMarkerName, ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH,
                    yAxisMeasurementName,xAxisMeasurementName));
        list.add(new ChildPhenotypeTableEntry(xAxisMarkerName,yAxisMarkerName, ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE,
                    xAxisMeasurementName,yAxisMeasurementName));
        this.setItems(list);
    }

    @Override
    public void update(String str1, String str2) {
        return;
    }

    @Override
    public void updateXThreshold(double threshold){
        if (getTable() != null) {
            getTable().getItems().get(0).setThresholdTwo(threshold);
            getTable().getItems().get(1).setThresholdOne(threshold);
            getTable().getItems().get(2).setThresholdTwo(threshold);
            getTable().getItems().get(3).setThresholdOne(threshold);
        }
    }

    @Override
    public void updateYThreshold(double threshold){
        if (getTable() != null) {
            getTable().getItems().get(0).setThresholdOne(threshold);
            getTable().getItems().get(1).setThresholdTwo(threshold);
            getTable().getItems().get(2).setThresholdOne(threshold);
            getTable().getItems().get(3).setThresholdTwo(threshold);
        }
    }


}

package com.wehi.table.wrapper;

import com.wehi.observer.Observer;
import com.wehi.table.entry.ChildPhenotypeTableEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class FunctionalPhenotypeTableWrapper extends TableWrapper<ChildPhenotypeTableEntry> implements Observer {


    public FunctionalPhenotypeTableWrapper(){
        super(new TableView<>());

        this.addColumn("Marker Combination", "markerCombinationString", 0.3);
        this.addColumn("Phenotype Name", "phenotypeTextField", 0.5);
        this.addColumn("Create as Child", "selectedAsChildCheckBox", 0.2);
    }

    @Override
    public void update(String str1, String str2, String str3, String str4) {
    }

    @Override
    public void update(String str1, String str2) {
        ObservableList<ChildPhenotypeTableEntry> list = FXCollections.observableArrayList();
        list.add(new ChildPhenotypeTableEntry(str1, str2, ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE));
        list.add(new ChildPhenotypeTableEntry(str1, str2, ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE));
        this.setItems(list);
    }

    @Override
    public void updateXThreshold(double threshold) {
        if (getTable() != null) {
            getTable().getItems().get(0).setThresholdTwo(threshold);
            getTable().getItems().get(1).setThresholdOne(threshold);
        }
    }

    @Override
    public void updateYThreshold(double threshold) {
    }
}

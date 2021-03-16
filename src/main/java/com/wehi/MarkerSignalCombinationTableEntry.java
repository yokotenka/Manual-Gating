package com.wehi;

import javafx.scene.control.TextField;

public class MarkerSignalCombinationTableEntry {
    private String markerCombination;
    private TextField phenotypeNameTextField;

    public MarkerSignalCombinationTableEntry(String markerCombination){
        this.markerCombination = markerCombination;
        phenotypeNameTextField = new TextField();
    }

    public String getMarkerCombination() {
        return markerCombination;
    }

    public TextField getPhenotypeNameTextField() {
        return phenotypeNameTextField;
    }
}

package com.wehi.TableTreeViewHelpers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;



// If MARKER_COMBINATION is ONE_OF_EACH, then markerOne is the positive marker,
// markerTwo is the negative marker.
public class PhenotypeCreationTableEntry {

    // TextField to write the phenotype in
    private TextField phenotypeTextField = new TextField();
    // Positive marker
    private String markerOne;
    // Negative marker
    private String markerTwo;

    // Measurement name;
    private String measurementOne;
    private String measurementTwo;

    // Threshold value
    private double thresholdOne;
    private double thresholdTwo; 

    // Number of positive / negative
    private MARKER_COMBINATION markerCombination;

    // Select to create child
    private CheckBox selectedAsChildCheckBox = new CheckBox();


    public PhenotypeCreationTableEntry(String markerOne, String markerTwo, MARKER_COMBINATION markerCombination,
    String measurementOne, String measurementTwo
    ){
        this.markerOne = markerOne;
        this.markerTwo = markerTwo;
        this.measurementOne = measurementOne;
        this.measurementTwo = measurementTwo;
        this.markerCombination = markerCombination;
    }


    public TextField getPhenotypeTextField() {
        return phenotypeTextField;
    }

    public CheckBox getSelectedAsChildCheckBox() {
        return selectedAsChildCheckBox;
    }

    public String getMarkerCombination(){
        if (markerCombination == MARKER_COMBINATION.TWO_NEGATIVE) {
            if (markerOne.equals("") && markerTwo.equals("")) {
                return null;
            }
            if (markerOne.equals("")) {
                return "-" + markerTwo;
            }
            if (markerTwo.equals("")) {
                return "-" + markerOne;
            }
            return "-" + markerOne + "," + "-" + markerTwo;
        } else if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE){
            if (markerOne.equals("") && markerTwo.equals("")) {
                return null;
            }
            if (markerOne.equals("")) {
                return "+" + markerTwo;
            }
            if (markerTwo.equals("")) {
                return "+" + markerOne;
            }
            return "+" + markerOne + "," + "+" + markerTwo;
        } else {
            if (markerOne.equals("") && markerTwo.equals("")) {
                return null;
            }
            if (markerOne.equals("")) {
                return "-" + markerTwo;
            }
            if (markerTwo.equals("")) {
                return "+" + markerOne;
            }
            return "+" + markerOne + "," + "-" + markerTwo;
        }
    }

    public void setPositiveMarker(String positiveMarker) {
        this.markerOne = positiveMarker;
    }

    public void setMarkerTwo(String markerTwo) {
        this.markerTwo = markerTwo;
    }

    public String getMarkerTwo() {
        return markerTwo;
    }

    public String getMarkerOne(){
        return markerOne;
    }


    public String getPositiveMarker() {
        return markerOne;
    }

    public String getPhenotypeName(){
        return phenotypeTextField.getText();
    }

    public boolean getIsSelected(){
        return selectedAsChildCheckBox.isSelected();
    }

    public MARKER_COMBINATION getMARKERCOMBINATION(){
        return markerCombination;
    }

    public String getMeasurementOne() {
        return measurementOne;
    }

    public String getMeasurementTwo() {
        return measurementTwo;
    }

    public void setThresholdOne(double thresholdOne) {
        this.thresholdOne = thresholdOne;
    }

    public void setThresholdTwo(double thresholdTwo) {
        this.thresholdTwo = thresholdTwo;
    }

    public double getThresholdOne() {
        return thresholdOne;
    }

    public double getThresholdTwo() {
        return thresholdTwo;
    }

    public enum MARKER_COMBINATION{
        TWO_POSITIVE,
        TWO_NEGATIVE,
        ONE_OF_EACH
    }
}

package com.wehi.TableViewHelpers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;


/**
 * Class for the row entries of the table for initialising phenotypes.
 * If one marker is positive and one is negative,
 * markerOne is positive
 * markerTwo is negative
 */
public class PhenotypeCreationTableEntry {

    // TextField to write the phenotype in
    private TextField phenotypeTextField = new TextField();

    // Positive marker
    private String markerOne;
    // Negative marker
    private String markerTwo;
    // Measurement name one;
    private String measurementOne;
    // Measurement name two
    private String measurementTwo;

    // Threshold value one
    private double thresholdOne;
    // Threshold value two
    private double thresholdTwo; 

    // Number of positive / negative
    private MARKER_COMBINATION markerCombination;

    // Select to create child
    private CheckBox selectedAsChildCheckBox = new CheckBox();

    /**
     * Constructor
     * @param markerOne name of first marker
     * @param markerTwo name of second marker
     * @param markerCombination the combination
     * @param measurementOne the measurement of first marker
     * @param measurementTwo the measurement of second marker
     */
    public PhenotypeCreationTableEntry(String markerOne, String markerTwo, MARKER_COMBINATION markerCombination,
    String measurementOne, String measurementTwo
    ){
        this.markerOne = markerOne;
        this.markerTwo = markerTwo;
        this.measurementOne = measurementOne;
        this.measurementTwo = measurementTwo;
        this.markerCombination = markerCombination;
    }


    /**
     * Getter for the text field
     * @return TextField
     */
    public TextField getPhenotypeTextField() {
        return phenotypeTextField;
    }


    /**
     * Getter for the check box
     * @return CheckBox
     */
    public CheckBox getSelectedAsChildCheckBox() {
        return selectedAsChildCheckBox;
    }


    /**
     * Getter for the Marker Combination string
     * @return String
     */
    public String getMarkerCombinationString(){
        String op1 = "+", op2 = "-";
        if (markerCombination == MARKER_COMBINATION.TWO_NEGATIVE){
            op1 = "-";
        } else if(markerCombination == MARKER_COMBINATION.TWO_POSITIVE){
            op2 = "+";
        }
        if (markerOne.equals("") && markerTwo.equals("")) {
            return null;
        }if (markerOne.equals("")) {
            return op2 + markerTwo;
        }if (markerTwo.equals("")) {
            return op1 + markerOne;
        }
        return op1 + markerOne + "," + op2 + markerTwo;
    }


    /**
     * Getter for second marker
     * @return markerTwo
     */
    public String getMarkerTwo() {
        return markerTwo;
    }

    /**
     * Getter for first marker
     * @return markerOne
     */
    public String getMarkerOne(){
        return markerOne;
    }


    /**
     * Getter for phenotype name
     * @return phenotypeName
     */
    public String getPhenotypeName(){
        return phenotypeTextField.getText();
    }


    /**
     * Getter for whether the phenotype is selected or not
     * @return isSelected
     */
    public boolean getIsSelected(){
        return selectedAsChildCheckBox.isSelected();
    }


    /**
     * Marker combination
     * @return Marker combination
     */
    public MARKER_COMBINATION getMarkerCombination(){
        return markerCombination;
    }

    /**
     * Getter for the first measurement
     * @return measurementOne
     */
    public String getMeasurementOne() {
        return measurementOne;
    }

    /**
     * Getter for the second measurement
     * @return
     */
    public String getMeasurementTwo() {
        return measurementTwo;
    }

    /**
     * Setter for the first threshold
     * @param thresholdOne
     */
    public void setThresholdOne(double thresholdOne) {
        this.thresholdOne = thresholdOne;
    }

    /**
     * Setter for the second threshold
     * @param thresholdTwo
     */
    public void setThresholdTwo(double thresholdTwo) {
        this.thresholdTwo = thresholdTwo;
    }

    /**
     * Getter for first threshold
     * @return
     */
    public double getThresholdOne() {
        return thresholdOne;
    }

    /**
     * Getter for second threshold
     * @return
     */
    public double getThresholdTwo() {
        return thresholdTwo;
    }

    /**
     * Enumeration for the marker combination
     */
    public enum MARKER_COMBINATION{
        TWO_POSITIVE,
        TWO_NEGATIVE,
        ONE_OF_EACH
    }

    public void setName(String name){
        phenotypeTextField.setText(name);
    }

    public void selectedAsChildCheckBox(){
        selectedAsChildCheckBox.setSelected(true);
    }
}

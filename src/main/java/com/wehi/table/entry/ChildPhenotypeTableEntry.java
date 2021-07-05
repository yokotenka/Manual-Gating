package com.wehi.table.entry;

import com.wehi.pathclasshandler.PathClassHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Class for the row entries of the table for initialising phenotypes.
 * If one marker is positive and one is negative,
 * markerOne is positive
 * markerTwo is negative
 */
public class ChildPhenotypeTableEntry {

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



    public ChildPhenotypeTableEntry(String markerOne, String measurementOne, MARKER_COMBINATION markerCombination){
        this.markerOne = markerOne;
        this.measurementOne = measurementOne;
        if (markerCombination == MARKER_COMBINATION.ONE_OF_EACH) {
            this.markerCombination = MARKER_COMBINATION.TWO_POSITIVE;
        } else {
            this.markerCombination = markerCombination;
        }
        this.markerTwo = "";
        this.measurementTwo = "";
    }


    /**
     * Constructor
     * @param markerOne name of first marker
     * @param markerTwo name of second marker
     * @param markerCombination the combination
     * @param measurementOne the measurement of first marker
     * @param measurementTwo the measurement of second marker
     */
    public ChildPhenotypeTableEntry(String markerOne, String markerTwo, MARKER_COMBINATION markerCombination,
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
     * Filters the cells with the threshold
     * @param cells cells to be filtered
     * @return filtered cells
     */
    public Collection<PathObject> filterCells(Collection<PathObject> cells) {
        Collection<PathObject> filteredCells;
        if (!markerTwo.equals("")) {
            if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE) {
                filteredCells = cells.stream().filter(p -> p.getMeasurementList()
                        .getMeasurementValue(measurementOne) > thresholdOne)
                        .filter(p -> p.getMeasurementList().getMeasurementValue(measurementTwo) > thresholdTwo)
                        .collect(Collectors.toList());
            } else if (markerCombination == MARKER_COMBINATION.TWO_NEGATIVE) {
                filteredCells = cells.stream().filter(p -> p.getMeasurementList()
                        .getMeasurementValue(measurementOne) < thresholdOne)
                        .filter(p -> p.getMeasurementList().getMeasurementValue(measurementTwo) < thresholdTwo)
                        .collect(Collectors.toList());
            } else {
                filteredCells = cells.stream().filter(p -> p.getMeasurementList()
                        .getMeasurementValue(measurementOne) > thresholdOne)
                        .filter(p -> p.getMeasurementList().getMeasurementValue(measurementTwo) < thresholdTwo)
                        .collect(Collectors.toList());
            }
            return filteredCells;
        } else {
            if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE) {
                filteredCells = cells.stream().filter(p -> p.getMeasurementList()
                        .getMeasurementValue(measurementOne) > thresholdOne)
                        .collect(Collectors.toList());
            } else {
                filteredCells = cells.stream().filter(p -> p.getMeasurementList()
                        .getMeasurementValue(measurementOne) < thresholdOne)
                        .collect(Collectors.toList());
            }
            return filteredCells;
        }

    }


    public Collection<PathObject> filterCellsAndUpdatePathClass(Collection<PathObject> cells, String oldName){
        ArrayList<PathObject> filteredCells = new ArrayList<>();
        if (!markerTwo.equals("")) {
            if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE) {
                for (PathObject cell : cells) {
                    if (cell.getMeasurementList().getMeasurementValue(measurementOne) > thresholdOne
                            && cell.getMeasurementList().getMeasurementValue(measurementTwo) > thresholdTwo) {
                        filteredCells.add(cell);
                        PathClassHandler.replacePathClass(cell, oldName, getPhenotypeName());
                    } else {
                        PathClassHandler.removeNoLongerPositive(cell, oldName);
                    }
                }
            } else if (markerCombination == MARKER_COMBINATION.TWO_NEGATIVE) {
                for (PathObject cell : cells) {
                    if (cell.getMeasurementList().getMeasurementValue(measurementOne) < thresholdOne
                            && cell.getMeasurementList().getMeasurementValue(measurementTwo) < thresholdTwo) {
                        filteredCells.add(cell);
                        PathClassHandler.replacePathClass(cell, oldName, getPhenotypeName());
                    } else {
                        PathClassHandler.removeNoLongerPositive(cell, oldName);
                    }
                }
            } else {
                for (PathObject cell : cells) {
                    if (cell.getMeasurementList().getMeasurementValue(measurementOne) > thresholdOne
                            && cell.getMeasurementList().getMeasurementValue(measurementTwo) < thresholdTwo) {
                        filteredCells.add(cell);
                        PathClassHandler.replacePathClass(cell, oldName, getPhenotypeName());
                    } else {
                        PathClassHandler.removeNoLongerPositive(cell, oldName);
                    }
                }
            }
            return filteredCells;
        } else {
            if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE){
                for (PathObject cell : cells){
                    if (cell.getMeasurementList().getMeasurementValue(measurementOne) > thresholdOne) {
                        filteredCells.add(cell);
                        PathClassHandler.replacePathClass(cell, oldName, getPhenotypeName());
                    } else {
                        PathClassHandler.removeNoLongerPositive(cell, oldName);
                    }
                }
            } else {
                for (PathObject cell : cells){
                    if (cell.getMeasurementList().getMeasurementValue(measurementOne) < thresholdOne) {
                        filteredCells.add(cell);
                        PathClassHandler.replacePathClass(cell, oldName, getPhenotypeName());
                    } else {
                        PathClassHandler.removeNoLongerPositive(cell, oldName);
                    }
                }
            }
            return filteredCells;
        }
    }


    /**
     * Gets the new positive marker array list
     * @param positiveMarkers current positive marker list
     * @return new positive marker array list
     */
    public ArrayList<String> getNewPositiveMarkers(ArrayList<String> positiveMarkers){
        ArrayList<String> newPositiveMarkers;
        if (markerCombination == MARKER_COMBINATION.TWO_POSITIVE) {
            if (positiveMarkers != null) {
                    newPositiveMarkers = new ArrayList<>(positiveMarkers);
            } else {
                    newPositiveMarkers = new ArrayList<>();
            }
            // Checks if markerOne is already in the positive array list
            if (newPositiveMarkers.stream().noneMatch(p -> p.equals(markerOne))) {
                newPositiveMarkers.add(markerOne);
            }
            if (newPositiveMarkers.stream().noneMatch(p -> p.equals(markerTwo))) {
                newPositiveMarkers.add(markerTwo);
            }
        }else if(markerCombination == MARKER_COMBINATION.TWO_NEGATIVE){
            if (positiveMarkers == null) {
                newPositiveMarkers = null;
            } else {
                newPositiveMarkers = new ArrayList<>(positiveMarkers);
            }
        } else{
            if (positiveMarkers != null) {
                    newPositiveMarkers = new ArrayList<>(positiveMarkers);
            } else {
                newPositiveMarkers = new ArrayList<>();
            }
            // Checks if markerOne is already in the positive array list
            if (newPositiveMarkers.stream().noneMatch(p -> p.equals(markerOne))) {
                newPositiveMarkers.add(markerOne);
            }
        }
        return newPositiveMarkers;
    }

    public ArrayList<String> getNewNegativeMarkers(ArrayList<String> negativeMarkers){
        ArrayList<String> newNegativeMarkers;
        if (markerCombination == MARKER_COMBINATION.TWO_NEGATIVE) {
            if (negativeMarkers != null) {
                newNegativeMarkers = new ArrayList<>(negativeMarkers);
            } else {
                newNegativeMarkers = new ArrayList<>();
            }
            // Checks if markerOne is already in the positive array list
            if (newNegativeMarkers.stream().noneMatch(p -> p.equals(markerOne))) {
                newNegativeMarkers.add(markerOne);
            }
            if (newNegativeMarkers.stream().noneMatch(p -> p.equals(markerTwo))) {
                newNegativeMarkers.add(markerTwo);
            }
        }else if(markerCombination == MARKER_COMBINATION.TWO_POSITIVE){
            if (negativeMarkers == null) {
                newNegativeMarkers = null;
            } else {
                newNegativeMarkers = new ArrayList<>(negativeMarkers);
            }
        } else{
            if (negativeMarkers != null) {
                newNegativeMarkers = new ArrayList<>(negativeMarkers);
            } else {
                newNegativeMarkers = new ArrayList<>();
            }
            // Checks if markerOne is already in the positive array list
            if (newNegativeMarkers.stream().noneMatch(p -> p.equals(markerTwo))) {
                newNegativeMarkers.add(markerTwo);
            }
        }
        return newNegativeMarkers;
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

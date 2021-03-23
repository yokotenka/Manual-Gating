package com.wehi.TableTreeViewHelpers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;


public class PhenotypeCreationTableEntry {

    // TextField to write the phenotype in
    private TextField phenotypeTextField = new TextField();
    // Positive marker
    private String positiveMarker;
    // Negative marker
    private String negativeMarker;

    // Select to create child
    private CheckBox selectedAsChildCheckBox = new CheckBox();


    public PhenotypeCreationTableEntry(String positiveMarker, String negativeMarker){
        this.positiveMarker = positiveMarker;
        this.negativeMarker = negativeMarker;
    }


    public TextField getPhenotypeTextField() {
        return phenotypeTextField;
    }

    public CheckBox getSelectedAsChildCheckBox() {
        return selectedAsChildCheckBox;
    }

    public String getMarkerCombination(){
        if (positiveMarker.equals("") && negativeMarker.equals("")){
            return null;
        }
        if (positiveMarker.equals("")){
            return "-" + negativeMarker;
        }
        if (negativeMarker.equals("")){
            return "+" + positiveMarker;
        }
        return "+" + positiveMarker + "," + "-" + negativeMarker;
    }

    public void setPositiveMarker(String positiveMarker) {
        this.positiveMarker = positiveMarker;
    }

    public void setNegativeMarker(String negativeMarker) {
        this.negativeMarker = negativeMarker;
    }

    public String getNegativeMarker() {
        return negativeMarker;
    }

    public String getPositiveMarker() {
        return positiveMarker;
    }

    public String getPhenotypeName(){
        return phenotypeTextField.getText();
    }
}

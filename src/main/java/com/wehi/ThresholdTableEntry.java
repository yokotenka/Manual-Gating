package com.wehi;

import javafx.scene.control.TextField;

public class ThresholdTableEntry {

    private String measurementName;
    private TextField logThresholdField;
    private TextField rawThresholdField;

    public ThresholdTableEntry(String measurementName, double logThreshold, double rawThreshold){
        this.measurementName = measurementName;
        logThresholdField = new TextField(Double.toString(logThreshold));
        rawThresholdField = new TextField(Double.toString(rawThreshold));
    }







    public static class DecimalTextField extends TextField{

    }
}

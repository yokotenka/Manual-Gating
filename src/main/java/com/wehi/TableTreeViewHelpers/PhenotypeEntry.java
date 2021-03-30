package com.wehi.TableTreeViewHelpers;

import com.wehi.ManualGatingWindow;
import com.wehi.TableCreator;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;

enum AxisValue {
    xAxis,
    yAxis
}

public class PhenotypeEntry {

    // Phenotype Name
    private String phenotypeName;
    // Array of positive markers
    private ArrayList<String> positiveMarkers;
    // Array of negative markers
    private ArrayList<String> negativeMarkers;

    // The cells
    private Collection<PathObject> cells;
    // The markers
    private ObservableList<String> markers;
    // The measurements
    private ObservableList<String> measurements;



    private AxisTableEntry xAxis;
    private AxisTableEntry yAxis;
    // x-Axis Marker measurement name
    private String xAxisMarkerMeasurementName;
    // y-Axis Marker measurement name
    private String yAxisMarkerMeasurementName;
    // Deliminator
    private static String MEASUREMENT_DELIMINATOR = ": ";

    /* We will have a Column for each phenotype */


    // ComboBox to select the number of dimensions
    private SplitPane pane;



    private TableCreator<AxisTableEntry> axisOptionsTableCreator;
    private TableCreator<PhenotypeCreationTableEntry> phenotypeCreationTableCreator;
    private CytometryChart cytometryChart;


    public PhenotypeEntry(Collection<PathObject> cells, String phenotypeName,
                          ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers,
                          ObservableList<String> markers, ObservableList<String> measurements
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.positiveMarkers = positiveMarkers;
        this.negativeMarkers = negativeMarkers;

        this.markers = markers;
        this.measurements = measurements;

    }



    public SplitPane createPane(Stage stage){
        pane = new SplitPane();

        // Initialise the phenotype creation table creator
        initialisePhenotypeCreationTableCreator();
        // Initialise the axis options table creator
        initialiseAxisOptionsTableCreator();




        /* Graph */
        initialiseCytometryChart(stage);


        /* Create column on the right */
        pane.setOrientation(Orientation.VERTICAL);
        pane.getItems().addAll(
                ManualGatingWindow.createColumn(
                        axisOptionsTableCreator.getTable(),
                        phenotypeCreationTableCreator.getTable()
                ),
                cytometryChart.getPane()
        );

        return pane;
    }



    /* Initialising JavaFX nodes */
    public void initialiseAxisOptionsTableCreator(){
        axisOptionsTableCreator = new TableCreator<>();

        axisOptionsTableCreator.addColumn("Axis", "axisValue", 0.1);
        axisOptionsTableCreator.addColumn("Markers", "markersBox", 0.2);
        axisOptionsTableCreator.addColumn("Measurements", "measurementsBox", 0.2 );
        axisOptionsTableCreator.addColumn("Log Threshold", "logThresholdTextField", 0.25 );
        axisOptionsTableCreator.addColumn("Raw Threshold", "thresholdTextField", 0.25 );

        xAxis = new AxisTableEntry(AxisValue.xAxis, markers, measurements);
        yAxis = new AxisTableEntry(AxisValue.yAxis, markers, measurements);
        axisOptionsTableCreator.addRow(xAxis);
        axisOptionsTableCreator.addRow(yAxis);

        createSetOnAction(xAxis, yAxis);
        createSetOnAction(yAxis, xAxis);
    }

    public void initialisePhenotypeCreationTableCreator(){
        phenotypeCreationTableCreator = new TableCreator<>();

        phenotypeCreationTableCreator.addColumn("Marker Combination", "markerCombination", 0.3);
        phenotypeCreationTableCreator.addColumn("Phenotype Name", "phenotypeTextField", 0.5);
        phenotypeCreationTableCreator.addColumn("Create as Child", "selectedAsChildCheckBox", 0.2);

    }


    private void updatePhenotypeCreationCreator(String marker1, String marker2){

        ObservableList<PhenotypeCreationTableEntry> list = FXCollections.observableArrayList();

        list.add(new PhenotypeCreationTableEntry(marker2, marker1, MARKER_COMBINATION.TWO_POSITIVE));
        list.add(new PhenotypeCreationTableEntry(marker1, marker2, MARKER_COMBINATION.ONE_OF_EACH));
        list.add(new PhenotypeCreationTableEntry(marker2, marker1, MARKER_COMBINATION.ONE_OF_EACH));
        list.add(new PhenotypeCreationTableEntry(marker1, marker2, MARKER_COMBINATION.TWO_NEGATIVE));

        phenotypeCreationTableCreator.getTable().setItems(list);
    }

    private void updateMeasurementNames(){
        xAxisMarkerMeasurementName = xAxis.getMarkersBox().getValue() + MEASUREMENT_DELIMINATOR + xAxis.getMeasurementsBox().getValue();
        yAxisMarkerMeasurementName = yAxis.getMarkersBox().getValue() + MEASUREMENT_DELIMINATOR + yAxis.getMeasurementsBox().getValue();
    }









    /* ******************** The behaviour of the nodes which exists in other classes ******************************* */

    // The behaviour for the ComboBoxes in the AxisTableEntry to set the marker and measurement names
    private void createSetOnAction(AxisTableEntry axis1, AxisTableEntry axis2){
        axis1.getMarkersBox().setOnAction(e -> {
            if (axis1.getMeasurementsBox().getValue() != null) {
                if (axis2.getMeasurementsBox().getValue() != null && axis2.getMarkersBox().getValue() != null) {
                    updatePhenotypeCreationCreator(axis1.getMarkersBox().getValue(), axis2.getMarkersBox().getValue());
                    updateMeasurementNames();
                    cytometryChart.updateAxisLabels(xAxisMarkerMeasurementName, yAxisMarkerMeasurementName);
                    cytometryChart.populateScatterChart(cells, xAxisMarkerMeasurementName, yAxisMarkerMeasurementName);
                }
            }
        });
        axis1.getMeasurementsBox().setOnAction(e -> {
            if (axis1.getMarkersBox().getValue() != null) {
                if (axis2.getMeasurementsBox().getValue() != null && axis2.getMarkersBox().getValue() != null) {
                    updatePhenotypeCreationCreator(axis1.getMarkersBox().getValue(), axis2.getMarkersBox().getValue());
                    updateMeasurementNames();
                    cytometryChart.updateAxisLabels(xAxisMarkerMeasurementName, yAxisMarkerMeasurementName);
                    cytometryChart.populateScatterChart(cells, xAxisMarkerMeasurementName, yAxisMarkerMeasurementName);
                }
            }
        });
    }

    // The behaviour of the Sliders in the ChartWrapper to get the threshold values for each of the markers
    private void initialiseCytometryChart(Stage stage){
        cytometryChart = new CytometryChart(stage);
        cytometryChart.getXSlider().valueProperty().addListener((arg0, oldValue, newValue) -> xAxis.setThresholdTextFields(newValue.doubleValue()));


        cytometryChart.getYSlider().valueProperty().addListener((arg0, oldValue, newValue) -> yAxis.setThresholdTextFields(newValue.doubleValue()));

        xAxis.getLogThresholdTextField().setOnAction(e -> {
            if (xAxis.getLogThresholdTextField().getText(0, 1).equals(".")){
                xAxis.getLogThresholdTextField().setText("0" + xAxis.getLogThresholdTextField().getText());
            }
            if (xAxis.getLogThresholdTextField().getText(0,2).equals("-.")){
                xAxis.getLogThresholdTextField().setText("-0"+xAxis.getLogThresholdTextField().getText(1, xAxis.getLogThresholdTextField().getText().length()));
            }
            if (xAxis.getLogThresholdTextField().getText(xAxis.getLogThresholdTextField().getText().length()-1, xAxis.getLogThresholdTextField().getText().length()).equals(".")){
                xAxis.getLogThresholdTextField().setText(xAxis.getLogThresholdTextField().getText(0, xAxis.getLogThresholdTextField().getText().length()-1));
            }
            if (xAxis.getLogThresholdTextField().getText().equals("-0")){
                xAxis.getLogThresholdTextField().setText("0");
            }
            if (Double.valueOf(xAxis.getLogThresholdTextField().getText()).compareTo(6.0) > 0){
                xAxis.getLogThresholdTextField().setText("6");
            }
            if (Double.valueOf(xAxis.getLogThresholdTextField().getText()).compareTo(-6.0) < 0){
                xAxis.getLogThresholdTextField().setText("-6");
            }
            xAxis.setThresholdTextFields(Double.parseDouble(xAxis.getLogThresholdTextField().getText()));
            cytometryChart.getXSlider().setValue(Double.parseDouble(xAxis.getLogThresholdTextField().getText()));
        });

        xAxis.getThresholdTextField().setOnAction(e -> {
            if (xAxis.getThresholdTextField().getText(0, 1).equals(".")){
                xAxis.getThresholdTextField().setText("0" + xAxis.getThresholdTextField().getText());
            }
            if (xAxis.getThresholdTextField().getText(0,2).equals("-.")){
                xAxis.getThresholdTextField().setText("-0"+xAxis.getThresholdTextField().getText(1, xAxis.getThresholdTextField().getText().length()));
            }
            if (xAxis.getThresholdTextField().getText(xAxis.getThresholdTextField().getText().length()-1, xAxis.getThresholdTextField().getText().length()).equals(".")){
                xAxis.getThresholdTextField().setText(xAxis.getThresholdTextField().getText(0, xAxis.getThresholdTextField().getText().length()-1));
            }
            if (xAxis.getThresholdTextField().getText().equals("-0")){
                xAxis.getThresholdTextField().setText("0");
            }
            if (Double.valueOf(xAxis.getThresholdTextField().getText()).compareTo(255.0) > 0){
                xAxis.getThresholdTextField().setText("255");
            }
            if (Double.valueOf(xAxis.getThresholdTextField().getText()).compareTo(0.0) < 0){
                xAxis.getThresholdTextField().setText("0");
            }
            xAxis.setThresholdTextFields(Math.log(Double.parseDouble(xAxis.getThresholdTextField().getText())));
            cytometryChart.getXSlider().setValue(Math.log(Double.parseDouble(xAxis.getThresholdTextField().getText())));
        });


        yAxis.getLogThresholdTextField().setOnAction(e -> {
            if (yAxis.getLogThresholdTextField().getText(0, 1).equals(".")){
                yAxis.getLogThresholdTextField().setText("0" + yAxis.getLogThresholdTextField().getText());
            }
            if (yAxis.getLogThresholdTextField().getText(0,2).equals("-.")){
                yAxis.getLogThresholdTextField().setText("-0"+yAxis.getLogThresholdTextField().getText(1, yAxis.getLogThresholdTextField().getText().length()));
            }
            if (yAxis.getLogThresholdTextField().getText(yAxis.getLogThresholdTextField().getText().length()-1, yAxis.getLogThresholdTextField().getText().length()).equals(".")){
                yAxis.getLogThresholdTextField().setText(yAxis.getLogThresholdTextField().getText(0, yAxis.getLogThresholdTextField().getText().length()-1));
            }
            if (yAxis.getLogThresholdTextField().getText().equals("-0")){
                yAxis.getLogThresholdTextField().setText("0");
            }
            if (Double.valueOf(yAxis.getLogThresholdTextField().getText()).compareTo(6.0) > 0){
                yAxis.getLogThresholdTextField().setText("6");
            }
            if (Double.valueOf(yAxis.getLogThresholdTextField().getText()).compareTo(-6.0) < 0){
                yAxis.getLogThresholdTextField().setText("-6");
            }
            yAxis.setThresholdTextFields(Double.parseDouble(yAxis.getLogThresholdTextField().getText()));
            cytometryChart.getYSlider().setValue(Double.parseDouble(yAxis.getLogThresholdTextField().getText()));
        });

        yAxis.getThresholdTextField().setOnAction(e -> {
            if (yAxis.getThresholdTextField().getText(0, 1).equals(".")){
                yAxis.getThresholdTextField().setText("0" + yAxis.getThresholdTextField().getText());
            }
            if (yAxis.getThresholdTextField().getText(0,2).equals("-.")){
                yAxis.getThresholdTextField().setText("-0"+yAxis.getThresholdTextField().getText(1, yAxis.getThresholdTextField().getText().length()));
            }
            if (yAxis.getThresholdTextField().getText(yAxis.getThresholdTextField().getText().length()-1, yAxis.getThresholdTextField().getText().length()).equals(".")){
                yAxis.getThresholdTextField().setText(yAxis.getThresholdTextField().getText(0, yAxis.getThresholdTextField().getText().length()-1));
            }
            if (yAxis.getThresholdTextField().getText().equals("-0")){
                yAxis.getThresholdTextField().setText("0");
            }
            if (Double.valueOf(yAxis.getThresholdTextField().getText()).compareTo(255.0) > 0){
                yAxis.getThresholdTextField().setText("255");
            }
            if (Double.valueOf(yAxis.getThresholdTextField().getText()).compareTo(0.0) < 0){
                yAxis.getThresholdTextField().setText("0");
            }
            yAxis.setThresholdTextFields(Math.log(Double.parseDouble(yAxis.getThresholdTextField().getText())));
            cytometryChart.getYSlider().setValue(Math.log(Double.parseDouble(yAxis.getThresholdTextField().getText())));
        });

    }





    public SplitPane getPane() {
        return pane;
    }

    public String getPhenotypeName() {
        return phenotypeName;
    }

    public ArrayList<String> getPositiveMarkers() {
        return positiveMarkers;
    }

    public ArrayList<String> getNegativeMarkers() {
        return negativeMarkers;
    }

    public String getPositiveMarkersString(){
        return positiveMarkers.toString().substring(1, positiveMarkers.toString().length()-1);
    }

    public String getNegativeMarkersString(){
        return negativeMarkers.toString().substring(1, positiveMarkers.toString().length()-1);
    }

    /**
     * Assigns the Phenotype name to the PathClass for each of the cells. Creates a column in the cell measurement
     * list containing boolean values for each of the markers it is positive for.
     */
    public void assignCellPhenotypes(){

    }




}

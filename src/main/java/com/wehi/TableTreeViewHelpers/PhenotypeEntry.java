package com.wehi.TableTreeViewHelpers;

import com.wehi.ManualGatingWindow;
import com.wehi.TableCreator;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
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


    // x-Axis Marker measurement name
    private String xAxisMarkerMeasurementName;
    // y-Axis Marker measurement name
    private String yAxisMarkerMeasurementName;


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
        // Initialise the axis options table creator
        initialiseAxisOptionsTableCreator();

        // Initialise the phenotype creation table creator
        initialisePhenotypeCreationTableCreator();


        /* Graph */
        cytometryChart = new CytometryChart(stage);


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

        axisOptionsTableCreator.addColumn("Axis", "axisValue", 0.2);
        axisOptionsTableCreator.addColumn("Markers", "markersBox", 0.4);
        axisOptionsTableCreator.addColumn("Measurements", "measurementsBox", 0.4 );


        AxisTableEntry xAxis = new AxisTableEntry(AxisValue.xAxis, markers, measurements);
        axisOptionsTableCreator.addRow(xAxis);
        xAxis.getMarkersBox().setOnAction(e -> {
            if (xAxis.getMeasurementsBox().getValue() != null){
//                phenotypeCreationTableCreator.getItems().get(0).setPositiveMarker(xAxis.getMarkersBox().getValue());
//                phenotypeCreationTableCreator.getItems().get(1).setPositiveMarker(xAxis.getMarkersBox().getValue());
//                phenotypeCreationTableCreator.getItems().get(2).setNegativeMarker(xAxis.getMarkersBox().getValue());
//                phenotypeCreationTableCreator.getItems().get(3).setNegativeMarker(xAxis.getMarkersBox().getValue());
            }
        });
        xAxis.getMeasurementsBox().setOnAction(e -> {
            if (xAxis.getMarkersBox().getValue() != null){
//                phenotypeCreationTableCreator.getItems().get();
//                phenotypeCreationTableCreator.getItems().get(1).setPositiveMarker(xAxis.getMarkersBox().getValue());
//                phenotypeCreationTableCreator.getItems().get(2).setNegativeMarker(xAxis.getMarkersBox().getValue());
//                phenotypeCreationTableCreator.getItems().get(3).setNegativeMarker(xAxis.getMarkersBox().getValue());
            }
        });

        AxisTableEntry yAxis = new AxisTableEntry(AxisValue.yAxis, markers, measurements);
        yAxis.getMarkersBox().setOnAction(e -> {

        });
        yAxis.getMeasurementsBox().setOnAction(e -> {

        });

        axisOptionsTableCreator.addRow(yAxis);
    }

    // TODO: Add in listener in the axisOptionsTableCreator which will add in rows to the phenotypeCreationTableCreator
    public void initialisePhenotypeCreationTableCreator(){
        phenotypeCreationTableCreator = new TableCreator<>();

        phenotypeCreationTableCreator.addColumn("Marker Combination", "markerCombination", 0.3);
        phenotypeCreationTableCreator.addColumn("Phenotype Name", "phenotypeTextField", 0.5);
        phenotypeCreationTableCreator.addColumn("Create as Child", "selectedAsChildCheckBox", 0.2);

        for (int i=0; i < 4; i ++) {
            phenotypeCreationTableCreator.addRow(new PhenotypeCreationTableEntry("", ""));
        }
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

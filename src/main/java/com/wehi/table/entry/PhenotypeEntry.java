package com.wehi.table.entry;

import com.wehi.chart.CytometryChart;
import com.wehi.ManualGatingWindow;

import com.wehi.table.wrapper.AxisTableWrapper;
import com.wehi.table.wrapper.ChildPhenotypeTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;



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

    private String splitMarkerOne = "";
    private String splitMarkerTwo = "";

    private ChildPhenotypeTableWrapper childPhenotypeTableWrapper;
    private AxisTableWrapper axisTableWrapper;

    private static String title = "Manual Gating";
    /* We will have a Column for each phenotype */

    private ChildPhenotypeTableEntry.MARKER_COMBINATION combination = ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH;
    // ComboBox to select the number of dimensions
    private SplitPane pane;




    private CytometryChart cytometryChart;


    public PhenotypeEntry(Collection<PathObject> cells, String phenotypeName,
                          ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers,
                          ObservableList<String> markers, ObservableList<String> measurements, Stage stage
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.positiveMarkers = positiveMarkers;
        this.negativeMarkers = negativeMarkers;

        this.markers = markers;
        this.measurements = measurements;
        this.childPhenotypeTableWrapper = new ChildPhenotypeTableWrapper();
        this.axisTableWrapper = new AxisTableWrapper(markers, measurements);
        createPane(stage);
    }

    public PhenotypeEntry(Collection<PathObject> cells, String phenotypeName,
                          ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers,
                          ObservableList<String> markers, ObservableList<String> measurements, Stage stage,
                          String splitPositive,
                          String splitNegative,
                          ChildPhenotypeTableEntry.MARKER_COMBINATION combination
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.positiveMarkers = positiveMarkers;
        this.negativeMarkers = negativeMarkers;

        this.markers = markers;
        this.measurements = measurements;
        this.splitMarkerOne = splitPositive;
        this.splitMarkerTwo = splitNegative;
        this.combination = combination;
        this.childPhenotypeTableWrapper = new ChildPhenotypeTableWrapper();
        this.axisTableWrapper = new AxisTableWrapper(markers, measurements);
        createPane(stage);
    }


    public SplitPane getSplitPane(){
        return pane;
    }

    public SplitPane createPane(Stage stage){
        pane = new SplitPane();

        axisTableWrapper.addObservers(childPhenotypeTableWrapper);
        axisTableWrapper.createSetOnAction();


        /* Graph */
        initialiseCytometryChart(stage);
        SplitPane loadChart = new SplitPane(
                ManualGatingWindow.createColumn(
                axisTableWrapper.getTable(),
                initialisePlotButton()),
                childPhenotypeTableWrapper.getTable()
        );
        loadChart.setOrientation(Orientation.VERTICAL);


        /* Create column on the right */
        pane.setOrientation(Orientation.VERTICAL);
        pane.getItems().addAll(
                loadChart,
                cytometryChart.getPane()
        );

        return pane;
    }


    public void updateNames(ArrayList<PhenotypeEntry> phenotypes){

        for (ChildPhenotypeTableEntry row : childPhenotypeTableWrapper.getItems()){
            for (PhenotypeEntry phenotype : phenotypes){
                if (row.getMarkerCombination() == phenotype.getCombination()){
                    if (row.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE ||
                    row.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE) {
                        row.setName(phenotype.getPhenotypeName());
                        row.selectedAsChildCheckBox();
                        break;
                    } else {
                        if (phenotype.getPositiveMarkers().contains(row.getMarkerOne())){
                            row.setName(phenotype.getPhenotypeName());
                            row.selectedAsChildCheckBox();
                            break;
                        }
                    }
                }
            }
        }
    }



    /* ******************** The behaviour of the nodes which exists in other classes ******************************* */
    // The behaviour for the ComboBoxes in the AxisTableEntry to set the marker and measurement names


    // The behaviour of the Sliders in the ChartWrapper to get the threshold values for each of the markers
    private void initialiseCytometryChart(Stage stage){
        cytometryChart = new CytometryChart(stage);

        cytometryChart.getXSlider().valueProperty().addListener(
                (arg0, oldValue, newValue) -> {
                    axisTableWrapper.setXThreshold(newValue.doubleValue(), cytometryChart.getXSlider().getMin());
                    if (!childPhenotypeTableWrapper.isEmpty()) {
                        childPhenotypeTableWrapper.updateXThreshold(axisTableWrapper.getXThreshold());
                    }
                }
        );
        cytometryChart.getYSlider().valueProperty().addListener(
                (arg0, oldValue, newValue) -> {
                    axisTableWrapper.setYThreshold(newValue.doubleValue(), cytometryChart.getYSlider().getMin());
                    if (!childPhenotypeTableWrapper.isEmpty()) {
                        childPhenotypeTableWrapper.updateYThreshold(axisTableWrapper.getYThreshold());
                    }
                }
        );
       axisTableWrapper.setXSliderListener(cytometryChart.getXSlider());
       axisTableWrapper.setYSliderListener(cytometryChart.getYSlider());
    }


    private HBox initialisePlotButton(){

        Button plotGraphButton = new Button("Plot Density Chart");
        plotGraphButton.setOnAction(e -> {
            if (axisTableWrapper.getXAxisFullMeasurementName()!=null && axisTableWrapper.getYAxisFullMeasurementName()!=null) {
                cytometryChart.updateAxisLabels(axisTableWrapper.getXAxisFullMeasurementName(), axisTableWrapper.getYAxisFullMeasurementName());
                cytometryChart.populateScatterChartHistogram(cells, axisTableWrapper.getXAxisFullMeasurementName(), axisTableWrapper.getYAxisFullMeasurementName(), 1);
            } else{
                Dialogs.showErrorMessage(title, "Please select the marker and the measurement to be used.");
            }

        });

        return new HBox(plotGraphButton);
    }

    public void setChildPhenotypeThresholds(){
        childPhenotypeTableWrapper.updateXThreshold(axisTableWrapper.getXThreshold());
        childPhenotypeTableWrapper.updateYThreshold(axisTableWrapper.getYThreshold());
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
        return negativeMarkers.toString().substring(1, negativeMarkers.toString().length()-1);
    }

    /**
     * Assigns the Phenotype name to the PathClass for each of the cells. Creates a column in the cell measurement
     * list containing boolean values for each of the markers it is positive for.
     */
    public void assignCellPhenotypes(){

    }


    public ChildPhenotypeTableWrapper getChildPhenotypeTableWrapper() {
        return childPhenotypeTableWrapper;
    }


    public double getXAxisThreshold(){
        return axisTableWrapper.getXThreshold();
    }

    public double getYAxisThreshold(){
        return axisTableWrapper.getYThreshold();
    }

    public String getXAxisMarkerMeasurementName(){
        return axisTableWrapper.getXAxisFullMeasurementName();
    }

    public String getYAxisMarkerMeasurementName() {
        return axisTableWrapper.getYAxisFullMeasurementName();
    }

    public Slider getXAxisSlider(){
        return cytometryChart.getXSlider();
    }

    public Slider getYAxisSlider(){
        return cytometryChart.getYSlider();
    }

    public Collection<PathObject> getCells(){
        return cells;
    }

    public void setCells(Collection<PathObject> cells){
        this.cells = cells;
    }


    public AxisTableEntry getXAxis() {
        return axisTableWrapper.getXAxis();
    }

    public AxisTableEntry getYAxis() {
        return axisTableWrapper.getYAxis();
    }

    public String getXAxisMarkerName(){
        return axisTableWrapper.getXAxis().getMarkerName();
    }

    public String getXAxisMeasurementName(){
        return axisTableWrapper.getXAxis().getMeasurementName();
    }

    public String getYAxisMarkerName(){
        return axisTableWrapper.getYAxis().getMarkerName();
    }

    public String getYAxisMeasurementName(){
        return axisTableWrapper.getYAxis().getMeasurementName();
    }

    public String getSplitMarkerOne() {
        return splitMarkerOne;
    }

    public String getSplitMarkerTwo(){
        return splitMarkerTwo;
    }

    public void setPhenotypeName(String name){
        this.phenotypeName = name;
    }


    public void setYAxisMarkerName(String markerName){
        axisTableWrapper.getYAxis().setMarkerName(markerName);
    }

    public void setXAxisMarkerName(String markerName){
        axisTableWrapper.getXAxis().setMarkerName(markerName);
    }

    public void setYAxisMeasurementName(String measurementName){
        axisTableWrapper.getYAxis().setMeasurementName(measurementName);
    }

    public void setXAxisMeasurementName(String measurementName){
        axisTableWrapper.getXAxis().setMeasurementName(measurementName);
    }

    public void setXAxisThreshold(double unLoggedThreshold){
        cytometryChart.getXSlider().setValue(Math.log(unLoggedThreshold));
    }

    public void setYAxisThreshold(double unLoggedThreshold){
        cytometryChart.getYSlider().setValue(Math.log(unLoggedThreshold));
    }

    public void refreshChildPhenotypeTable(){
        axisTableWrapper.setOnAction();
    }

    public ChildPhenotypeTableEntry.MARKER_COMBINATION getCombination(){
        return combination;
    }

}

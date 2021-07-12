package com.wehi.table.entry;

import com.wehi.chart.CytometryChart;
import com.wehi.ManualGatingWindow;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.wrapper.AxisTableWrapper;
import com.wehi.table.wrapper.ChildPhenotypeTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Represents the entry for the phenotype hierarchy tree table. Contains all the information needed in order to
 * perform manual gating and phenotyping of the cells.
 */
public class PhenotypeEntry implements Colorable {

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

    private final ChildPhenotypeTableWrapper childPhenotypeTableWrapper;
    private final AxisTableWrapper axisTableWrapper;

    private static String title = "Manual Gating";
    /* We will have a Column for each phenotype */

    private ChildPhenotypeTableEntry.MARKER_COMBINATION combination = ChildPhenotypeTableEntry.MARKER_COMBINATION.ONE_OF_EACH;
    // ComboBox to select the number of dimensions
    private SplitPane pane;

    private Stage stage;

    private TreeItem<PhenotypeEntry> treeItem;
    private ArrayList<PhenotypeEntry> childPhenotypes;


    private CytometryChart cytometryChart;

    private Label count;

    private Color color;
    private boolean isHidden;

    public PhenotypeEntry(Collection<PathObject> cells, String phenotypeName,
                          ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers,
                          ObservableList<String> markers, ObservableList<String> measurements, Stage stage,
                          boolean createTreeItem
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.positiveMarkers = positiveMarkers;
        this.negativeMarkers = negativeMarkers;
        childPhenotypes = new ArrayList<>();
        this.markers = markers;
        this.measurements = measurements;
        this.childPhenotypeTableWrapper = new ChildPhenotypeTableWrapper();
        this.axisTableWrapper = new AxisTableWrapper(markers, measurements);
        this.stage = stage;
        if (createTreeItem) {
            this.treeItem = new TreeItem<>(this);
        }
        createPane();
    }

    public PhenotypeEntry(Collection<PathObject> cells, String phenotypeName,
                          ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers,
                          ObservableList<String> markers, ObservableList<String> measurements, Stage stage,
                          String splitPositive,
                          String splitNegative,
                          ChildPhenotypeTableEntry.MARKER_COMBINATION combination,
                          boolean createTreeItem
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.positiveMarkers = positiveMarkers;
        this.negativeMarkers = negativeMarkers;
        childPhenotypes = new ArrayList<>();
        this.markers = markers;
        this.measurements = measurements;
        this.splitMarkerOne = splitPositive;
        this.splitMarkerTwo = splitNegative;
        this.combination = combination;
        this.childPhenotypeTableWrapper = new ChildPhenotypeTableWrapper();
        this.axisTableWrapper = new AxisTableWrapper(markers, measurements);
        this.stage = stage;

        if (createTreeItem) {
            this.treeItem = new TreeItem<>(this);
        }
        createPane();
    }


    public SplitPane getSplitPane(){
        return pane;
    }

    public SplitPane createPane(){
        pane = new SplitPane();

        axisTableWrapper.addObservers(childPhenotypeTableWrapper);
        axisTableWrapper.createSetOnAction();

        count = new Label(phenotypeName + " count: " + cells.size());

        /* Graph */
        initialiseCytometryChart();
        SplitPane loadChart = new SplitPane(
                ManualGatingWindow.createColumn(
                axisTableWrapper.getTable(),
                initialisePlotButton()),
                childPhenotypeTableWrapper.getTable(),
                count
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






    /* ******************** The behaviour of the nodes which exists in other classes ******************************* */
    // The behaviour for the ComboBoxes in the AxisTableEntry to set the marker and measurement names


    // The behaviour of the Sliders in the ChartWrapper to get the threshold values for each of the markers
    private void initialiseCytometryChart(){
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
                plotCytometryChart();
            } else{
                Dialogs.showErrorMessage(title, "Please select the marker and the measurement to be used.");
            }
        });
        return new HBox(plotGraphButton);
    }

    private void plotCytometryChart(){
        if (axisTableWrapper.getXAxisFullMeasurementName()!=null && axisTableWrapper.getYAxisFullMeasurementName()!=null) {
            cytometryChart.updateAxisLabels(axisTableWrapper.getXAxisFullMeasurementName(), axisTableWrapper.getYAxisFullMeasurementName());
            cytometryChart.populateScatterChartHistogram(cells, axisTableWrapper.getXAxisFullMeasurementName(), axisTableWrapper.getYAxisFullMeasurementName(), 1);
        }
    }

    //TODO: Must change this to observer
    public void setChildPhenotypeThresholds(){
        childPhenotypeTableWrapper.updateXThreshold(axisTableWrapper.getXThreshold());
        childPhenotypeTableWrapper.updateYThreshold(axisTableWrapper.getYThreshold());
    }


    /**
     * Action taken upon pressing the button applyThreshold
     */
    public void createPhenotypes(){
        // List of new phenotypes
//        treeItem.getChildren().clear();
        treeItem.getChildren().clear();
        childPhenotypes = new ArrayList<>();
        setChildPhenotypeThresholds();

        for (ChildPhenotypeTableEntry entry : childPhenotypeTableWrapper.getItems()){
            if (entry.getIsSelected() && entry.getPhenotypeName() != null) {
                Collection<PathObject> filteredCells = entry.filterCells(cells);
                ArrayList<String> newPositiveMarkers = entry.getNewPositiveMarkers(positiveMarkers);
                ArrayList<String> newNegativeMarkers = entry.getNewNegativeMarkers(negativeMarkers);

                PhenotypeEntry newPhenotype = new PhenotypeEntry(
                        filteredCells,
                        entry.getPhenotypeName(),
                        newPositiveMarkers,
                        newNegativeMarkers,
                        markers,
                        measurements,
                        stage,
                        entry.getMarkerOne(),
                        entry.getMarkerTwo(),
                        entry.getMarkerCombination(),
                        false
                );

                newPhenotype.getXAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(newPhenotype.getXAxis()));
                newPhenotype.getYAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(newPhenotype.getYAxis()));
                childPhenotypes.add(newPhenotype);
                PathClassHandler.restorePathClass();
                PathClassHandler.setCellPathClass(filteredCells, entry.getPhenotypeName());
                PathClassHandler.storeClassification();
            }
        }
        setChildren(childPhenotypes);
    }


    // TODO: Should fix how the Child phenotype thresholds are set to observer-pattern
    public void updateSubPhenotypes(){
        setChildPhenotypeThresholds();
        PathClassHandler.restorePathClass();

        for (ChildPhenotypeTableEntry entry : childPhenotypeTableWrapper.getItems()) {
            if (entry.getIsSelected() && entry.getPhenotypeName() != null) {
                for (PhenotypeEntry childPhenotype : childPhenotypes){

                    if (!entry.getMarkerOne().equals(childPhenotype.getSplitMarkerOne()) ||
                            !entry.getMarkerTwo().equals(childPhenotype.getSplitMarkerTwo())){
                        continue;
                    }


                    if (entry.getMarkerCombination() == childPhenotype.getCombination()) {
                        Collection<PathObject> filteredCells = entry.filterCellsAndUpdatePathClass(cells, childPhenotype.getName());
                        childPhenotype.setPhenotypeName(entry.getPhenotypeName());
                        //set CellPath class after updating tree
                        childPhenotype.setCells(filteredCells);
                        PathClassHandler.setCellPathClass(filteredCells, entry.getPhenotypeName());
                        PathClassHandler.storeClassification();

                        childPhenotype.getXAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(childPhenotype.getXAxis()));
                        childPhenotype.getYAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(childPhenotype.getYAxis()));
                    }
                }
            }
        }
    }



    // ************************ Getter and Setters ***************** //

    public void setChildren(ArrayList<PhenotypeEntry> list){
        this.childPhenotypes = list;
        updateNames();
        for (PhenotypeEntry child : list){
            TreeItem<PhenotypeEntry> childTreeItem = new TreeItem<>(child);
            treeItem.getChildren().add(childTreeItem);
            child.setTreeItem(childTreeItem);
        }
    }

    public void addChild(PhenotypeEntry child){
        this.childPhenotypes.add(child);
        updateNames();
        TreeItem<PhenotypeEntry> childTreeItem = new TreeItem<>(child);
        treeItem.getChildren().add(childTreeItem);
        child.setTreeItem(childTreeItem);
    }


    private void updateNames(){

        for (ChildPhenotypeTableEntry row : childPhenotypeTableWrapper.getItems()){
            for (PhenotypeEntry phenotype : childPhenotypes){
                if (row.getMarkerCombination() == phenotype.getCombination()){
                    if (row.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE ||
                            row.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE) {
                        row.setName(phenotype.getName());
                        row.selectedAsChildCheckBox();
                        break;
                    } else {
                        if (phenotype.getPositiveMarkers().contains(row.getMarkerOne())){
                            row.setName(phenotype.getName());
                            row.selectedAsChildCheckBox();
                            break;
                        }
                    }
                }
            }
        }
    }


    public static void updatePhenotypeTree(PhenotypeEntry phenotype){
        phenotype.updateSubPhenotypes();
        for (PhenotypeEntry subPhenotype : phenotype.getChildren()){
            updatePhenotypeTree(subPhenotype);
        }
    }

    public static void plotAllChartPhenotypeTree(PhenotypeEntry phenotype){
        phenotype.plotCytometryChart();
        for (PhenotypeEntry subPhenotype : phenotype.getChildren()){
            plotAllChartPhenotypeTree(subPhenotype);
        }
    }

    public ArrayList<PhenotypeEntry> getChildren(){
        return childPhenotypes;
    }

    public void setTreeItem(TreeItem<PhenotypeEntry> treeItem){
        this.treeItem = treeItem;
    }

    public TreeItem<PhenotypeEntry> getTreeItem(){
        return treeItem;
    }


    public SplitPane getPane() {
        return pane;
    }

    @Override
    public String getName() {
        return phenotypeName;
    }

    @Override
    public boolean isHidden(){
        return isHidden;
    }

    @Override
    public Color getColor(){
        return color;
    }

    @Override
    public void setColor(Color color){
        this.color = color;


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
        count.setText(phenotypeName + " count: " + cells.size());
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

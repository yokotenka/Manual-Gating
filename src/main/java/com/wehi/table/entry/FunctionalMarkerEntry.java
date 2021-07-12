package com.wehi.table.entry;

import com.wehi.ManualGatingWindow;
import com.wehi.chart.HistogramChart;
import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.wrapper.FunctionalSubPhenotypeTableWrapper;
import com.wehi.table.wrapper.SingleAxisTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.stage.Stage;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionalMarkerEntry {

    private Collection<PathObject> cells;
    private String marker;
    private String name;
    private FunctionalSubPhenotypeTableWrapper functionalPhenotypeTableWrapper;
    private SingleAxisTableWrapper singleAxisTableWrapper;

    private HistogramChart histogramWrapper;

    private Stage stage;

    private SplitPane splitPane;

    private Label count;
    private Button plotChartButton;

    private ArrayList<FunctionalMarkerEntry> childPhenotypes;

    private TreeItem<FunctionalMarkerEntry> treeItem;

    private ObservableList<String> markers;
    private ObservableList<String> measurements;

    private boolean isDisplayable = true;

    private ArrayList<FunctionalMarkerEntry> kids = new ArrayList<>();

    private ChildPhenotypeTableEntry.MARKER_COMBINATION combination;



    public FunctionalMarkerEntry(
            Collection<PathObject> cells,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Stage stage
            ){
        this.treeItem = new TreeItem<>(this);
        this.cells = cells;
        this.name = "";
        this.marker = "";

        this.functionalPhenotypeTableWrapper = new FunctionalSubPhenotypeTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
        this.stage = stage;
        if (isDisplayable) {
            createPane();
        }
    }
    public FunctionalMarkerEntry(
            Collection<PathObject> cells,
            String marker,
            String phenotypeName,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            boolean isDisplayable,
            ChildPhenotypeTableEntry.MARKER_COMBINATION combo,
            Stage stage
    ){
        this.treeItem = new TreeItem<>(this);
        this.cells = cells;
        this.name = phenotypeName;
        this.marker = marker;
        this.combination = combo;
        this.markers = markers;
        this.measurements = measurements;

        this.functionalPhenotypeTableWrapper = new FunctionalSubPhenotypeTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
        this.isDisplayable = isDisplayable;
        this.stage = stage;

        if (isDisplayable) {
            createPane();
        }
    }

    public FunctionalMarkerEntry(
            Collection<PathObject> cells,
            String phenotypeName,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            Stage stage
    ){
        this.treeItem = new TreeItem<>(this);
        this.cells = cells;
        this.name = phenotypeName;
//        this.marker = marker;

        this.markers = markers;
        this.measurements = measurements;

        this.functionalPhenotypeTableWrapper = new FunctionalSubPhenotypeTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
        this.isDisplayable = isDisplayable;
        this.stage = stage;
        if (isDisplayable) {
            createPane();
        }
    }

    private SplitPane createPane(){
        splitPane = new SplitPane();

        singleAxisTableWrapper.addObservers(functionalPhenotypeTableWrapper);
        singleAxisTableWrapper.createSetOnAction();

        plotChartButton();
//        count = new Label(phenotypeName + " count: " + cells.size());

        /* Graph */
        initialiseChart();
        singleAxisTableWrapper.addSlider(histogramWrapper.getSliderThreshold());
        SplitPane loadChart = new SplitPane(
                ManualGatingWindow.createColumn(
                        singleAxisTableWrapper.getTable(),

                        functionalPhenotypeTableWrapper.getTable(),
                        plotChartButton
//                        count
                )
            );
        loadChart.setOrientation(Orientation.VERTICAL);



        /* Create column on the right */
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(
                loadChart,
                histogramWrapper.getGroup()
        );
        getXAxisSlider().valueProperty().addListener((v,o,n) -> PathClassHandler.previewThreshold(getXAxis()));
        return splitPane;
    }

    public SplitPane getSplitPane(){
        if(splitPane == null){
            createPane();
        }
        return splitPane;
    }


    public void initialiseChart(){
        histogramWrapper = new HistogramChart(stage);

        histogramWrapper.getSliderThreshold().valueProperty().addListener(
                (arg0, oldValue, newValue) -> {
                    singleAxisTableWrapper.setXThreshold(newValue.doubleValue(), histogramWrapper.getSliderThreshold().getMin());
                    if (!functionalPhenotypeTableWrapper.isEmpty()) {
                        functionalPhenotypeTableWrapper.updateXThreshold(singleAxisTableWrapper.getThreshold());
                    }
                }
        );
    }

    public void plotChartButton(){
        plotChartButton = new Button("Plot");

        // plot the chart when button is pressed.
        plotChartButton.setOnAction(e -> {
            plotChart();
        });
    }

    public void plotChart(){
        histogramWrapper.updateAxisLabel(singleAxisTableWrapper.getFullMeasurementName());
        histogramWrapper.populateChart(cells);
    }

    public void setChildPhenotypeThresholds() {
        functionalPhenotypeTableWrapper.updateXThreshold(singleAxisTableWrapper.getThreshold());
    }

    public void createPhenotypes(){
        // List of new phenotypes
//        treeItem.getChildren().clear();

        setName(this.getMarker());
        treeItem.getChildren().clear();
        childPhenotypes = new ArrayList<>();
        setChildPhenotypeThresholds();

        if (kids.isEmpty()) {
            for (ChildPhenotypeTableEntry entry : functionalPhenotypeTableWrapper.getItems()) {
                if (entry.getIsSelected() && entry.getPhenotypeName() != null) {
                    PathClassHandler.restorePathClass();
                    Collection<PathObject> filteredCells = entry.filterCells(cells);
//

                    PathClassHandler.setCellPathClass(filteredCells, entry.getPhenotypeName());
                    PathClassHandler.storeClassification();

                    FunctionalMarkerEntry kid = new FunctionalMarkerEntry(cells,
                            entry.getMarkerOne(),
                            entry.getPhenotypeName(),
                            markers,
                            measurements,
                            false,
                            entry.getMarkerCombination(),
                            stage);

                    treeItem.getChildren().add(new TreeItem<>(kid));
                    kids.add(kid);
                }
            }
        } else {
            for (ChildPhenotypeTableEntry entry : functionalPhenotypeTableWrapper.getItems()) {
                for (FunctionalMarkerEntry kid : kids)
                    if (entry.getIsSelected() && entry.getPhenotypeName() != null && kid.combination == entry.getMarkerCombination()) {
                        PathClassHandler.restorePathClass();
                        Collection<PathObject> filteredCells = entry.filterCells(cells, kid.getName());

                        PathClassHandler.setCellPathClass(filteredCells, entry.getPhenotypeName());
                        PathClassHandler.storeClassification();

                        FunctionalMarkerEntry newKid = new FunctionalMarkerEntry(cells,
                                entry.getMarkerOne(),
                                entry.getPhenotypeName(),
                                markers,
                                measurements,
                                false,
                                entry.getMarkerCombination(),
                                stage);

                        treeItem.getChildren().add(new TreeItem<>(newKid));
                        kids.remove(kid);
                        kids.add(newKid);
                    }
            }
        }
    }

    public void removeAllKids(){
        for (FunctionalMarkerEntry kid : kids){
            for (PathObject cell : cells) {
                PathClassHandler.removeNoLongerPositive(cell, kid.getName());
            }
        }
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getMarker(){
        return singleAxisTableWrapper.getMarker();
    }

    public String getMeasurement(){
        return singleAxisTableWrapper.getPartialMeasurementName();
    }

    public TreeItem<FunctionalMarkerEntry> getTreeItem(){
        return treeItem;
    }

    public boolean isDisplayable(){
        return isDisplayable;
    }

    public Slider getXAxisSlider(){
        return histogramWrapper.getSliderThreshold();
    }

    public AxisTableEntry getXAxis(){
        return singleAxisTableWrapper.getAxis();
    }

    public String getFullMeasurementName(){
        return getXAxis().getFullMeasurementName();
    }

    public double getThreshold(){
        return getXAxis().getThreshold();
    }

    public String getAboveThreshold(){
        return functionalPhenotypeTableWrapper.getPositiveName();
    }

    public String getBelowThreshold(){
        return functionalPhenotypeTableWrapper.getNegativeName();
    }

    public void setAboveThreshold(String above){
        functionalPhenotypeTableWrapper.setPositiveName(above);
    }

    public void setBelowThreshold(String below){
        functionalPhenotypeTableWrapper.setNegativeName(below);
    }

    public void setThreshold(double threshold){
        singleAxisTableWrapper.setThreshold(threshold);

    }

    public void setMarker(String marker){
        singleAxisTableWrapper.setMarkerName(marker);
    }

    public void setMeasurement(String measurement){
        singleAxisTableWrapper.setMeasurementName(measurement);
    }

    public void selectAbove(){
        functionalPhenotypeTableWrapper.selectPositive();
    }

    public void selectBelow(){
        functionalPhenotypeTableWrapper.selectNegative();
    }

    public void updateFunctionalChildTable(){
        singleAxisTableWrapper.notifyObservers();
    }
}

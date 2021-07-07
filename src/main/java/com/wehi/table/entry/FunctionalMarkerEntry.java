package com.wehi.table.entry;

import com.wehi.ManualGatingWindow;
import com.wehi.chart.HistogramChart;
import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.wrapper.FunctionalPhenotypeOptionTableWrapper;
import com.wehi.table.wrapper.SingleAxisTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionalMarkerEntry {

    private Collection<PathObject> cells;
    private String marker;
    private String name;
    private FunctionalPhenotypeOptionTableWrapper functionalPhenotypeTableWrapper;
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

        this.functionalPhenotypeTableWrapper = new FunctionalPhenotypeOptionTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
        this.stage = stage;
    }
    public FunctionalMarkerEntry(
            Collection<PathObject> cells,
            String marker,
            String phenotypeName,
            ObservableList<String> markers,
            ObservableList<String> measurements,
            boolean isDisplayable,
            Stage stage
    ){
        this.treeItem = new TreeItem<>(this);
        this.cells = cells;
        this.name = phenotypeName;
        this.marker = marker;

        this.markers = markers;
        this.measurements = measurements;

        this.functionalPhenotypeTableWrapper = new FunctionalPhenotypeOptionTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
        this.isDisplayable = isDisplayable;
        this.stage = stage;
    }

    public SplitPane createPane(){
        splitPane = new SplitPane();

        singleAxisTableWrapper.addObservers(functionalPhenotypeTableWrapper);
        singleAxisTableWrapper.createSetOnAction();

        plotChartButton();
//        count = new Label(phenotypeName + " count: " + cells.size());

        /* Graph */
        initialiseChart();
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
                        functionalPhenotypeTableWrapper.updateXThreshold(singleAxisTableWrapper.getXThreshold());
                    }
                }
        );
    }

    public void plotChartButton(){
        plotChartButton = new Button("Plot");

        // plot the chart when button is pressed.
        plotChartButton.setOnAction(e -> {
            histogramWrapper.updateAxisLabel(singleAxisTableWrapper.getXAxisFullMeasurementName());
            histogramWrapper.populateChart(cells);
        });
    }

    public void setChildPhenotypeThresholds() {
        functionalPhenotypeTableWrapper.updateXThreshold(singleAxisTableWrapper.getXThreshold());
        Dialogs.showInfoNotification("", String.valueOf(singleAxisTableWrapper.getXThreshold()));
    }

    public void createPhenotypes(){
        // List of new phenotypes
//        treeItem.getChildren().clear();
        treeItem.getChildren().clear();
        childPhenotypes = new ArrayList<>();
        setChildPhenotypeThresholds();

        for (ChildPhenotypeTableEntry entry : functionalPhenotypeTableWrapper.getItems()){
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
                        stage);

                treeItem.getChildren().add(new TreeItem<>(kid));

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

    public TreeItem<FunctionalMarkerEntry> getTreeItem(){
        return treeItem;
    }

    public boolean isDisplayable(){
        return isDisplayable;
    }
}

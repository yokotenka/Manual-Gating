package com.wehi.table.entry;

import com.wehi.ManualGatingWindow;
import com.wehi.chart.HistogramWrapper;
import com.wehi.table.wrapper.FunctionalPhenotypeOptionTableWrapper;
import com.wehi.table.wrapper.SingleAxisTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import qupath.lib.objects.PathObject;

import java.util.Collection;

public class FunctionalPhenotypeEntry {

    private Collection<PathObject> cells;
    private String marker;
    private String phenotypeName;
    private FunctionalPhenotypeOptionTableWrapper functionalPhenotypeTableWrapper;
    private SingleAxisTableWrapper singleAxisTableWrapper;

    private HistogramWrapper histogramWrapper;


    private SplitPane splitPane;

    private Label count;
    private Button plotChartButton;

    public FunctionalPhenotypeEntry(
            Collection<PathObject> cells,
                                    ObservableList<String> markers,
                                    ObservableList<String> measurements){

        this.cells = cells;
        this.phenotypeName = "";
        this.marker = "";

        this.functionalPhenotypeTableWrapper = new FunctionalPhenotypeOptionTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);
    }
    public FunctionalPhenotypeEntry(
            Collection<PathObject> cells,
            String marker,
            String phenotypeName,
            ObservableList<String> markers,
            ObservableList<String> measurements
    ){
        this.cells = cells;
        this.phenotypeName = phenotypeName;
        this.marker = marker;

        this.functionalPhenotypeTableWrapper = new FunctionalPhenotypeOptionTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);


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
                histogramWrapper.getPane()
        );

        return splitPane;
    }


    public void initialiseChart(){
        histogramWrapper = new HistogramWrapper();

    }

    public void plotChartButton(){
        plotChartButton = new Button("Plot");

        // plot the chart when button is pressed.
        plotChartButton.setOnAction(e -> {
            histogramWrapper.updateAxisLabel(singleAxisTableWrapper.getXAxisFullMeasurementName());
            histogramWrapper.populateChart(cells);
        });
    }

}

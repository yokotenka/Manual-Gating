package com.wehi.table.entry;

import com.wehi.ManualGatingWindow;
import com.wehi.chart.HistogramWrapper;
import com.wehi.table.wrapper.FunctionalPhenotypeTableWrapper;
import com.wehi.table.wrapper.SingleAxisTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.chart.Chart;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import qupath.lib.gui.charts.HistogramPanelFX;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionalPhenotypeEntry {

    private Collection<PathObject> cells;
    private String marker;
    private String phenotypeName;
    private FunctionalPhenotypeTableWrapper functionalPhenotypeTableWrapper;
    private SingleAxisTableWrapper singleAxisTableWrapper;

    private HistogramWrapper histogramWrapper;


    private SplitPane splitPane;

    private Label count;

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

        this.functionalPhenotypeTableWrapper = new FunctionalPhenotypeTableWrapper();
        this.singleAxisTableWrapper = new SingleAxisTableWrapper(markers, measurements);


    }

    public SplitPane createPane(){
        splitPane = new SplitPane();

        singleAxisTableWrapper.addObservers(functionalPhenotypeTableWrapper);
        singleAxisTableWrapper.createSetOnAction();

        count = new Label(phenotypeName + " count: " + cells.size());

        /* Graph */
        initialiseChart();
        SplitPane loadChart = new SplitPane(
                ManualGatingWindow.createColumn(
                        singleAxisTableWrapper.getTable(),
//                        initialisePlotButton()),
                        functionalPhenotypeTableWrapper.getTable(),
                        count
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

}

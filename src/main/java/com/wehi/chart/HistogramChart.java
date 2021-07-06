package com.wehi.chart;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.analysis.stats.Histogram;
import qupath.lib.common.ColorTools;
import qupath.lib.gui.charts.HistogramPanelFX;
import qupath.lib.objects.PathObject;

import java.util.Collection;

import static com.wehi.JavaFXHelpers.createVBox;

public class HistogramChart extends CustomChart{

    private HistogramPanelFX histogramPane;
    private HistogramPanelFX.ThresholdedChartWrapper chartWrapper;

    private String measurement;
    private final Slider sliderThreshold = new Slider();

    private Pane pane;
    private Stage stage;

    private GridPane group;

    public HistogramChart(Stage stage){
        pane = new Pane();
        initialiseChart();
        initialiseSlider();
        chartWrapper.addThreshold(sliderThreshold.valueProperty(),  Color.rgb(0, 0, 0, 0.2));
        pane.getChildren().add(sliderThreshold);
        this.stage = stage;
        initialisePane();
    }

    public void initialiseSlider(){
        Chart chart = histogramPane.getChart();
        NumberAxis xAxis = (NumberAxis) histogramPane.getChart().getXAxis();
        NumberAxis yAxis = (NumberAxis) histogramPane.getChart().getYAxis();
        sliderThreshold.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
                    double xAxisPosition = xAxis.getDisplayPosition(xAxis.getLowerBound());
                    Point2D positionInScene = xAxis.localToScene(xAxisPosition, 0);
                    return pane.sceneToLocal(positionInScene).getX();
                },
                chart.widthProperty(),
                chart.heightProperty(),
                chart.boundsInParentProperty(),
                xAxis.lowerBoundProperty(),
                xAxis.upperBoundProperty(),
                xAxis.autoRangingProperty(),
                xAxis.scaleProperty(),
                yAxis.autoRangingProperty(),
                yAxis.lowerBoundProperty(),
                yAxis.upperBoundProperty()
        ));
        sliderThreshold.prefWidthProperty().bind(xAxis.widthProperty());



    }

    public void initialiseChart(){
        histogramPane = new HistogramPanelFX();
        chartWrapper = new HistogramPanelFX.ThresholdedChartWrapper(histogramPane.getChart());
        histogramPane.getChart().setVisible(true);
//        chartWrapper.getPane().setPrefSize(200, 80);

        histogramPane.getChart().prefWidthProperty().bind(pane.widthProperty());
        histogramPane.getChart().prefHeightProperty().bind(pane.heightProperty());
        pane.getChildren().add(chartWrapper.getPane());
//        double[] dummy = {0};
//        var stats = new DescriptiveStatistics(dummy);
//        var histogram = new Histogram(dummy, 100, stats.getMin(), stats.getMax());
//        histogramPane.getHistogramData().setAll(HistogramPanelFX.createHistogramData(histogram, false, (Integer) null));
    }

    public void updateAxisLabel(String measurement){
        this.measurement = measurement;
    }

    public void populateChart(Collection<PathObject> cells) {
        if (measurement == null || cells.isEmpty()) {
            sliderThreshold.setMin(0);
            sliderThreshold.setMax(1);
            sliderThreshold.setValue(0);
            return;
        }
        double[] allValues = cells.stream().mapToDouble(p -> p.getMeasurementList().getMeasurementValue(measurement))
                .filter(Double::isFinite)
                .map(Math::log)
                .filter(Double::isFinite)
                .toArray();
        var stats = new DescriptiveStatistics(allValues);
        var histogram = new Histogram(allValues, 200, stats.getMin(), stats.getMax());
        histogramPane.getHistogramData().setAll(HistogramPanelFX.createHistogramData(histogram, false, (Integer) null));
//        histogramPane.getChart().getXAxis().setLowerBound();
//        double value = previousThresholds.getOrDefault(measurement, stats.getMean());
        sliderThreshold.setMin(Math.floor(stats.getMin()));
        sliderThreshold.setMax(Math.ceil(stats.getMax()));
        sliderThreshold.setValue(stats.getMin());

        NumberAxis xAxis = (NumberAxis) histogramPane.getChart().getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(Math.floor(stats.getMin()));
        xAxis.setUpperBound(Math.ceil(stats.getMax()));

//
    }

    public GridPane getGroup(){
        return  group;
    }

    public Slider getSliderThreshold(){
        return sliderThreshold;
    }

    /* *********** Initialise the pane ********* */
    private void initialisePane(){

        // get the gridPane
        group = new GridPane();
        group.add(pane, 0, 0);

        // Set the size properties
        GridPane.setFillWidth(group, true);
        GridPane.setFillHeight(group, true);
        chartWrapper.getPane().prefWidthProperty().bind(stage.widthProperty());
        chartWrapper.getPane().prefHeightProperty().bind(stage.heightProperty());
        group.prefWidthProperty().bind(stage.widthProperty());
        group.prefHeightProperty().bind(stage.heightProperty());

        // Pad
        group.setPadding(new Insets(10, 10, 10, 10));
    }
}

package com.wehi.TableTreeViewHelpers;

import com.wehi.ThresholdedScatterChartWrapper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.objects.PathObject;

import java.util.Collection;


public class CytometryChart {
    private ScatterChart<Number, Number> scatterChart;
    private LineChart<Number, Number> lineChart;
    private Stage stage;
    private GridPane group;
    private Pane pane;


    private String horizontalMeasurement;
    private String verticalMeasurement;





    private ThresholdedScatterChartWrapper chartWrapper;

    private Series<Number, Number> horizontalLine;
    private Series<Number, Number> verticalLine;

    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private String xAxisName;
    private String yAxisName;

    private boolean isTwoDimensional = true;

    public static final double MAX_LOG_INTENSITY = 6;




    // For the case where both dimensions are used
    public CytometryChart(Stage stage){

        this.stage = stage;

        initialiseScatterChart();
        initialisePane();
    }



    /* ****** Chart initialise methods ***********/


    private void initialiseScatterChart(){
        // Axis
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        // x-axis
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(MAX_LOG_INTENSITY);
        xAxis.setLowerBound(-MAX_LOG_INTENSITY);
        xAxis.setLabel(" ");

        // y-axis
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(MAX_LOG_INTENSITY);
        yAxis.setLowerBound(-MAX_LOG_INTENSITY);
        yAxis.setLabel(" ");

        // Creating the scatter chart
        scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.prefWidthProperty().bind(stage.widthProperty());
        scatterChart.prefHeightProperty().bind(stage.heightProperty());
        scatterChart.setLayoutX(20);
        scatterChart.setLayoutY(10);
    }



    private void initialisePane(){

        chartWrapper = new ThresholdedScatterChartWrapper(scatterChart);
        chartWrapper.setIsInteractive(true);
        chartWrapper.addThreshold(Color.rgb(255, 0, 0, 0.2));


        double xAxisPosition = xAxis.getDisplayPosition(xAxis.getLowerBound());
//        Point2D positionInSceneX = xAxis.localToScene(xAxisPosition, 0);

        double yAxisPosition = yAxis.getDisplayPosition(yAxis.getLowerBound());
//        Point2D positionInSceneY = yAxis.localToScene(0, yAxisPosition);



        group = new GridPane();

        group.add(chartWrapper.getPane(), 0, 0);

        GridPane.setFillWidth(group, true);
        GridPane.setFillHeight(group, true);
        chartWrapper.getPane().prefWidthProperty().bind(stage.widthProperty());
        chartWrapper.getPane().prefHeightProperty().bind(stage.heightProperty());
        group.prefWidthProperty().bind(stage.widthProperty());
        group.prefHeightProperty().bind(stage.heightProperty());
        group.setPadding(new Insets(10, 10, 10, 10));

    }

    public void populateScatterChart(Collection<PathObject> cells, String xMeasurement, String yMeasurement){
        double[] xLogMarkerIntensities = cells.stream()
                .parallel()
                .mapToDouble(p -> Math.log(p.getMeasurementList().getMeasurementValue(xMeasurement)))
                .filter(d -> !Double.isNaN(d)).toArray();

        DescriptiveStatistics xDS = new DescriptiveStatistics(xLogMarkerIntensities);
        double xMedian = xDS.getPercentile(50);
        double xIQR = xDS.getPercentile(75) - xDS.getPercentile(25);
        double xUpperBound = xDS.getMax();
        double xLowerBound = xDS.getPercentile(25) - 1.5 * xIQR;

        double[] yMarkerIntensities = cells.stream()
                .parallel()
                .mapToDouble(p -> Math.log(p.getMeasurementList().getMeasurementValue(yMeasurement)))
                .filter(d -> !Double.isNaN(d)).toArray();

        DescriptiveStatistics yDS = new DescriptiveStatistics(xLogMarkerIntensities);
        double yMedian = yDS.getPercentile(50);
        double yIQR = yDS.getPercentile(75) - yDS.getPercentile(25);
        double yUpperBound = yDS.getMax();
        double yLowerBound = yDS.getPercentile(25) - 1.5 * yIQR;

        Series<Number, Number> series = new Series<Number, Number>();

        for (PathObject cell : cells){
            if (Double.isNaN(cell.getMeasurementList().getMeasurementValue(xMeasurement))){
                cell.getMeasurementList().putMeasurement(xMeasurement, xMedian);
            }

            if (Double.isNaN(cell.getMeasurementList().getMeasurementValue(yMeasurement))){
                cell.getMeasurementList().putMeasurement(yMeasurement, yMedian);
            }


            series.getData().add(new XYChart.Data<>(
                    cell.getMeasurementList().getMeasurementValue(xMeasurement),
                    cell.getMeasurementList().getMeasurementValue(yMeasurement)
            ));
        }
        updateXBounds(xLowerBound, xUpperBound);
        updateYBounds(yLowerBound, yUpperBound);
        scatterChart.getData().add(series);
    }


    public void updateXBounds(double lowerBound, double upperBound){
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);

        chartWrapper.updateXSliderBounds(lowerBound, upperBound);
    }

    public void updateYBounds(double lowerBound, double upperBound){
        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);

        chartWrapper.updateYSliderBounds(lowerBound, upperBound);
    }


    public GridPane getPane() {
        return group;
    }

    public void updateAxisLabels(String xAxisLabel, String yAxisLabel){
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
    }


    public Slider getXSlider(){
        return chartWrapper.getHorizontalSlider();
    }

    public Slider getYSlider(){
        return chartWrapper.getVerticalSlider();
    }


    // TODO: Add in method to populate the charts
    // TODO: Add in method to label the chart axis

}
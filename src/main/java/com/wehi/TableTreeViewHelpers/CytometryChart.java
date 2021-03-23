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

        // y-axis
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(MAX_LOG_INTENSITY);
        yAxis.setLowerBound(-MAX_LOG_INTENSITY);

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
        Point2D positionInSceneX = xAxis.localToScene(xAxisPosition, 0);

        double yAxisPosition = yAxis.getDisplayPosition(yAxis.getLowerBound());
        Point2D positionInSceneY = yAxis.localToScene(0, yAxisPosition);



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

    public GridPane getPane() {
        return group;
    }

    // TODO: Add in method to populate the charts
    // TODO: Add in method to label the chart axis

}
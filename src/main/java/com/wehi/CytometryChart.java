package com.wehi;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.apache.commons.math3.util.FastMath;


public class CytometryChart {
    private ScatterChart<Number, Number> scatterChart;
    private LineChart<Number, Number> lineChart;
    private Stage stage;
    private GridPane group;
    private Pane pane;

    private String horizontalMeasurement;
    private String verticalMeasurement;

    private Slider horizontalSlider;
    private Slider verticalSlider;

    private TextField horizontalSliderValueTextField;
    private TextField verticalSliderValueTextField;

    private Series<Number, Number> horizontalLine;
    private Series<Number, Number> verticalLine;

    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private boolean isTwoDimensional = true;

    public static final double MAX_LOG_INTENSITY = 6;

    // For the case where only one dimension is chosen
    public CytometryChart(Stage stage, String horizontalMeasurement){
        this.horizontalMeasurement = horizontalMeasurement;
        this.isTwoDimensional = false;
        this.stage = stage;
        initialiseLineChart();
    }


    // For the case where both dimensions are used
    public CytometryChart(Stage stage, String horizontalMeasurement, String verticalMeasurement){
        this.horizontalMeasurement = horizontalMeasurement;
        this.verticalMeasurement = verticalMeasurement;
        this.stage = stage;

        initialiseScatterChart();
        initialiseSliders();
        initialisePane();
    }



    /* ****** Chart initialise methods ***********/

    private void initialiseLineChart() {

        // Axis
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        // x-axis
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(MAX_LOG_INTENSITY);
        xAxis.setLowerBound(-MAX_LOG_INTENSITY);
        xAxis.setLabel("Intensity of " + horizontalMeasurement);

        // y-axis
        yAxis.setAutoRanging(false);

        //creating the chart
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Density Estimation for " + horizontalMeasurement);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

    }

    private void initialiseScatterChart(){
        // Axis
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        // x-axis
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(MAX_LOG_INTENSITY);
        xAxis.setLowerBound(-MAX_LOG_INTENSITY);
        xAxis.setLabel("Intensity of " + horizontalMeasurement);

        // y-axis
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(MAX_LOG_INTENSITY);
        yAxis.setLowerBound(-MAX_LOG_INTENSITY);
        yAxis.setLabel("Intensity of " + verticalMeasurement);

        // Creating the scatter chart
        scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.prefWidthProperty().bind(stage.widthProperty());
        scatterChart.prefHeightProperty().bind(stage.heightProperty());
        scatterChart.setLayoutX(20);
        scatterChart.setLayoutY(10);


    }

    private void initialiseSliders(){

        horizontalSlider = new Slider();
        horizontalSlider.setMax(MAX_LOG_INTENSITY);
        horizontalSlider.setMin(-MAX_LOG_INTENSITY);
        horizontalSlider.setValue(0);
        horizontalSlider.prefWidthProperty().bind(scatterChart.widthProperty());
        horizontalSlider.setShowTickLabels(true);
        horizontalSlider.setShowTickMarks(true);
        horizontalSliderValueTextField = new TextField("2.71");

//        horizontalSliderValueTextField.textProperty().addListener(
//                ((observableValue, oldValue, newValue) ->
//                        horizontalSlider.setValue(Double.parseDouble(horizontalSliderValueTextField.getText()))
//                        )
//        );
//
//        horizontalSlider.valueProperty().addListener(
//                (observableValue, oldValue, newValue) ->
//                        horizontalSliderValueTextField.setText(String.valueOf(newValue.doubleValue()))
//        );
//
//        horizontalSliderValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue.matches("\\d*([\\.]\\d*)?")) {
//                horizontalSliderValueTextField.setText(oldValue);
//            }
//        });
//
//        horizontalSliderValueTextField.setOnAction(e -> {
//            if (horizontalSliderValueTextField.getText().length() > 0 && horizontalSliderValueTextField.getText().charAt(0)=='.') {
//                horizontalSliderValueTextField.setText(horizontalSliderValueTextField.getText(1, horizontalSliderValueTextField.getText().length()));
//            }
//            if (horizontalSliderValueTextField.getText().length() > 0 && horizontalSliderValueTextField.getText().charAt(horizontalSliderValueTextField.getText().length()-1)=='.'){
//                horizontalSliderValueTextField.setText(horizontalSliderValueTextField.getText(0, horizontalSliderValueTextField.getText().length()-1));
//            }
//        });

        if (isTwoDimensional){
            verticalSlider = new Slider();
            verticalSlider.setOrientation(Orientation.VERTICAL);
            verticalSlider.setMax(MAX_LOG_INTENSITY);
            verticalSlider.setMin(-MAX_LOG_INTENSITY);
            verticalSlider.setValue(0);
            verticalSlider.setShowTickMarks(true);
            verticalSlider.setShowTickLabels(true);
            verticalSlider.prefHeightProperty().bind(scatterChart.heightProperty());

            verticalSliderValueTextField = new TextField("2.71");

//            verticalSliderValueTextField.textProperty().addListener(
//                    (observableValue, oldValue, newValue) ->
//                            verticalSlider.setValue(Double.parseDouble(horizontalSliderValueTextField.getText()))
//            );
//
//            verticalSlider.valueProperty().addListener(
//                    (observableValue, oldValue, newValue) ->
//                            verticalSliderValueTextField.setText(String.valueOf(newValue.doubleValue()))
//            );
//
//            verticalSliderValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
//                if (!newValue.matches("\\d*([\\.]\\d*)?")) {
//                    verticalSliderValueTextField.setText(oldValue);
//                }
//            });
//
//            verticalSliderValueTextField.setOnAction(e -> {
//                if (verticalSliderValueTextField.getText().length() > 0 && verticalSliderValueTextField.getText().charAt(0)=='.') {
//                    verticalSliderValueTextField.setText(verticalSliderValueTextField.getText(1, verticalSliderValueTextField.getText().length()));
//                }
//                if (verticalSliderValueTextField.getText().length() > 0 && verticalSliderValueTextField.getText().charAt(verticalSliderValueTextField.getText().length()-1)=='.'){
//                    verticalSliderValueTextField.setText(verticalSliderValueTextField.getText(0, verticalSliderValueTextField.getText().length()-1));
//                }
//            });
        }
    }

    private void initialisePane(){
        group = new GridPane();

        HBox hBox = new HBox(horizontalSlider);
        hBox.setPadding(new Insets(0, 5, 0 ,50));
        group.add(hBox, 1,0);

        VBox vBox = new VBox(verticalSlider);
        vBox.setPadding(new Insets(10, 0, 50, 0));
        group.add(vBox, 0, 1);
        group.add(scatterChart, 1, 1);
        GridPane.setFillWidth(group, true);
        GridPane.setFillHeight(group, true);
        group.prefWidthProperty().bind(stage.widthProperty());
        group.prefHeightProperty().bind(stage.widthProperty());

        group.setPadding(new Insets(10, 10, 10, 10));
    }

    public GridPane getPane() {
        return group;
    }



    private Line createVerticalLine(){
        Line verticalLine  = new Line();

        return verticalLine;
    }

    private Line createHorizontalLine(){
        Line horizontalLine = new Line();

        return horizontalLine;
    }
}

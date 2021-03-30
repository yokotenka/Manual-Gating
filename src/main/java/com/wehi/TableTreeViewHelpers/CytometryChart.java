package com.wehi.TableTreeViewHelpers;

import com.wehi.ChartVisualiseHelpers.BivariateKDE;
import com.wehi.ThresholdedScatterChartWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        scatterChart.setLegendVisible(false);
    }



    private void initialisePane(){

        chartWrapper = new ThresholdedScatterChartWrapper(scatterChart);
        chartWrapper.setIsInteractive(true);
        chartWrapper.addThreshold(Color.rgb(255, 0, 0));


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

        if (!scatterChart.getData().isEmpty()){
            scatterChart.getData().clear();
        }

        // Assume that the event of a missing value is low. We will ignore such cells for our KDE.
        ArrayList<Point> dataPoints = new ArrayList<>();
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        Series<Number, Number> series = new Series<>();

        for (PathObject cell : cells){
            if (Double.isNaN(cell.getMeasurementList().getMeasurementValue(xMeasurement)) ||
                    Double.isNaN(cell.getMeasurementList().getMeasurementValue(yMeasurement))){
                continue;
            }
            if (Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement))) && Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)))) {
                x.add(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)));
                y.add(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)));


                Point point = new Point(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)),
                        Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement))
                );

                series.getData().add(new XYChart.Data<>(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)),
                        Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement))));

                dataPoints.add(point);
            }
        }

        BivariateKDE kde = new BivariateKDE(x, y);
        double[] density = kde.estimate();
        int k=0;
        for (PathObject cell : cells){
            if (k < density.length) {
                cell.getMeasurementList().putMeasurement("KDE", density[k]);
                cell.getMeasurementList().putMeasurement("Logged x", x.get(k));
                cell.getMeasurementList().putMeasurement("Logged y", y.get(k++));

            }
        }


        double max = new DescriptiveStatistics(density).getMax();
        int i=0;


        scatterChart.getData().add(series);


        Set<Node> nodes = scatterChart.lookupAll(".series0");
        for (Node n : nodes) {
            double normalisedValue = density[i]/max;
            Color color = ColourMapper.mapToColor(normalisedValue);
            n.setStyle("-fx-background-color: rgb("+(int) Math.floor(color.getRed())+","+(int) Math.floor(color.getGreen())+","+(int) Math.floor(color.getBlue())+");"
            +"-fx-background-radius: 1px;"
            );
            n.setScaleX(0.1);
            n.setScaleY(0.1);
            i++;
        }
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

    public static class Point {
        private final double x;
        private final double y;

        private double density;

        public Point(double x, double y){
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setDensity(double density) {
            this.density = density;
        }

        public void incrementDensity(double additionalDensity){
            this.density += additionalDensity;
        }

        public void normaliseDensity(double divisor){
            this.density /= divisor;
        }

        public double getDensity() {
            return density;
        }
    }

    public static class ColourMapper {


        public static Color mapToColor(double x){
            Color colour = null;

            if (Double.compare(x, 1) > 0){
                colour = Color.rgb(255, 255, 0);
            } else if (Double.compare(1, x) >= 0 && Double.compare(x, 0.666) > 0){
                colour = Color.rgb((int) Math.floor((x - 0.666)* 255/(0.333)), 255, 0);
            } else if (Double.compare(0.666, x) >= 0 && Double.compare(x, 0.333) > 0){
                colour = Color.rgb(0, 255, (int) Math.floor((1-((x - 0.333))/(0.333))*255));
            } else if (Double.compare(0.333, x) >= 0 && Double.compare(x, 0) > 0){
                colour = Color.rgb(0, (int) Math.floor((x)* 255/(0.333)), 255);
            } else{
                colour = Color.rgb(0, 0, 255);
            }


            return colour;
        }

    }
}
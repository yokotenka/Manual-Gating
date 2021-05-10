package com.wehi.ChartVisualiseHelpers;

import com.wehi.ManualGatingWindow;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.objects.PathObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Class which is responsible for creating the ThresholdedScatterChartWrapper and returning the grid pane containing
 * it.
 */
public class CytometryChart {

    // The scatterChart to be wrapped
    private ScatterChart<Number, Number> scatterChart;
    // The stage needed for binding the size property
    private Stage stage;
    // GridPane to be returned
    private GridPane group;

    // scatter chart wrapper containing the sliders
    private ThresholdedScatterChartWrapper chartWrapper;

    // X axis
    private NumberAxis xAxis;
    // Y axis
    private NumberAxis yAxis;

    // Series containing the data points
    private Series<Number, Number> series;

    // variable to check whether the series needs to be initialised or replaced
    private boolean isFirstTime = true;

    // Maximum value the axes will go to
    public static final double MAX_LOG_INTENSITY = 6;


    /**
     * Constructor
     * @param stage the main stage of the plugin
     */
    public CytometryChart(Stage stage){
        this.stage = stage;
        // Initialise the scatter chart
        initialiseScatterChart();
        // initialise the pane
        initialisePane();
    }


    /* ****** Initialise the chart ***********/
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

        // Instantiate the series
        series = new Series<>();
        scatterChart.getData().add(series);
    }

    /* *********** Initialise the pane ********* */
    private void initialisePane(){
        // wrap the scatter chart
        chartWrapper = new ThresholdedScatterChartWrapper(scatterChart);
        chartWrapper.setIsInteractive(true);
        chartWrapper.addThreshold(Color.rgb(255, 0, 0));

        // get the gridPane
        group = new GridPane();
        group.add(chartWrapper.getPane(), 0, 0);

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


    /**
     * To populate the scatter chart with data points
     * @param cells the cells which will be plotted
     * @param xMeasurement the x Axis measurement
     * @param yMeasurement the y Axis measurement
     * We will not plot cells which have missing values.
     */
    public void populateScatterChart(Collection<PathObject> cells, String xMeasurement, String yMeasurement){

        // Assume that the event of a missing value is low. We will ignore such cells for our KDE.
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();

        // This is to make sure we remove the previous plot before we plot our new points.
        if (!isFirstTime){
            series = new Series<>();
            scatterChart.setData(FXCollections.observableArrayList(series));
        }

        // Iterate through the cells
        for (PathObject cell : cells){
            // Check if either of the measurement is NaN
            if (Double.isNaN(cell.getMeasurementList().getMeasurementValue(xMeasurement)) ||
                    Double.isNaN(cell.getMeasurementList().getMeasurementValue(yMeasurement))){
                continue;
            }
            // Check that the logged value is finite
            if (Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)))
                    && Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)))) {
                x.add(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)));
                y.add(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)));

                series.getData().add(new XYChart.Data<>(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)),
                        Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement))));

            }
        }

        // To indicate there is data to be replaced in the series
        isFirstTime = false;

        // Get the KDE
        BivariateKDE kde = new BivariateKDE(x, y);
        // Estimate for all the values
        double[] density = kde.estimate();

        // update the bounds so that the chart looks better
        updateXBound(kde.getXOutlier());
        updateYBounds(kde.getYOutlier());


        // Recolour all of the points according to the density
        double max = new DescriptiveStatistics(density).getMax();
        int i=0;

        Set<Node> nodes = scatterChart.lookupAll(".series0");
        for (Node n : nodes) {
            double normalisedValue = density[i]/max;
            ColourMapper.Triplet color = ColourMapper.mapToColor(normalisedValue);
            n.setStyle(
//                    "-fx-background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");\n"
                    "-fx-background-radius: 1px;"
                    +"-fx-shape: \"M 10 10 H 90 V 90 H 10 L 10 10\";"

            );
            n.setScaleX(0.25);
            n.setScaleY(0.25);
            i++;
        }
    }

    /**
     * Plots without calculating the KDE. Bins each value and assigns a colour.
     * @param cells
     * @param xMeasurement
     * @param yMeasurement
     * @param nThDecimal
     */
    public void populateScatterChartHistogram(Collection<PathObject> cells, String xMeasurement, String yMeasurement, int nThDecimal){
        // Assume that the event of a missing value is low. We will ignore such cells for our KDE.
        HashMap<Point2D, Integer> squareWeightMap = new HashMap<>();

        // This is to make sure we remove the previous plot before we plot our new points.
        if (!isFirstTime){
            series = new Series<>();
            scatterChart.setData(FXCollections.observableArrayList(series));
        }
        isFirstTime = false;
        int total = 0;
        for (PathObject cell : cells){
            if (!cell.getMeasurementList().containsNamedMeasurement(xMeasurement) ||!cell.getMeasurementList().containsNamedMeasurement(yMeasurement) ){
                if (!cell.getMeasurementList().containsNamedMeasurement(xMeasurement) && cell.getMeasurementList().containsNamedMeasurement(yMeasurement) ){
                    Dialogs.showErrorMessage(ManualGatingWindow.TITLE, "The following measurement does not exist: " + xMeasurement);
                } else if (cell.getMeasurementList().containsNamedMeasurement(xMeasurement) && !cell.getMeasurementList().containsNamedMeasurement(yMeasurement) ) {
                    Dialogs.showErrorMessage(ManualGatingWindow.TITLE, "The following measurement does not exist: " + yMeasurement);
                } else {
                    Dialogs.showErrorMessage(ManualGatingWindow.TITLE, "The following measurements do not exist: " + xMeasurement + ", " + yMeasurement);
                }
                return;
            }


            // Check if either of the measurement is NaN
            if (Double.isNaN(cell.getMeasurementList().getMeasurementValue(xMeasurement)) ||
                    Double.isNaN(cell.getMeasurementList().getMeasurementValue(yMeasurement))){
                continue;
            }
            // Check that the logged value is finite
            if (Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)))
                    && Double.isFinite(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)))) {
                Point2D point = new Point2D(roundValue(Math.log(cell.getMeasurementList().getMeasurementValue(xMeasurement)), nThDecimal),
                        roundValue(Math.log(cell.getMeasurementList().getMeasurementValue(yMeasurement)), nThDecimal));

                Integer frequency = squareWeightMap.getOrDefault(point, 0) + 1;
                squareWeightMap.put(point, frequency);
                total++;
            }
        }
        Set<Map.Entry<Point2D, Integer>> entrySet = squareWeightMap.entrySet();
        List<Map.Entry<Point2D, Integer>> entryList = new ArrayList<>(entrySet);
        entryList.sort(Comparator.comparingDouble(Map.Entry::getValue));

        double max = 0;
        for (Map.Entry<Point2D, Integer> entry : entryList){
            series.getData().add(new XYChart.Data<>(entry.getKey().getX(), entry.getKey().getY()));
            if (Double.compare(entry.getValue(), max) > 0){
                max = entry.getValue();
            }
        }

        int i=0;
        Set<Node> nodes = scatterChart.lookupAll(".series0");
        for (Node n : nodes) {
            double normalisedValue = (double) entryList.get(i).getValue()/ max;
            ColourMapper.Triplet color = ColourMapper.mapToColor(normalisedValue);
            n.setStyle(
                    "-fx-background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");\n"
                    +"-fx-background-radius: 1px;"
                    +"-fx-shape: \"M 25, 50\n" +
                            "    a 25,25 0 1,1 50,0\n" +
                            "    a 25,25 0 1,1 -50,0\";"
            );
            n.setScaleX(2);
            n.setScaleY(2);
            i++;
        }
    }

    private Double roundValue(double num, int nThDecimal){
        return Math.round(num * Math.pow(10, nThDecimal)) / Math.pow(10, nThDecimal);
    }


    // Update the bounds for the x axis
    private void updateXBound(double lowerBound){
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(CytometryChart.MAX_LOG_INTENSITY);

        chartWrapper.updateXSliderBounds(lowerBound, CytometryChart.MAX_LOG_INTENSITY);
    }

    // Update the bounds for the y axis
    private void updateYBounds(double lowerBound){
        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(CytometryChart.MAX_LOG_INTENSITY);

        chartWrapper.updateYSliderBounds(lowerBound, CytometryChart.MAX_LOG_INTENSITY);
    }

    /**
     * Getter for the Pane containing the chart and sliders
     * @return GridPane
     */
    public GridPane getPane() {
        return group;
    }

    /**
     * Update the Axis labels
     * @param xAxisLabel new x label
     * @param yAxisLabel new y label
     */
    public void updateAxisLabels(String xAxisLabel, String yAxisLabel){
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
    }

    /**
     * Getter for X axis Slider
     * @return Slider
     */
    public Slider getXSlider(){
        return chartWrapper.getHorizontalSlider();
    }

    /** Getter for Y axis Slider
     * @return Slider
     */
    public Slider getYSlider(){
        return chartWrapper.getVerticalSlider();
    }


    /**
     * Helper class to map a double to a (r,g,b) colour triplet
     */
    public static class ColourMapper {

        /**
         * Static method which does the mapping
         * @param x
         * @return colour triplet
         */
        public static Triplet mapToColor(double x){
            Triplet colour;

            // If the double is beyond 1
            if (Double.compare(x, 1) > 0){
                colour = new Triplet(255, 255, 0);
            }
            // The double is between 1 and 2/3
            else if (Double.compare(1, x) >= 0 && Double.compare(x, 0.666) > 0){
                double scaledVal = (x - 0.666)/(0.333);
                colour = new Triplet((int) Math.floor(scaledVal * 255), 255, 0);
            }
            // The double is between 2/3 and 1/3
            else if (Double.compare(0.666, x) >= 0 && Double.compare(x, 0.333) > 0){
                double scaledVal = 1-(x - 0.333)/0.333;
                colour = new Triplet(0, 255, (int) Math.floor(scaledVal*255));
            }
            // The double is between 1/3 and 0
            else if (Double.compare(0.333, x) >= 0 && Double.compare(x, 0) > 0){
                double scaledVal = x /(0.333);
                colour = new Triplet(0, (int) Math.floor(scaledVal * 255), 255);
            }
            // The double is less than 0
            else{
                colour = new Triplet(0, 0, 255);
            }
            return colour;
        }

        /**
         * Helper class for ColourMapper. Stores three integers.
         */
        public static class Triplet{
            public static final int VALUE_MAX = 255;
            public static final int VALUE_MIN = 0;

            // Red value
            private int red;
            // Green value
            private int green;
            // Blue value
            private int blue;

            /**
             * Constructor for the triplet
             * @param red value for red
             * @param green value for green
             * @param blue value for blue
             */
            public Triplet(int red, int green, int blue){
                this.red = checkColour(red);
                this.green = checkColour(green);
                this.blue = checkColour(blue);
            }

            // Check it is within 0-255
            private int checkColour(int value){
                if (value > VALUE_MAX){
                    value = VALUE_MAX;
                }else if (value < VALUE_MIN){
                    value = VALUE_MIN;
                }
                return value;
            }

            /** Getter for blue value
             * @return int
             */
            public int getBlue() {
                return blue;
            }

            /**
             * Getter for green value
             * @return int
             */
            public int getGreen() {
                return green;
            }

            /** Getter for red value
             * @return int
             */
            public int getRed() {
                return red;
            }
        }
    }
}
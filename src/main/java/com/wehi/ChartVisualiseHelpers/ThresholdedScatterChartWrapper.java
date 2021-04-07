package com.wehi.ChartVisualiseHelpers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.WritableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import qupath.lib.gui.dialogs.Dialogs;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for the scatter chart. Has a horizontal and a vertical slider which displays the thresholds.
 */
public class ThresholdedScatterChartWrapper {
    // Chart
    private XYChart<Number, Number> chart;
    // The axes
    private NumberAxis xAxis, yAxis;

    // The horizontal slider
    private Slider horizontalSlider;
    // The vertical slider
    private Slider verticalSlider;

    // The pane containing the chart and the sliders
    private Pane pane = new Pane();

    // The width of the threshold line
    private DoubleProperty lineWidth = new SimpleDoubleProperty(1);
    private DoubleProperty lineHeight = new SimpleDoubleProperty(1);

    // Telling us whether the chart is interactive or not
    private BooleanProperty isInteractive = new SimpleBooleanProperty(false);

    // The active thresholds
    private ObservableList<ObservableNumberValue> thresholds = FXCollections.observableArrayList();
    // The lines showing the thresholds
    private Map<ObservableNumberValue, Line> lines = new HashMap<>();


    /**
     * Note: xAxis and yAxis must be instances of NumberAxis.
     *
     * @param chart
     */
    public ThresholdedScatterChartWrapper(final XYChart<Number, Number> chart) {
        this.chart = chart;
        this.xAxis = (NumberAxis)chart.getXAxis();
        this.yAxis = (NumberAxis)chart.getYAxis();
        pane.getChildren().add(chart);
        chart.prefWidthProperty().bind(pane.widthProperty());
        chart.prefHeightProperty().bind(pane.heightProperty());

        initialiseSliders();

        thresholds.addListener((ListChangeListener<ObservableNumberValue>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    continue;
                } else {
                    for (ObservableNumberValue removedItem : c.getRemoved()) {
                        pane.getChildren().remove(lines.remove(removedItem));
                    }
                }
            }
        });

    }

    /**
     * Get the pane containing the histogram, which may be added to a scene.
     * @return
     */
    public Pane getPane() {
        return pane;
    }


    /**
     * Get a list of all thresholds.
     * @return
     */
    public ObservableList<ObservableNumberValue> getThresholds() {
        return thresholds;
    }


    /**
     * Clear all thresholds.
     */
    public void clearThresholds() {
        this.thresholds.clear();
    }

    /**
     * Set the color of a specified threshold line.
     * @param val
     * @param color
     */
    public void setThresholdColor(final ObservableNumberValue val, final Color color) {
        Line line = lines.get(val);
        if (line == null) {
            Dialogs.showErrorMessage("Thresholded Scatter Chart", "No threshold line found for "+val);
            return;
        }
        line.setStroke(color);
    }


    /**
     * Add a threshold value with its display color.
     * @param color
     */
    public void addThreshold(final Color color) {
        Line horizontalLine = new Line();
        Line verticalLine = new Line();
        if (color != null) {
            horizontalLine.setStroke(color);
            verticalLine.setStroke(color);
        }

        // For the horizontal threshold line
        horizontalLine.strokeWidthProperty().bind(lineWidth);

        // Bind the requested x position of the line to the 'actual' coordinate within the parent
        horizontalLine.startXProperty().bind(
                Bindings.createDoubleBinding(() -> {
                            double xAxisPosition = xAxis.getDisplayPosition(horizontalSlider.valueProperty().doubleValue());
                            Point2D positionInScene = xAxis.localToScene(xAxisPosition, 0);
                            return pane.sceneToLocal(positionInScene).getX();
                        },
                        horizontalSlider.valueProperty(),
                        chart.widthProperty(),
                        chart.heightProperty(),
                        chart.boundsInParentProperty(),
                        xAxis.lowerBoundProperty(),
                        xAxis.upperBoundProperty(),
                        xAxis.autoRangingProperty(),
                        yAxis.autoRangingProperty(),
                        yAxis.lowerBoundProperty(),
                        yAxis.upperBoundProperty(),
                        yAxis.scaleProperty()
                )
        );

        // End position same as starting position for vertical line
        horizontalLine.endXProperty().bind(horizontalLine.startXProperty());

        // Bind the y coordinates to the top and bottom of the chart
        // Binding to scale property can cause 2 calls, but this is required
        horizontalLine.startYProperty().bind(
                Bindings.createDoubleBinding(() -> {
                            double yAxisPosition = yAxis.getDisplayPosition(yAxis.getLowerBound());
                            Point2D positionInScene = yAxis.localToScene(0, yAxisPosition);
                            return pane.sceneToLocal(positionInScene).getY();
                        },
                        chart.widthProperty(),
                        chart.heightProperty(),
                        chart.boundsInParentProperty(),
                        xAxis.lowerBoundProperty(),
                        xAxis.upperBoundProperty(),
                        xAxis.autoRangingProperty(),
                        yAxis.autoRangingProperty(),
                        yAxis.lowerBoundProperty(),
                        yAxis.upperBoundProperty(),
                        yAxis.scaleProperty()
                )
        );
        horizontalLine.endYProperty().bind(
                Bindings.createDoubleBinding(() -> {
                            double yAxisPosition = yAxis.getDisplayPosition(yAxis.getUpperBound());
                            Point2D positionInScene = yAxis.localToScene(0, yAxisPosition);
                            return pane.sceneToLocal(positionInScene).getY();
                        },
                        chart.widthProperty(),
                        chart.heightProperty(),
                        chart.boundsInParentProperty(),
                        xAxis.lowerBoundProperty(),
                        xAxis.upperBoundProperty(),
                        xAxis.autoRangingProperty(),
                        yAxis.autoRangingProperty(),
                        yAxis.lowerBoundProperty(),
                        yAxis.upperBoundProperty(),
                        yAxis.scaleProperty()
                )
        );

        horizontalLine.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> {
                            if (Double.isNaN(horizontalSlider.valueProperty().doubleValue()))
                                return false;
                            return chart.isVisible();
                        },
                        horizontalSlider.valueProperty(),
                        chart.visibleProperty())
        );

        // We can only bind both ways if we have a writable value
        if (horizontalSlider.valueProperty() != null) {
            horizontalLine.setOnMouseDragged(e -> {
                if (isInteractive()) {
                    double xNew = xAxis.getValueForDisplay(xAxis.sceneToLocal(e.getSceneX(), e.getSceneY()).getX()).doubleValue();
                    xNew = Math.max(xNew, xAxis.getLowerBound());
                    xNew = Math.min(xNew, xAxis.getUpperBound());
                    ((WritableNumberValue) horizontalSlider.valueProperty()).setValue(xNew);
                }
            });


            horizontalLine.setOnMouseEntered(e -> {
                if (isInteractive())
                    horizontalLine.setCursor(Cursor.H_RESIZE);
            });

            horizontalLine.setOnMouseExited(e -> {
                if (isInteractive())
                    horizontalLine.setCursor(Cursor.DEFAULT);
            });

        }
        thresholds.add(horizontalSlider.valueProperty());
        lines.put(horizontalSlider.valueProperty(), horizontalLine);
        pane.getChildren().add(horizontalLine);


        // For vertical Line
        verticalLine.strokeWidthProperty().bind(lineHeight);

        // Bind the requested x position of the line to the 'actual' coordinate within the parent
        // Bind the y coordinates to the top and bottom of the chart
        // Binding to scale property can cause 2 calls, but this is required
        verticalLine.startYProperty().bind(
                Bindings.createDoubleBinding(() -> {
                            double yAxisPosition = yAxis.getDisplayPosition(verticalSlider.valueProperty().doubleValue());
                            Point2D positionInScene = yAxis.localToScene(0, yAxisPosition);
                            return pane.sceneToLocal(positionInScene).getY();
                        },
                        verticalSlider.valueProperty(),
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
                )
        );
        verticalLine.endYProperty().bind(verticalLine.startYProperty());

        verticalLine.startXProperty().bind(
                Bindings.createDoubleBinding(() -> {
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
                )
        );

        // End position same as starting position for vertical line
        verticalLine.endXProperty().bind(
                Bindings.createDoubleBinding(() -> {
                            double xAxisPosition = xAxis.getDisplayPosition(xAxis.getUpperBound());
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
                )
        );

        verticalLine.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> {
                            if (Double.isNaN(verticalSlider.valueProperty().doubleValue()))
                                return false;
                            return chart.isVisible();
                        },
                        verticalSlider.valueProperty(),
                        chart.visibleProperty())
        );

        // We can only bind both ways if we have a writable value
        if (verticalSlider.valueProperty() != null) {
            verticalLine.setOnMouseDragged(e -> {
                if (isInteractive()) {
                    double yNew = yAxis.getValueForDisplay(yAxis.sceneToLocal(e.getSceneX(), e.getSceneY()).getY()).doubleValue();
                    yNew = Math.max(yNew, yAxis.getLowerBound());
                    yNew = Math.min(yNew, yAxis.getUpperBound());
                    ((WritableNumberValue) verticalSlider.valueProperty()).setValue(yNew);
                }
            });


            verticalLine.setOnMouseEntered(e -> {
                if (isInteractive())
                    verticalLine.setCursor(Cursor.V_RESIZE);
            });

            verticalLine.setOnMouseExited(e -> {
                if (isInteractive())
                    verticalLine.setCursor(Cursor.DEFAULT);
            });


            horizontalSlider.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
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
            horizontalSlider.prefWidthProperty().bind(xAxis.widthProperty());

            verticalSlider.layoutYProperty().bind(
                    Bindings.createDoubleBinding(() -> {
                                double yAxisPosition = yAxis.getDisplayPosition(yAxis.getUpperBound());
                                Point2D positionInScene = yAxis.localToScene(yAxisPosition, 0);
                                return pane.sceneToLocal(positionInScene).getY();
                            },
                            chart.widthProperty(),
                            chart.heightProperty(),
                            chart.boundsInParentProperty(),
                            xAxis.lowerBoundProperty(),
                            xAxis.upperBoundProperty(),
                            xAxis.autoRangingProperty(),
                            yAxis.scaleProperty(),
                            yAxis.autoRangingProperty(),
                            yAxis.lowerBoundProperty(),
                            yAxis.upperBoundProperty()
                    )
            );
            verticalSlider.prefHeightProperty().bind(yAxis.heightProperty());
            thresholds.add(verticalSlider.valueProperty());
            lines.put(verticalSlider.valueProperty(), verticalLine);
            pane.getChildren().add(verticalLine);
        }
    }

    private void initialiseSliders(){

        horizontalSlider = new Slider();
        horizontalSlider.setMax(CytometryChart.MAX_LOG_INTENSITY);
        horizontalSlider.setMin(-CytometryChart.MAX_LOG_INTENSITY);
        horizontalSlider.setValue(0);
        horizontalSlider.setShowTickLabels(false);
        horizontalSlider.setShowTickMarks(false);

        verticalSlider = new Slider();
        verticalSlider.setOrientation(Orientation.VERTICAL);
        verticalSlider.setMax(CytometryChart.MAX_LOG_INTENSITY);
        verticalSlider.setMin(-CytometryChart.MAX_LOG_INTENSITY);
        verticalSlider.setValue(0);
        verticalSlider.setShowTickMarks(false);
        verticalSlider.setShowTickLabels(false);

        pane.getChildren().add(verticalSlider);
        pane.getChildren().add(horizontalSlider);
    }

    /**
     * Updates the bounds for the X slider
     * @param lowerBound
     * @param upperBound
     */
    public void updateXSliderBounds(double lowerBound, double upperBound){
        horizontalSlider.setMax(upperBound);
        horizontalSlider.setMin(lowerBound);
    }

    /**
     * Updates the bounds for the Y slider
     * @param lowerBound
     * @param upperBound
     */
    public void updateYSliderBounds(double lowerBound, double upperBound){
        verticalSlider.setMin(lowerBound);
        verticalSlider.setMax(upperBound);
    }


    /**
     * Line width property used for displaying threshold lines.
     * @return
     */
    public DoubleProperty lineWidthProperty() {
        return lineWidth;
    }

    /**
     * Get the threshold line width.
     * @return
     */
    public double getLineWidth() {
        return lineWidth.get();
    }

    /**
     * Set the threshold line width.
     * @param width
     */
    public void setLineWidth(final double width) {
        lineWidth.set(width);
    }

    /**
     * Line width property used for displaying threshold lines.
     * @return
     */
    public DoubleProperty lineHeightProperty() {
        return lineHeight;
    }

    /**
     * Get the threshold line height.
     * @return
     */
    public double getLineHeight(){
        return lineHeight.get();
    }

    /**
     * Set the threshold line height.
     * @param height
     */
    public void setLineHeight(final double height){
        lineHeight.set(height);
    }


    /**
     * Property indicating whether thresholds can be adjusted interactively.
     * @return
     */
    public BooleanProperty isInteractiveProperty() {
        return isInteractive;
    }

    /**
     * Returns the value of {@link #isInteractiveProperty()}.
     * @return
     */
    public boolean isInteractive() {
        return isInteractive.get();
    }

    /**
     * Sets the value of {@link #isInteractiveProperty()}.
     * @param isInteractive
     */
    public void setIsInteractive(final boolean isInteractive) {
        this.isInteractive.set(isInteractive);
    }

    /**
     * Gets the xAxis
     * @return
     */
    public NumberAxis getXAxis(){
        return xAxis;
    }

    /**
     * Gets the yAxis
     * @return
     */
    public NumberAxis getYAxis(){
        return yAxis;
    }

    /**
     * Getter for the horizontal slider
     * @return
     */
    public Slider getHorizontalSlider() {
        return horizontalSlider;
    }

    /**
     * Getter for the vertical slider
     * @return
     */
    public Slider getVerticalSlider() {
        return verticalSlider;
    }
}

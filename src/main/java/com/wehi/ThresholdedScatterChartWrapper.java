package com.wehi;

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
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import qupath.lib.gui.dialogs.Dialogs;

import java.util.HashMap;
import java.util.Map;

public class ThresholdedScatterChartWrapper {
    private XYChart<Number, Number> chart;
    private NumberAxis xAxis, yAxis;
    private Pane pane = new Pane();

    private DoubleProperty lineWidth = new SimpleDoubleProperty(3);
    private DoubleProperty lineHeight = new SimpleDoubleProperty(3);

    private BooleanProperty isInteractive = new SimpleBooleanProperty(false);

    private ObservableList<ObservableNumberValue> thresholds = FXCollections.observableArrayList();
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

        thresholds.addListener((ListChangeListener<ObservableNumberValue>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    continue;
                } else {
                    for (ObservableNumberValue removedItem : c.getRemoved()) {
                        pane.getChildren().remove(lines.remove(removedItem));
                    }
//	        				pane.getChildren().removeAll(c.getRemoved());
//	        				for (ObservableNumberValue addedItem : c.getAddedSubList()) {
//	        					addThreshold(addedItem.getValue().doubleValue());
//	        				}
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
     * Set thresholds, which are visualized as vertical lines.
     * @param color
     * @param thresholds
     */
    public void setThresholds(Color color, double... thresholds) {
        clearThresholds();
        for (double xx : thresholds)
            addThreshold(xx, color);
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
     * Add a threshold value.
     * @param x
     * @return
     */
    public ObservableNumberValue addThreshold(final double x) {
        return addThreshold(x, null);
    }

    /**
     * Add a threshold value with its display color.
     * @param x
     * @param color
     * @return
     */
    public ObservableNumberValue addThreshold(final double x, final Color color) {
        return addThreshold(new SimpleDoubleProperty(x), color, 0);
    }


    /**
     * Add a threshold value with its display color.
     * @param d
     * @param color
     * @return
     */
    public ObservableNumberValue addThreshold(final ObservableNumberValue d, final Color color, int orientation) {
        Line line = new Line();
        if (color != null)
            line.setStroke(color);

        if (orientation == 0) {
            line.strokeWidthProperty().bind(lineWidth);

            // Bind the requested x position of the line to the 'actual' coordinate within the parent
            line.startXProperty().bind(
                    Bindings.createDoubleBinding(() -> {
                                double xAxisPosition = xAxis.getDisplayPosition(d.doubleValue());
                                Point2D positionInScene = xAxis.localToScene(xAxisPosition, 0);
                                return pane.sceneToLocal(positionInScene).getX();
                            },
                            d,
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
            line.endXProperty().bind(line.startXProperty());

            // Bind the y coordinates to the top and bottom of the chart
            // Binding to scale property can cause 2 calls, but this is required
            line.startYProperty().bind(
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
            line.endYProperty().bind(
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

            line.visibleProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                                if (Double.isNaN(d.doubleValue()))
                                    return false;
                                return chart.isVisible();
                            },
                            d,
                            chart.visibleProperty())
            );

            // We can only bind both ways if we have a writable value
            if (d instanceof WritableNumberValue) {
                line.setOnMouseDragged(e -> {
                    if (isInteractive()) {
                        double xNew = xAxis.getValueForDisplay(xAxis.sceneToLocal(e.getSceneX(), e.getSceneY()).getX()).doubleValue();
                        xNew = Math.max(xNew, xAxis.getLowerBound());
                        xNew = Math.min(xNew, xAxis.getUpperBound());
                        ((WritableNumberValue) d).setValue(xNew);
                    }
                });


                line.setOnMouseEntered(e -> {
                    if (isInteractive())
                        line.setCursor(Cursor.H_RESIZE);
                });

                line.setOnMouseExited(e -> {
                    if (isInteractive())
                        line.setCursor(Cursor.DEFAULT);
                });

            }

        } else {
            line.strokeWidthProperty().bind(lineHeight);

            // Bind the requested x position of the line to the 'actual' coordinate within the parent
            // Bind the y coordinates to the top and bottom of the chart
            // Binding to scale property can cause 2 calls, but this is required
            line.startYProperty().bind(
                    Bindings.createDoubleBinding(() -> {
                                double yAxisPosition = yAxis.getDisplayPosition(d.doubleValue());
                                Point2D positionInScene = yAxis.localToScene(0, yAxisPosition);
                                return pane.sceneToLocal(positionInScene).getY();
                            },
                            d,
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
            line.endYProperty().bind(line.startYProperty());

            line.startXProperty().bind(
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
            line.endXProperty().bind(
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

            line.visibleProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                                if (Double.isNaN(d.doubleValue()))
                                    return false;
                                return chart.isVisible();
                            },
                            d,
                            chart.visibleProperty())
            );

            // We can only bind both ways if we have a writable value
            if (d instanceof WritableNumberValue) {
                line.setOnMouseDragged(e -> {
                    if (isInteractive()) {
                        double yNew = yAxis.getValueForDisplay(yAxis.sceneToLocal(e.getSceneX(), e.getSceneY()).getY()).doubleValue();
                        yNew = Math.max(yNew, yAxis.getLowerBound());
                        yNew = Math.min(yNew, yAxis.getUpperBound());
                        ((WritableNumberValue) d).setValue(yNew);
                    }
                });


                line.setOnMouseEntered(e -> {
                    if (isInteractive())
                        line.setCursor(Cursor.V_RESIZE);
                });

                line.setOnMouseExited(e -> {
                    if (isInteractive())
                        line.setCursor(Cursor.DEFAULT);
                });

            }
        }



        thresholds.add(d);
        lines.put(d, line);
        pane.getChildren().add(line);
        //			updateChart();
        return d;
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

}

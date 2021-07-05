package com.wehi.chart;

import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.analysis.stats.Histogram;
import qupath.lib.common.ColorTools;
import qupath.lib.gui.charts.HistogramPanelFX;
import qupath.lib.objects.PathObject;

import java.util.Collection;

public class HistogramWrapper extends CustomChart{

    private HistogramPanelFX histogramPane;
    private HistogramPanelFX.ThresholdedChartWrapper chartWrapper;

    private String measurement;
    private Slider sliderThreshold = new Slider();

    public HistogramWrapper(){

        initialiseChart();
    }

    public void initialiseChart(){
        histogramPane = new HistogramPanelFX();
        chartWrapper = new HistogramPanelFX.ThresholdedChartWrapper(histogramPane.getChart());
    }

    public void updateAxisLabel(String measurement){
        this.measurement = measurement;
    }

    void populateChart(Collection<PathObject> cells) {
        if (measurement == null || cells.isEmpty()) {
            sliderThreshold.setMin(0);
            sliderThreshold.setMax(1);
            sliderThreshold.setValue(0);
            return;
        }
        double[] allValues = cells.stream().mapToDouble(p -> p.getMeasurementList().getMeasurementValue(measurement))
                .filter(Double::isFinite)
                .map(Math::log)
                .toArray();
        var stats = new DescriptiveStatistics(allValues);
        var histogram = new Histogram(allValues, 100, stats.getMin(), stats.getMax());
        histogramPane.getHistogramData().setAll(HistogramPanelFX.createHistogramData(histogram, false, (Integer) null));

//        double value = previousThresholds.getOrDefault(measurement, stats.getMean());
        sliderThreshold.setMin(stats.getMin());
        sliderThreshold.setMax(stats.getMax());
        sliderThreshold.setValue(stats.getMin());
    }

    public Pane getPane(){
        return chartWrapper.getPane();
    }


}

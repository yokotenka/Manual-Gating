package com.wehi.ChartVisualiseHelpers;

import com.wehi.TableTreeViewHelpers.CytometryChart;
import javafx.geometry.Point2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.objects.PathObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BivariateKDE {


    public static final int dimensions = 2;
    private double[][] bandwidthMatrix;

    private ArrayList<CytometryChart.Point> dataPoints;

    private DescriptiveStatistics ds1;
    private DescriptiveStatistics ds2;

    private ArrayList<Double> x;
    private ArrayList<Double> y;

    private double[] density;


    public BivariateKDE(ArrayList<Double> x, ArrayList<Double> y){
        this.x = x;
        this.y = y;

        bandwidthMatrix = new double[dimensions][dimensions];
        calculateBandwidths();
    }





    public double[] estimate(){

        double[] densities = new double[x.size()];
        IndependentBivariateNormalDistribution bivariateNormalDistribution = new IndependentBivariateNormalDistribution(bandwidthMatrix);

        boolean isFirstPoint = true;

        for (int i=0; i < x.size(); i++){
            bivariateNormalDistribution.setMean(x.get(i), y.get(i));
            for (int j=0; j < x.size(); j++){
                if (isFirstPoint) {
                    densities[j] = bivariateNormalDistribution.density(x.get(j), y.get(j));
                    isFirstPoint = false;
                } else {
                    densities[j] += bivariateNormalDistribution.density(x.get(j), y.get(j));
                }
            }
        }

        for (int k=0; k< densities.length; k++){
            densities[k] = densities[k] / x.size();
        }

        return densities;
    }

    public void calculateBandwidths(){
        ds1 = new DescriptiveStatistics(x.stream().mapToDouble(p -> p).toArray());
        ds2 = new DescriptiveStatistics(y.stream().mapToDouble(p -> p).toArray());

        double n = x.size();
        double num = Math.pow(4.0 / (n * (2 + dimensions)), 1.0/(dimensions + 4));

        bandwidthMatrix[0] = new double[2];
        bandwidthMatrix[1] = new double[2];

        bandwidthMatrix[0][0] = Math.pow(num * ds1.getStandardDeviation(),2);
        bandwidthMatrix[0][1] = 0;

        bandwidthMatrix[1][0] = 0;
        bandwidthMatrix[1][1] = Math.pow(num * ds2.getStandardDeviation(),2);
    }


    public double getXOutlier(){
        return ds1.getPercentile(25) - (ds1.getPercentile(75) - ds1.getPercentile(25)) * 3;
    }

    public double getYOutlier(){
        return ds2.getPercentile(25) - (ds2.getPercentile(75) - ds2.getPercentile(25)) * 3;
    }
}

package com.wehi.ChartVisualiseHelpers;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Bi-Variate KDE Assuming the Bi-variate Gaussian are independent
 */
public class BivariateKDE {
    // Number of dimensions
    public static final int dimensions = 2;
    // The bandwidth matrix
    private final double[][] bandwidthMatrix;

    // stats for x
    private DescriptiveStatistics ds1;
    // stats for y
    private DescriptiveStatistics ds2;

    // x
    private ArrayList<Double> x;
    // y
    private ArrayList<Double> y;

    private final int INCREMENT = 100;
    /**
     * Constructor
     * @param x x values
     * @param y y values
     */
    public BivariateKDE(ArrayList<Double> x, ArrayList<Double> y){
        this.x = x;
        this.y = y;
        bandwidthMatrix = new double[dimensions][dimensions];
        calculateBandwidths();
    }


    /**
     * Estimate the densities
     * @return double[] containing the estimates at each point (x,y)
     */
    public double[] estimate(){

        double[] densities = new double[x.size()];
        IndependentBivariateNormalDistribution bivariateNormalDistribution = new IndependentBivariateNormalDistribution(bandwidthMatrix);

        int n = x.size();
        for (int i=n; --i>=0; ){
            bivariateNormalDistribution.setMean(x.get(i), y.get(i));
            for (int j=n; --j>=0; ){
                densities[j] += bivariateNormalDistribution.density(x.get(j), y.get(j)) / x.size();
            }
        }
        return densities;
    }

    /**
     * Calculates the bandwidth matrix
     */
    public void calculateBandwidths(){
        int n = x.size();
        double[] arr1 = new double[n];
        double[] arr2 = new double[n];

        for (int i=n; --i>=0;){
            arr1[i] = x.get(i);
            arr2[i] = y.get(i);
        }

        ds1 = new DescriptiveStatistics(arr1);
        ds2 = new DescriptiveStatistics(arr2);

        double num = Math.pow(4.0 / (n * (2 + dimensions)), 1.0/(dimensions + 4));

        bandwidthMatrix[0] = new double[2];
        bandwidthMatrix[1] = new double[2];

        bandwidthMatrix[0][0] = Math.pow(num * ds1.getStandardDeviation(),2);
        bandwidthMatrix[0][1] = 0;

        bandwidthMatrix[1][0] = 0;
        bandwidthMatrix[1][1] = Math.pow(num * ds2.getStandardDeviation(),2);
    }

    /**
     * Get the X outlier. Needed to rescale the chart
     * @return lower outlier cutoff
     */
    public double getXOutlier(){
        return ds1.getPercentile(25) - (ds1.getPercentile(75) - ds1.getPercentile(25)) * 3;
    }

    /**
     * Get the Y outlier. Needed to rescale the chart
     * @return lower outlier cutoff
     */
    public double getYOutlier(){
        return ds2.getPercentile(25) - (ds2.getPercentile(75) - ds2.getPercentile(25)) * 3;
    }


    /**
     * The Independent Bi-Variate Normal Distribution
     */
    public class IndependentBivariateNormalDistribution {
        // Covariance matrix
        private double[][] covarianceMatrix;
        // mean of the two variables
        private double[] mean;

        /**
         * Constructor
         * @param covarianceMatrix covariance matrix
         */
        public IndependentBivariateNormalDistribution(double[][] covarianceMatrix){
            this.covarianceMatrix = covarianceMatrix;
            this.mean = new double[2];
        }

        /**
         * set means for the two variables
         * @param mean1 mean for variable 1
         * @param mean2 mean for variable 2
         */
        public void setMean(double mean1, double mean2){
            mean[0] = mean1;
            mean[1] = mean2;
        }

        /**
         * Get the density for the specified point (x1, x2)
         * @param x1 x1
         * @param x2 x2
         * @return density at that point
         */
        public double density(double x1, double x2){
            double exponent1 = (x1 - mean[0]) * (x1 - mean[0]) / covarianceMatrix[0][0];
            double exponent2 = (x2 - mean[1]) * (x2 - mean[1]) / covarianceMatrix[1][1];
            return (1 / (2 * Math.PI * Math.sqrt(covarianceMatrix[0][0]) * Math.sqrt(covarianceMatrix[1][1])))
                    * Math.exp(-0.5 * (exponent1 + exponent2));
        }
    }

}

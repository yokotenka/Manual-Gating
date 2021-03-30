package com.wehi.ChartVisualiseHelpers;

public class IndependentBivariateNormalDistribution {

    private double[][] covarianceMatrix;
    private double[] mean;

    public IndependentBivariateNormalDistribution(double[][] covarianceMatrix){
        this.covarianceMatrix = covarianceMatrix;
        this.mean = new double[2];
    }

    public void setMean(double mean1, double mean2){
        mean[0] = mean1;
        mean[1] = mean2;
    }



    public double density(double x1, double x2){

        double exponent1 = (x1 - mean[0]) * (x1 - mean[0]) / covarianceMatrix[0][0];
        double exponent2 = (x2 - mean[1]) * (x2 - mean[1]) / covarianceMatrix[1][1];


        return (1 / (2 * Math.PI * Math.sqrt(covarianceMatrix[0][0]) * Math.sqrt(covarianceMatrix[1][1]))) * Math.exp(-0.5 * (exponent1 + exponent2));
    }
}

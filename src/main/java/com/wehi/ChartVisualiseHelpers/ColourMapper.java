package com.wehi.ChartVisualiseHelpers;


import javafx.scene.paint.Color;

public class ColourMapper {


    public static Color mapToColor(double x){
        Color colour = null;

        if (Double.compare(x, 1) > 0){
            colour = Color.rgb(255, 255, 0);
        } else if (Double.compare(1, x) >= 0 && Double.compare(x, 0.666) > 0){
            colour = Color.rgb((int) Math.floor((x - 0.666)* 255/(0.333)), 255, 0);
        } else if (Double.compare(0.666, x) >= 0 && Double.compare(x, 0.333) > 0){
            colour = Color.rgb(0, 255, (int) Math.floor(1-((x - 0.333)/(0.333))*255));
        } else if (Double.compare(0.333, x) >= 0 && Double.compare(x, 0) > 0){
            colour = Color.rgb(0, (int) Math.floor((x)* 255/(0.333)), 255);
        } else{
            colour = Color.rgb(0, 0, 255);
        }


        return colour;
    }

}

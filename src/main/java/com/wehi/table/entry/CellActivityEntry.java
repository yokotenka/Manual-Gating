package com.wehi.table.entry;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class CellActivityEntry implements Colorable{
    private String name;

    private boolean isHidden;

    private Color color;

    private ArrayList<String> allActivity;



    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHidden(){
        return isHidden;
    }

    @Override
    public Color getColor(){
        return color;
    }

    @Override
    public void setColor(Color color){
        this.color = color;
    }

}

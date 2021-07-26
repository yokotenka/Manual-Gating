package com.wehi.table.entry;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public interface IVisualisable {

    String getName();

//    boolean isShown();

    Color getColor();

    void setColor(Color color);
    void setShow(boolean show);
    void applyColor();
//
//    Button getShowButton();
//
//    Button getHideButton();

    CheckBox getShow();

    ColorPicker getColorPicker();

    void hide();

    void show();

    void setColorDownTree(Color color);
}

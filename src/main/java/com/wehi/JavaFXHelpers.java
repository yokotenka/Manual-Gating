package com.wehi;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JavaFXHelpers {

    /**
     * Static method to create a label
     * @param msg the message to be displayed
     * @return label
     */
    public static Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    /**
     * Static helper to create a HBox
     * @return
     */
    public static HBox createHBox(){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        return hBox;
    }

    /**
     * Static helper to create a VBox
     * @return
     */
    public static VBox createVBox(){
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        return vBox;
    }

}

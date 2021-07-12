package com.wehi.table.entry;

import javafx.scene.paint.Color;

public interface Colorable {

    String getName();

    boolean isHidden();

    Color getColor();

    void setColor(Color color);
    }
}

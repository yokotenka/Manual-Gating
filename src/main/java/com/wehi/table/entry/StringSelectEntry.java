package com.wehi.table.entry;

import javafx.scene.control.CheckBox;

public class StringSelectEntry {

    private String name;

    private CheckBox selectedBox;

    public StringSelectEntry(String name, boolean isSelected){
        this.name = name;
        selectedBox = new CheckBox();
        selectedBox.setSelected(isSelected);
    }

    public String getName(){
        return name;
    }

    public CheckBox getSelectedBox(){
        return selectedBox;
    }

    public boolean isSelected(){
        return selectedBox.isSelected();
    }
}

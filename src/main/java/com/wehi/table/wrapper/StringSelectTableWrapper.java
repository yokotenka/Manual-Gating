package com.wehi.table.wrapper;

import com.wehi.table.entry.StringSelectEntry;

import java.util.ArrayList;

public class StringSelectTableWrapper extends TableWrapper<StringSelectEntry>{

    public StringSelectTableWrapper(){
        super();
        this.addColumn("Name", "name", 0.8);
        this.addColumn("Select", "selectedBox", 0.2);
    }

    public void selectAll(){
        for (StringSelectEntry entry : this.getItems()){
            entry.getSelectedBox().setSelected(true);
        }
    }

    public void deSelectAll(){
        for (StringSelectEntry entry : this.getItems()){
            entry.getSelectedBox().setSelected(false);
        }
    }

    public ArrayList<String> collectSelected(){
        ArrayList<String> selected = new ArrayList<>();

        for (StringSelectEntry entry : getItems()){
            if (entry.isSelected()){
                selected.add(entry.getName());
            }
        }
        return selected;
    }
}

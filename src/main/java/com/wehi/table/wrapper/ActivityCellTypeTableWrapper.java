package com.wehi.table.wrapper;

import com.wehi.table.entry.ActivityCellTypeEntry;

/**
 * This is a table for listing all of the cell activities for a particular phenotype
 */
public class ActivityCellTypeTableWrapper extends TableWrapper<ActivityCellTypeEntry>{

    public ActivityCellTypeTableWrapper(){
        super();
        this.addColumn("Name", "name", 0.7);
//        this.addColumn("Colour", "colorPicker", 0.3);
        this.addColumn("Show", "showButton", 0.3);
    }



}

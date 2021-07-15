package com.wehi.table.wrapper;

import com.wehi.table.entry.ActivityCellTypeEntry;
import com.wehi.table.entry.IVisualisable;

/**
 * This class is for listing all of the currently visible cell types
 */
public class ListActivityTableWrapper extends TableWrapper<IVisualisable>{

    public ListActivityTableWrapper(){
        super();
        this.addColumn("Combination", "name", 0.77);
        this.addColumn("Colour", "colorPicker", 0.15);
        this.addColumn("", "show", 0.08);
    }
}

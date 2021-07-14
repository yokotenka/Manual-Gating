package com.wehi.table.wrapper;

import com.wehi.table.entry.ActivityCellTypeEntry;
import com.wehi.table.entry.IVisualisable;

/**
 * This class is for listing all of the currently visible cell types
 */
public class ListActivityTableWrapper extends TableWrapper<IVisualisable>{

    public ListActivityTableWrapper(){
        super();
        this.addColumn("Combination", "name", 0.4);
        this.addColumn("Colour", "colorPicker", 0.4);
        this.addColumn("Hide", "hideButton", 0.2);
    }
}

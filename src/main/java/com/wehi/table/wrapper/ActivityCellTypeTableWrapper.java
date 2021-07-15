package com.wehi.table.wrapper;

import com.wehi.table.entry.ActivityCellTypeEntry;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This is a table for listing all of the cell activities for a particular phenotype
 */
public class ActivityCellTypeTableWrapper extends TableWrapper<ActivityCellTypeEntry>{

    public ActivityCellTypeTableWrapper(){
        super();
        this.addColumn("Name", "name", 0.92);
//        this.addColumn("Colour", "colorPicker", 0.3);
        this.addColumn("", "show", 0.08);
    }

    public void remove(ArrayList<String> activities){
        for (ActivityCellTypeEntry entry : this.getItems()){
//            Collections.sort(entry.getActivities());
//            Collections.sort(activities);
            if (entry.getActivities().equals(activities)){
                this.removeRow(entry);
                break;
            }
        }
    }

    public ActivityCellTypeEntry getSelectedItem(){
        return getTable().getSelectionModel().getSelectedItem();
    }

    public boolean contains(ArrayList<String> activity){
        for (ActivityCellTypeEntry entry : this.getItems()){
//            Collections.sort(entry.getActivities());
//            Collections.sort(activities);
            if (entry.getActivities().equals(activity)){
                return true;
            }
        }
        return false;
    }



}

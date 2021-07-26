package com.wehi.table.wrapper;

import com.wehi.table.entry.ActivityCellTypeEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    public void removeAllActivities(){
        getItems().clear();
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

    @Override
    public void addRow(ActivityCellTypeEntry row){
        getTable().getItems().add(row);
        getTable().getItems().sort(ActivityCellTypeComparator.getInstance());
        getTable().refresh();
    }

    public static class ActivityCellTypeComparator implements Comparator<ActivityCellTypeEntry> {

        private static ActivityCellTypeComparator activityCellTypeComparator;

        public static ActivityCellTypeComparator getInstance(){
            if (activityCellTypeComparator == null){
                activityCellTypeComparator = new ActivityCellTypeComparator();
            }
            return activityCellTypeComparator;
        }

        @Override
        public int compare(ActivityCellTypeEntry entry1, ActivityCellTypeEntry entry2) {
            return Integer.compare(entry1.getActivities().size(), entry2.getActivities().size());
        }
        //Override other methods if need to
    }

}

package com.wehi.table.wrapper;

import com.wehi.io.FunctionalIO;
import com.wehi.table.entry.FunctionalMarkerEntry;
import javafx.scene.control.TreeItem;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is the table which appears on the left of the functional marker plugin. It will list all
 * available functional marker entries.
 */
public class FunctionalMarkerTreeTableWrapper extends TreeTableCreator<FunctionalMarkerEntry>{
    // The dummy root of the tree table view
    private TreeItem<FunctionalMarkerEntry> root;

    /**
     * Constructor for the tree table
     */
    public FunctionalMarkerTreeTableWrapper(){
        super();
        FunctionalMarkerEntry rootEntry = new FunctionalMarkerEntry(null, null, "Functional Markers",null,null, false, null,null);
        root = new TreeItem<>(rootEntry);
        setRoot(root);
        this.addColumn("Name", "name", 1);
    }

    /**
     * Method to add extra rows
     * @param entry the new row
     */
    public void add(FunctionalMarkerEntry entry){
        root.getChildren().add(entry.getTreeItem());
    }

    public void addAll(List<FunctionalMarkerEntry> entries){
        for (FunctionalMarkerEntry entry : entries ){
            root.getChildren().add(entry.getTreeItem());
        }
    }


    /**
     * A method to check whether the selected and the current entry is equal
     * @param curr current entry
     * @return
     */
    public boolean checkIfToBeRemovedIsCurrent(FunctionalMarkerEntry curr){
        for (TreeItem<FunctionalMarkerEntry> e : getTreeTable().getSelectionModel().getSelectedItems()){
            if(e.getValue() == curr){
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the selected entries
     */
    public void removeSelected(){
        for (TreeItem<FunctionalMarkerEntry> e : getTreeTable().getSelectionModel().getSelectedItems()){
            try{

                e.getValue().removeAllKids();
                e.getParent().getChildren().remove(e);
            } catch(NullPointerException exception){
                return;
            }
        }
    }

    /**
     * Check if there already exists an entry with a given name
     * @param entryName
     * @param current
     * @return
     */
    public boolean contains(String entryName, FunctionalMarkerEntry current){
        for (TreeItem<FunctionalMarkerEntry> child : root.getChildren()){
            if (child.getValue().getName().equals(entryName) && current != child.getValue()){
                return true;
            }
        }
        return false;
    }

    /**
     * Save
     * @param folder
     * @param file
     * @throws IOException
     * @throws JSONException
     */
    public void save(File folder, String file) throws IOException, JSONException {
        FunctionalIO.saveToExistingPhenotypeFile(folder, file, root.getChildren());
    }

    /**
     * Deletes all children
     */
    public void clearChildren(){
        root.getChildren().clear();
    }

}

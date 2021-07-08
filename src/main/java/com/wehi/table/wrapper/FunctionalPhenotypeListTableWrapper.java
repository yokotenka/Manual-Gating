package com.wehi.table.wrapper;

import com.wehi.ClassifierSaver;
import com.wehi.table.entry.FunctionalMarkerEntry;
import javafx.scene.control.TreeItem;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.gui.QuPathGUI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FunctionalPhenotypeListTableWrapper extends TreeTableCreator<FunctionalMarkerEntry>{

    private TreeItem<FunctionalMarkerEntry> root;

    public FunctionalPhenotypeListTableWrapper(){
        super();
        FunctionalMarkerEntry rootEntry = new FunctionalMarkerEntry(null, null, "Functional Markers",null,null, false, null,null);
        root = new TreeItem<>(rootEntry);
        setRoot(root);
        this.addColumn("Name", "name", 1);
//        this.addColumn("Marker", "marker", 0.5);
    }

    public void add(FunctionalMarkerEntry entry){
        root.getChildren().add(entry.getTreeItem());
    }

    public boolean checkIfToBeRemovedIsCurrent(FunctionalMarkerEntry curr){
        for (TreeItem<FunctionalMarkerEntry> e : getTreeTable().getSelectionModel().getSelectedItems()){
            if(e.getValue() == curr){
                return true;
            }

        }
        return false;
    }

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

    public boolean contains(String entryName, FunctionalMarkerEntry current){
        for (TreeItem<FunctionalMarkerEntry> child : root.getChildren()){
            if (child.getValue().getName().equals(entryName) && current != child.getValue()){

                return true;
            }
        }
        return false;
    }

    public void saveTree(QuPathGUI qupath, String classifierName){
        var children = root.getChildren();
        ClassifierSaver.saveClassifiers(qupath, classifierName, children);
    }

    public void clearChildren(){
        root.getChildren().clear();
    }

//    public static FunctionalPhenotypeListTableWrapper loadOptions(QuPathGUI qupath, String string) throws IOException {
//
//    }
}

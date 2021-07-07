package com.wehi.table.wrapper;

import com.wehi.table.entry.FunctionalMarkerEntry;
import javafx.scene.control.TreeItem;

public class FunctionalPhenotypeListTableWrapper extends TreeTableCreator<FunctionalMarkerEntry>{

    private TreeItem<FunctionalMarkerEntry> root;

    public FunctionalPhenotypeListTableWrapper(){
        super();
        FunctionalMarkerEntry rootEntry = new FunctionalMarkerEntry(null, null, "Markers",null,null, false, null);
        root = new TreeItem<>(rootEntry);
        setRoot(root);
        this.addColumn("Name", "name", 1);
//        this.addColumn("Marker", "marker", 0.5);
    }

    public void add(FunctionalMarkerEntry entry){
        root.getChildren().add(entry.getTreeItem());
    }

    public void remove(FunctionalMarkerEntry entry){
        root.getChildren().remove(entry.getTreeItem());
    }


}

package com.wehi.table.wrapper;

import com.wehi.table.entry.PhenotypeEntry;

/**
 * This is the table for showing the treetable view for all of the available phenotypes
 */
public class VisualisationTreeTableWrapper extends TreeTableWrapper<PhenotypeEntry>{

    public VisualisationTreeTableWrapper(){
        super();
        this.addColumn("Name", "name", 0.92);
//        this.addColumn("Colour", "colorPicker", 0.4);
        this.addColumn("", "show", 0.08);
    }

    public void setRoot(PhenotypeEntry phenotypeEntry){
        this.getTreeTable().setRoot(phenotypeEntry.getTreeItem());
        this.getTreeTable().refresh();
    }


}

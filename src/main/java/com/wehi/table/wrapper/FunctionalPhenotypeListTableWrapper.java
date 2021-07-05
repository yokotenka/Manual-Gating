package com.wehi.table.wrapper;

import com.wehi.table.entry.FunctionalPhenotypeEntry;

public class FunctionalPhenotypeListTableWrapper extends TableWrapper<FunctionalPhenotypeEntry>{


    public FunctionalPhenotypeListTableWrapper(){
        super();
        this.addColumn("Phenotype", "phenotypeName", 0.5);
        this.addColumn("Marker", "marker", 0.5);
    }



}

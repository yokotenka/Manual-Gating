# Manual-Gating

Fixed in the latest push:   
 -calling   
 resetClassifications(imageData.getHierarchy(), mapPrevious.get(imageData.getHierarchy()))   
 on line 615, 640 and 669. Needed because when we click on one of the sliders, it will store the current hierarchy and create a new one just for visualising the cells which are positive on the image eg. if we are thresholding CD45 and PanCK and a cell is positive to PanCK the cell will be labelled "PanCK". If we then press the "Update Subphenotype" button, that cell will be labelled "PanCK: Tumour" instead of "Tumour". To prevent this, we need the resetClassifications() method which will reset the hierarchy to what it was before we used the slider.    

-there was a bug where if you keep clicking update phenotype, the phenotype name keeps getting concatenated eg. immune: immune: immune. This was fixed by adding in a condition on line 704 by calling checkForSingleClassification() which checks if the path class already contains a certain name. 

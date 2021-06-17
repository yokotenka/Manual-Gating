package com.wehi.pathclasshandler;

import com.wehi.table.entry.AxisTableEntry;
import qupath.lib.classifiers.PathClassifierTools;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.classifiers.object.ObjectClassifiers;
import qupath.lib.common.ThreadTools;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjectFilter;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class for applying changes to the PathClass of cells in the image
 */
public class PathClassHandler {

    public static PathClassHandler pathClassHandler;
    private ImageData<BufferedImage> imageData;
    private ExecutorService pool;
    public ClassificationRequest<BufferedImage> nextRequest;
    // The current Cell Phenotypes. The hierarchy
    private final HashMap<PathObjectHierarchy, Map<PathObject, PathClass>> mapPrevious = new HashMap<>();

    /**
     * Getter for instance of Singleton
     * @return
     */
    public static PathClassHandler getInstance(){
        if (pathClassHandler == null){
            pathClassHandler = new PathClassHandler();
        }
        return pathClassHandler;
    }

    /**
     * Sets the ImageData for the instance
     * @param imageData
     */
    public static void setInstanceImageData(ImageData<BufferedImage> imageData){
        pathClassHandler.setImageData(imageData);
    }

    /**
     * Constructor
     */
    private PathClassHandler(){
        pool = Executors.newSingleThreadExecutor(ThreadTools.createThreadFactory("manual-gating", true));
    }

    /**
     * Setter for imageData
     * @param imageData
     */
    public void setImageData(ImageData<BufferedImage> imageData){
        this.imageData = imageData;
    }

    /**
     * Should be called when a cell is no longer a given phenotype.
     * @param cell Cell in question
     * @param oldPhenotypeName the Phenotype the cell is no longer
     */
    public static void removeNoLongerPositive(PathObject cell, String oldPhenotypeName){
        if (checkForSingleClassification(cell.getPathClass(), oldPhenotypeName)) {
            ArrayList<String> name = new ArrayList<>();
            PathClass pathClass = cell.getPathClass();
            boolean isPassed = false;
            while (pathClass != null) {
                if (isPassed) {
                    name.add(pathClass.getName());
                }
                if (pathClass.getName().equals(oldPhenotypeName)) {
                    isPassed = true;
                }
                pathClass = pathClass.getParentClass();
            }
            Collections.reverse(name);
            if (!name.isEmpty()) {
                cell.setPathClass(PathClassFactory.getPathClass(name));
            } else {
                cell.setPathClass(null);
            }
        }
    }

    /**
     * Replaces the old phenotype name to the new phenotype name
     * @param cell Cell in question
     * @param oldPhenotype Old phenotype name
     * @param newPhenotype New phenotype name
     */
    public static void replacePathClass(PathObject cell, String oldPhenotype, String newPhenotype){
        ArrayList<String> name = new ArrayList<>();
        replaceSinglePathClass(cell.getPathClass(), oldPhenotype, newPhenotype, name);
        cell.setPathClass(PathClassFactory.getPathClass(name));
    }


    // Helper for replacePathClass
    private static void replaceSinglePathClass(PathClass pathClass, String oldPhenotype, String newPhenotype, ArrayList<String> name){
        if (pathClass == null){
            Collections.reverse(name);
        } else {
            if (pathClass.getName().equals(oldPhenotype)){
                name.add(newPhenotype);
            } else{
                name.add(pathClass.getName());
            }
            replaceSinglePathClass(pathClass.getParentClass(), oldPhenotype, newPhenotype, name);
        }
    }


    /**
     * Sets the Path class for the given cells
     * @param positive the cells
     * @param phenotypeName the phenotype to be set
     */
    public static void setCellPathClass(Collection<PathObject> positive, String phenotypeName) {
        positive.forEach(it -> {
                    PathClass currentClass = it.getPathClass();
                    PathClass pathClass;

                    if (currentClass == null) {
                        pathClass = PathClassFactory.getPathClass(phenotypeName);
                        it.setPathClass(pathClass);
                    } else {
                        if (!checkForSingleClassification(currentClass, phenotypeName)) {
                            pathClass = PathClassFactory.getDerivedPathClass(
                                    currentClass,
                                    phenotypeName,
                                    null);
                            it.setPathClass(pathClass);
                        }
                    }
                }
        );
    }

    // Helper which checks if a given phenotype is in the path class
    private static boolean checkForSingleClassification(PathClass pathClass, String classificationName) {
        if (pathClass == null)
            return false;
        if (pathClass.getName().equals(classificationName))
            return true;
        return checkForSingleClassification(pathClass.getParentClass(), classificationName);
    }

    /**
     * Resets the path classes of the cells to null
     * @param cells the input cells
     */
    public static void resetCellPathClass(Collection<PathObject> cells){
        cells.forEach(it ->
                it.setPathClass(null)
        );
    }


    /**
     * Store the classifications for the current hierarchy, so these may be reset if the user cancels.
     */
    public static void storeClassification(){
        pathClassHandler.storeClassificationMap();
    }
    // Helper which stores the hierarchy.
    private void storeClassificationMap() {
        PathObjectHierarchy hierarchy = imageData.getHierarchy();
        if (hierarchy == null)
            return;
        List<PathObject> pathObjects = hierarchy.getFlattenedObjectList(null);
        mapPrevious.put(
                hierarchy,
                PathClassifierTools.createClassificationMap(pathObjects)
        );
    }

    /**
     * Restores the path classes of the cells to what it was before the slider was used.
     */
    public static void restorePathClass(){
        pathClassHandler.resetClassification();
    }
    // Helper
    private void resetClassification() {
        PathObjectHierarchy hierarchy = imageData.getHierarchy();
        Map<PathObject, PathClass> map = mapPrevious.get(imageData.getHierarchy());
        // Restore classifications if the user cancelled
        Collection<PathObject> changed = PathClassifierTools.restoreClassificationsFromMap(map);
        if (hierarchy != null && !changed.isEmpty())
            hierarchy.fireObjectClassificationsChangedEvent(this, changed);
    }



    /**
     * Previewer for when the slider is moved
     * Code based of Pete Bankhead's code
     * @param axisTableEntry
     */
    public static void previewThreshold(AxisTableEntry axisTableEntry){
        pathClassHandler.maybePreview(axisTableEntry);
    }
    // Helper for the preview threshold
    private void maybePreview(AxisTableEntry axisTableEntry) {
        nextRequest = getUpdatedRequest(axisTableEntry);
        pool.execute(this::processRequest);
    }
    // Helper which makes the request
    private ClassificationRequest<BufferedImage> getUpdatedRequest(AxisTableEntry axisTableEntry) {
        if (imageData == null) {
            return null;
        }
        var classifier = updateClassifier(axisTableEntry);
        if (classifier == null)
            return null;
        return new ClassificationRequest<>(imageData, classifier);
    }

    private ObjectClassifier<BufferedImage> updateClassifier(AxisTableEntry axisTableEntry) {
        PathObjectFilter filter = PathObjectFilter.CELLS;
        String measurement = axisTableEntry.getFullMeasurementName();
        double threshold = axisTableEntry.getThreshold();
        var classAbove = axisTableEntry.getMarkerName();
        var classEquals = classAbove; // We use >= and if this changes the tooltip must change too!

        if (measurement == null || Double.isNaN(threshold))
            return null;

        return new ObjectClassifiers.ClassifyByMeasurementBuilder<BufferedImage>(measurement)
                .threshold(threshold)
                .filter(filter)
                .above(classAbove)
                .equalTo(classAbove)
                .build();
    }

    // Helper which processes the request
    private synchronized void processRequest() {
        if (nextRequest == null || nextRequest.isComplete())
            return;
        nextRequest.doClassification();
    }

    /**
     * Encapsulate the requirements for a intensity classification into a single object.
     * Taken from Pete Bankhead
     */
    public static class ClassificationRequest<T> {

        private final ImageData<T> imageData;
        private final ObjectClassifier<T> classifier;

        private boolean isComplete = false;

        ClassificationRequest(ImageData<T> imageData, ObjectClassifier<T> classifier) {
            this.imageData = imageData;
            this.classifier = classifier;
        }

        public synchronized void doClassification() {
            var pathObjects = classifier.getCompatibleObjects(imageData);
            classifier.classifyObjects(imageData, pathObjects, true);
            imageData.getHierarchy().fireObjectClassificationsChangedEvent(classifier, pathObjects);
            isComplete = true;
        }

        public synchronized boolean isComplete() {
            return isComplete;
        }

    }


}

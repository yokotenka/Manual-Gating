package com.wehi;

import com.wehi.table.entry.AxisTableEntry;
import com.wehi.table.entry.ChildPhenotypeTableEntry;
import com.wehi.table.entry.PhenotypeEntry;

import com.wehi.table.wrapper.TreeTableCreator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import qupath.lib.classifiers.PathClassifierTools;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.classifiers.object.ObjectClassifiers;
import qupath.lib.common.ThreadTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjectFilter;
import qupath.lib.objects.PathObjects;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.projects.Projects;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The main window which will be displayed upon opening the extension.
 *
 */
// TODO: Need to implement a way to switch between images and remember them
public class ManualGatingWindow implements Runnable, ChangeListener<ImageData<BufferedImage>> {
    // The instance of qupath
    private QuPathGUI qupath;
    // The instance of the qupath viewer
    private QuPathViewer viewer;
    // The currently displayed image data
    private ImageData<BufferedImage> imageData;
    // The currently displayed image
    private ImageServer<BufferedImage> imageServer;
    // The cells in the image
    private Collection<PathObject> cells;

    // Title
    public static final String TITLE = "Manual Gating";

    // The main scene to be displayed in the stage
    private VBox mainBox;
    // The middle bit
    private SplitPane splitPane;
    // The stage
    private Stage stage;

    private Scene scene;

    // The phenotype hierarchy
    private TreeTableCreator<PhenotypeEntry> phenotypeHierarchy;

    // for selecting previously saved options
    private ComboBox<String> manualGatingOptionsBox;
    // button to confirm gating option
    private Button confirmManualGatingOptionButton;

    // apply threshold
    private Button createSubPhenotypeButton;
    private Button updateSubPhenotypeButton;
    private HBox createSubPhenotypeBox;
    private HBox updateSubPhenotypeBox;

    // Where the pane for each phenotype is
    private VBox optionsColumn;

    // The markers in the image
    private ObservableList<String> markers;
    // The  available measurements
    private ObservableList<String> measurements;

    // The current phenotype
    private TreeItem<PhenotypeEntry> currentNode;


    private ExecutorService pool;

    /**
     * Constructor for the ManualGatingWindow
     * @param quPathGUI
     */
    public ManualGatingWindow(QuPathGUI quPathGUI){
        this.qupath = quPathGUI;
    }

    @Override
    public void run() {
        createDialog();
        stage.show();
    }

    /**
     * Creates the stage for the plugin
     */
    public void createDialog(){
        stage = new Stage();
        pool = Executors.newSingleThreadExecutor(ThreadTools.createThreadFactory("manual-gating", true));
        // Update qupath data
        updateQupath();
        // Update title
        updateTitle();

        // Initialise Main Scene
        initialiseMainBox();


        /* For loading previously saved options */
        HBox loadOptionsBox = updateManualGatingOptionsBox();
        Separator sep = new Separator();
        sep.setHalignment(HPos.CENTER);
        mainBox.getChildren().addAll(loadOptionsBox, sep);


        /* The body of the options */
        splitPane = new SplitPane();


        /* Initialise Phenotype Hierarchy */
        Label phenotypeHierarchyLabel = createLabel("Phenotype Hierarchy");
        initialiseTreeTableView();
        splitPane.getItems().add(createColumn(phenotypeHierarchyLabel, phenotypeHierarchy.getTreeTable()));


        /* ***** Options column **************/

        /* Adding the main body to the scene */
        createSubPhenotypeButton = new Button("Create Subphenotypes");
        createSubPhenotypeButton.setOnAction(e -> {
            if (currentNode.getValue().getXAxisMarkerMeasurementName() != null &&
                    currentNode.getValue().getYAxisMarkerMeasurementName()!=null)
                createPhenotypes();
        });
        createSubPhenotypeBox =  createHBox();
        createSubPhenotypeBox.getChildren().addAll(
                createLabel("Creates subphenotypes. Deletes any existing subphenotypes."),
                createSubPhenotypeButton);


        updateSubPhenotypeButton = new Button("Update Subphenotypes");
        updateSubPhenotypeButton.setOnAction(e -> {
            if (currentNode.getValue().getXAxisMarkerMeasurementName() != null &&
                    currentNode.getValue().getYAxisMarkerMeasurementName()!=null)
                updateSubPhenotypes();
        });
        updateSubPhenotypeBox =  createHBox();
        updateSubPhenotypeBox.getChildren().addAll(
                createLabel("Update existing subphenotypes with new thresholds."),
                updateSubPhenotypeButton);


        optionsColumn = createColumn(
                phenotypeHierarchy.getRoot().getValue().getSplitPane(),
                createSubPhenotypeBox,
                updateSubPhenotypeBox
        );

        splitPane.getItems().add(
                optionsColumn
        );



        mainBox.getChildren().add(splitPane);

        Separator sep2 = new Separator();
        sep2.setHalignment(HPos.CENTER);
        HBox saveOptions = createHBox();
        TextField phenotypeHierarchyNameField = new TextField();
        Button saveButton = new Button("Save");
        saveOptions.getChildren().addAll(
                createLabel("Phenotype Hierarchy Name"),
                phenotypeHierarchyNameField,
                saveButton
        );

        saveButton.setOnAction(e -> {
            String fileName = phenotypeHierarchyNameField.getText();
            if (fileName == null){
                Dialogs.showErrorMessage(ManualGatingWindow.TITLE, "Phenotype Hierarchy Name is empty");
            }
            File baseDir = Projects.getBaseDirectory(qupath.getProject());
            try {
                JSONTreeSaver.writeTreeToJSON(phenotypeHierarchy.getTreeTable(), baseDir, fileName);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        });
        mainBox.getChildren().add(sep2);
        mainBox.getChildren().add(saveOptions);

        stage.initOwner(QuPathGUI.getInstance().getStage());

        scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(500);


        if(Dialogs.showYesNoDialog("Manual Gating", "Do you wish to reset existing classifications on your cells?")){
            resetCellPathClass();
            storeClassificationMap(imageData.getHierarchy());
        }
    }

    /**
     * To observe when image changes
     * @param observableValue
     * @param bufferedImageImageData
     * @param t1
     */
    @Override
    public void changed(ObservableValue<? extends ImageData<BufferedImage>> observableValue,
                        ImageData<BufferedImage> bufferedImageImageData, ImageData<BufferedImage> t1) {
        updateQupath();
        updateTitle();
    }


    //********* Update Methods *************//
    private void updateQupath(){
        this.viewer = qupath.getViewer();
        this.imageData = this.viewer.getImageData();
        this.imageServer = this.viewer.getServer();
        this.cells = this.imageData.getHierarchy().getCellObjects();
    }

    private void updateTitle() {
        try {
            stage.setTitle(TITLE + " (" + imageData.getServer().getMetadata().getName() + ")");
        }catch(Exception e){
            stage.setTitle(TITLE);
        }
    }

    private HBox updateManualGatingOptionsBox(){

        HBox loadOptionsBox = createHBox();
        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), JSONTreeSaver.FOLDER);
        manualGatingOptionsBox = new ComboBox<>();
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        manualGatingOptionsBox.setItems(FXCollections.observableArrayList(folderName.list()));
        confirmManualGatingOptionButton = new Button("Load Options");
        confirmManualGatingOptionButton.setOnAction(e -> {
            try {
                currentNode = JSONTreeSaver.readLoadOptions(folderName, manualGatingOptionsBox.getValue(), markers, measurements, cells, stage);
                currentNode.getValue().getXAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(currentNode.getValue().getXAxis()));
                currentNode.getValue().getYAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(currentNode.getValue().getYAxis()));
                phenotypeHierarchy.setRoot(currentNode);


                splitPane.getItems().remove(optionsColumn);

                if (currentNode.getValue().getPane() == null) {
                    optionsColumn = createColumn(
                            currentNode.getValue().createPane(stage),
                            createSubPhenotypeBox,
                            updateSubPhenotypeBox
                    );
                } else{
                    optionsColumn = createColumn(
                            currentNode.getValue().getPane(),
                            createSubPhenotypeBox,
                            updateSubPhenotypeBox
                    );
                }
                splitPane.getItems().add(
                        optionsColumn
                );
                resetClassifications(imageData.getHierarchy(), mapPrevious.get(imageData.getHierarchy()));
                phenotypeHierarchy.getTreeTable().refresh();
            } catch (IOException | JSONException ioException) {
                ioException.printStackTrace();
            }
        });

        loadOptionsBox.getChildren().addAll(
                createLabel("Load saved options"),
                manualGatingOptionsBox,
                confirmManualGatingOptionButton
        );


        return loadOptionsBox;
    }



    //********* JavaFX Helper functions *************//
    private void initialiseMainBox(){
        mainBox = new VBox();
        mainBox.setFillWidth(true);
        mainBox.setSpacing(5);
        mainBox.setPadding(new Insets(10, 10, 10, 10));
    }

    private void initialiseTreeTableView(){
        phenotypeHierarchy = new TreeTableCreator<>();
        phenotypeHierarchy.getTreeTable().setRowFactory(tv -> {
            TreeTableRow<PhenotypeEntry> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    PhenotypeEntry rowData = row.getItem();
                    splitPane.getItems().remove(optionsColumn);

                    if (rowData.getPane() == null) {
                        optionsColumn = createColumn(
                                rowData.createPane(stage),
                                createSubPhenotypeBox,
                                updateSubPhenotypeBox
                        );
                    } else{
                        optionsColumn = createColumn(
                                rowData.getPane(),
                                createSubPhenotypeBox,
                                updateSubPhenotypeBox
                        );
                    }

                    splitPane.getItems().add(
                            optionsColumn
                    );
                    currentNode = row.getTreeItem();
                    resetClassifications(imageData.getHierarchy(), mapPrevious.get(imageData.getHierarchy()));
                }
            });
            return row ;
        });

        phenotypeHierarchy.getTreeTable().prefHeightProperty().bind(stage.heightProperty());
        phenotypeHierarchy.addColumn("Phenotype", "phenotypeName", 0.2);
        phenotypeHierarchy.addColumn("Positive Markers", "positiveMarkers", 0.4);
        phenotypeHierarchy.addColumn("Negative Markers", "negativeMarkers", 0.4);

        extractMarkers();
        extractMarkerMeasurements();

        PhenotypeEntry currentPhenotypeEntry = new PhenotypeEntry(
                cells,
                "Cell",
                null,
                null,
                markers,
                measurements,
                stage
        );
        currentNode = new TreeItem<>(
                currentPhenotypeEntry
            );
        phenotypeHierarchy.setRoot(currentNode);
        currentPhenotypeEntry.getXAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(currentPhenotypeEntry.getXAxis()));
        currentPhenotypeEntry.getYAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(currentPhenotypeEntry.getYAxis()));
    }
    private void firePaneChange(){

    }

    public static VBox createColumn(Node... nodes){
        VBox column = createVBox();
        column.getChildren().addAll(nodes);
        column.setFillWidth(true);
        return column;
    }

    /**
     * Method to extract the markers
     */
    public void extractMarkers(){
        markers = FXCollections.observableArrayList();
        for (int i=0; i < imageServer.nChannels(); i++){
            markers.add(imageServer.getChannel(i).getName());
        }
    }

    /**
     * Method to extract the measurement names
     */
    public void extractMarkerMeasurements() {

        // Do something for when no cell detected
        if (cells == null) {
            return;
        }
        // Gets a cell
        PathObject cell = (PathObject) cells.toArray()[0];
        String markerName = imageServer.getChannel(0).getName();
        List<String> measurementList = cell.getMeasurementList().getMeasurementNames();

        // Potentially could be a source of error #################################################
        measurements = FXCollections.observableList(measurementList.stream()
                .parallel()
                .filter(it -> it.contains(markerName + ":"))
                .map(it -> it.substring(markerName.length() + 2))
                .collect(Collectors.toList()));
    }


    /**
     * Static method to create a label
     * @param msg the message to be displayed
     * @return label
     */
    public static Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    /**
     * Static helper to create a HBox
     * @return
     */
    public static HBox createHBox(){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        return hBox;
    }

    /**
     * Static helper to create a VBox
     * @return
     */
    public static VBox createVBox(){
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        return vBox;
    }


    /**
     * Action taken upon pressing the button applyThreshold
     */
    public void createPhenotypes(){
        // List of new phenotypes
        currentNode.getValue().setChildPhenotypeThresholds();
        ObservableList<TreeItem<PhenotypeEntry>> newPhenotypes = FXCollections.observableArrayList();
        for (ChildPhenotypeTableEntry entry : currentNode.getValue().getChildPhenotypeTableWrapper().getTable().getItems()){
            if (entry.getIsSelected() && entry.getPhenotypeName() != null){

                ArrayList<String> newPositiveMarkers;
                ArrayList<String> newNegativeMarkers;
                Collection<PathObject> filteredCells;

                if (entry.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
                     filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) > entry.getThresholdTwo())
                            .collect(Collectors.toList());


                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newPositiveMarkers = new ArrayList<>(currentNode.getValue().getPositiveMarkers());
                    } else{
                        newPositiveMarkers = new ArrayList<>();
                    }
                    // Checks if markerOne is already in the positive array list
                    if (!newPositiveMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerOne()))){
                        newPositiveMarkers.add(entry.getMarkerOne());
                    }
                    if (!newPositiveMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerTwo()))){
                        newPositiveMarkers.add(entry.getMarkerTwo());
                    }
                    if (currentNode.getValue().getNegativeMarkers()==null){
                        newNegativeMarkers = null;
                    }else{
                        newNegativeMarkers = new ArrayList<>(currentNode.getValue().getNegativeMarkers());
                    }
                } else if (entry.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE){
                    filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) < entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo())
                            .collect(Collectors.toList());

                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newNegativeMarkers = new ArrayList<>(currentNode.getValue().getNegativeMarkers());
                    } else{
                        newNegativeMarkers = new ArrayList<>();
                    }
                    if (newNegativeMarkers.stream().noneMatch(p -> p.equals(entry.getMarkerOne()))){
                        newNegativeMarkers.add(entry.getMarkerOne());
                    }
                    if (newNegativeMarkers.stream().noneMatch(p -> p.equals(entry.getMarkerTwo()))){
                        newNegativeMarkers.add(entry.getMarkerTwo());
                    }
                    if (currentNode.getValue().getNegativeMarkers()==null){
                        newPositiveMarkers = null;
                    }else{
                        newPositiveMarkers = new ArrayList<>(currentNode.getValue().getNegativeMarkers());
                    }
                } else {
                    filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo())
                            .collect(Collectors.toList());

                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newPositiveMarkers = new ArrayList<>(currentNode.getValue().getPositiveMarkers());
                    } else{
                        newPositiveMarkers = new ArrayList<>();
                    }
                    // Checks if markerOne is already in the positive array list
                    if (newPositiveMarkers.stream().noneMatch(p -> p.equals(entry.getMarkerOne()))){
                        newPositiveMarkers.add(entry.getMarkerOne());
                    }

                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newNegativeMarkers = new ArrayList<>(currentNode.getValue().getNegativeMarkers());
                    } else{
                        newNegativeMarkers = new ArrayList<>();
                    }
                    newNegativeMarkers.add(entry.getMarkerTwo());
                    // Checks if markerTwo is already in the negative array list
                    if (newNegativeMarkers.stream().noneMatch(p -> p.equals(entry.getMarkerTwo()))){
                        newNegativeMarkers.add(entry.getMarkerTwo());
                    }
                }
                PhenotypeEntry newPhenotype = new PhenotypeEntry(
                        filteredCells,
                        entry.getPhenotypeName(),
                        newPositiveMarkers,
                        newNegativeMarkers,
                        markers,
                        measurements,
                        stage,
                        entry.getMarkerOne(),
                        entry.getMarkerTwo(),
                        entry.getMarkerCombination()
                );
                newPhenotype.getXAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(newPhenotype.getXAxis()));
                newPhenotype.getYAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(newPhenotype.getYAxis()));
                newPhenotypes.add(new TreeItem<>(newPhenotype));
                resetClassifications(imageData.getHierarchy(), mapPrevious.get(imageData.getHierarchy()));
                setCellPathClass(filteredCells, entry.getPhenotypeName());
                storeClassificationMap(imageData.getHierarchy());
            }
        }
        currentNode.getChildren().setAll(newPhenotypes);
    }

    public void updateSubPhenotypes(){
        currentNode.getValue().setChildPhenotypeThresholds();
        resetClassifications(imageData.getHierarchy(), mapPrevious.get(imageData.getHierarchy()));
        ArrayList<PathObject> filteredCells = new ArrayList<>();
        for (ChildPhenotypeTableEntry entry : currentNode.getValue().getChildPhenotypeTableWrapper().getTable().getItems()) {
            if (entry.getIsSelected() && entry.getPhenotypeName() != null) {
                for (TreeItem<PhenotypeEntry> subPhenotype : currentNode.getChildren()){
                    Integer lengthPositive = subPhenotype.getValue().getPositiveMarkers().size();
                    Integer lengthNegative = subPhenotype.getValue().getNegativeMarkers().size();

                    if (!entry.getMarkerOne().equals(subPhenotype.getValue().getSplitMarkerOne()) ||
                            !entry.getMarkerTwo().equals(subPhenotype.getValue().getSplitMarkerTwo())){
                        continue;
                    }
                    if (entry.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
                        //update PhenotypeName
                        if(lengthPositive>1) {
                            if (subPhenotype.getValue().getPositiveMarkers().get(lengthPositive - 2).equals(entry.getMarkerOne()) &&
                                    subPhenotype.getValue().getPositiveMarkers().get(lengthPositive - 1).equals(entry.getMarkerTwo())) {
                                for (PathObject cell : currentNode.getValue().getCells()){
                                    if (cell.getMeasurementList().getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne()
                                            && cell.getMeasurementList().getMeasurementValue(entry.getMeasurementTwo()) > entry.getThresholdTwo()){
                                        filteredCells.add(cell);
                                        replacePathClass(cell, subPhenotype.getValue().getPhenotypeName(), entry.getPhenotypeName());
                                    } else{
                                        removeNoLongerPositive(cell, subPhenotype.getValue().getPhenotypeName());
                                    }
                                }
                                subPhenotype.getValue().setPhenotypeName(entry.getPhenotypeName());
                                //set CellPath class after updating tree
                                setCellPathClass(filteredCells, entry.getPhenotypeName());
                                storeClassificationMap(imageData.getHierarchy());
                            }
                        }
                    } else if (entry.getMarkerCombination() == ChildPhenotypeTableEntry.MARKER_COMBINATION.TWO_NEGATIVE) {
                        if(lengthNegative>1) {
                            if (subPhenotype.getValue().getNegativeMarkers().get(lengthNegative - 2).equals(entry.getMarkerOne()) &&
                                    subPhenotype.getValue().getNegativeMarkers().get(lengthNegative - 1).equals(entry.getMarkerTwo())) {

                                for (PathObject cell : currentNode.getValue().getCells()){
                                    if (cell.getMeasurementList().getMeasurementValue(entry.getMeasurementOne()) < entry.getThresholdOne()
                                            && cell.getMeasurementList().getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo()){
                                        filteredCells.add(cell);
                                        replacePathClass(cell, subPhenotype.getValue().getPhenotypeName(), entry.getPhenotypeName());
                                    } else{
                                        removeNoLongerPositive(cell, subPhenotype.getValue().getPhenotypeName());
                                    }
                                }
                                subPhenotype.getValue().setCells(filteredCells);
                                subPhenotype.getValue().setPhenotypeName(entry.getPhenotypeName());
                                //set CellPath class after updating tree
                                setCellPathClass(filteredCells, entry.getPhenotypeName());
                                storeClassificationMap(imageData.getHierarchy());
                            }
                        }
                    } else {
                        if (subPhenotype.getValue().getNegativeMarkers().get(lengthNegative - 1).equals(entry.getMarkerTwo()) &&
                                subPhenotype.getValue().getPositiveMarkers().get(lengthPositive - 1).equals(entry.getMarkerOne())) {
                            for (PathObject cell : currentNode.getValue().getCells()){
                                if (cell.getMeasurementList().getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne()
                                        && cell.getMeasurementList().getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo()){
                                    filteredCells.add(cell);
                                    replacePathClass(cell, subPhenotype.getValue().getPhenotypeName(), entry.getPhenotypeName());
                                } else{
                                    removeNoLongerPositive(cell, subPhenotype.getValue().getPhenotypeName());
                                }
                            }
                            subPhenotype.getValue().setCells(filteredCells);
                            subPhenotype.getValue().setPhenotypeName(entry.getPhenotypeName());

                            //set CellPath class after updating tree
                            setCellPathClass(filteredCells, subPhenotype.getValue().getPhenotypeName());
                            storeClassificationMap(imageData.getHierarchy());
                        }


                    }

                    subPhenotype.getValue().getXAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(subPhenotype.getValue().getXAxis()));
                    subPhenotype.getValue().getYAxisSlider().valueProperty().addListener((v, o, n) -> maybePreview(subPhenotype.getValue().getYAxis()));
                    phenotypeHierarchy.getTreeTable().refresh();
                }
            }
        }
    }

    private void removeNoLongerPositive(PathObject cell, String oldPhenotypeName){
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


    private void replacePathClass(PathObject cell, String oldPhenotype, String newPhenotype){
        ArrayList<String> name = new ArrayList<>();
        replaceSinglePathClass(cell.getPathClass(), oldPhenotype, newPhenotype, name);
        cell.setPathClass(PathClassFactory.getPathClass(name));
    }


    private void replaceSinglePathClass(PathClass pathClass, String oldPhenotype, String newPhenotype, ArrayList<String> name){
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


    // Method for setting cell path class
    private void setCellPathClass(Collection<PathObject> positive, String phenotypeName) {
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

    private boolean checkForSingleClassification(PathClass pathClass, String classificationName) {
        if (pathClass == null)
            return false;
        if (pathClass.getName().equals(classificationName))
            return true;
        return checkForSingleClassification(pathClass.getParentClass(), classificationName);
    }

    private void resetCellPathClass(){
        cells.forEach(it ->
                    it.setPathClass(null)
                );
    }


    // The current Cell Phenotypes. The hierarchy
    private HashMap<PathObjectHierarchy, Map<PathObject, PathClass>> mapPrevious = new HashMap<>();

    /**
     * Store the classifications for the current hierarchy, so these may be reset if the user cancels.
     */
    public void storeClassificationMap(PathObjectHierarchy hierarchy) {
        if (hierarchy == null)
            return;
        List<PathObject> pathObjects = hierarchy.getFlattenedObjectList(null);
        mapPrevious.put(
                hierarchy,
                PathClassifierTools.createClassificationMap(pathObjects)
        );
    }

    public void resetClassifications(PathObjectHierarchy hierarchy, Map<PathObject, PathClass> mapPrevious) {
        // Restore classifications if the user cancelled
        Collection<PathObject> changed = PathClassifierTools.restoreClassificationsFromMap(mapPrevious);
        if (hierarchy != null && !changed.isEmpty())
            hierarchy.fireObjectClassificationsChangedEvent(this, changed);
    }




    private ClassificationRequest<BufferedImage> nextRequest;
    void maybePreview(AxisTableEntry axisTableEntry) {
        nextRequest = getUpdatedRequest(axisTableEntry);
        pool.execute(this::processRequest);
    }

    ClassificationRequest<BufferedImage> getUpdatedRequest(AxisTableEntry axisTableEntry) {
        if (imageData == null) {
            return null;
        }
        var classifier = updateClassifier(axisTableEntry);
        if (classifier == null)
            return null;
        return new ClassificationRequest<>(imageData, classifier);
    }

    ObjectClassifier<BufferedImage> updateClassifier(AxisTableEntry axisTableEntry) {
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

    synchronized void processRequest() {
        if (nextRequest == null || nextRequest.isComplete())
            return;
        nextRequest.doClassification();
    }

    /**
     * Encapsulate the requirements for a intensity classification into a single object.
     */
    static class ClassificationRequest<T> {

        private ImageData<T> imageData;
        private ObjectClassifier<T> classifier;

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

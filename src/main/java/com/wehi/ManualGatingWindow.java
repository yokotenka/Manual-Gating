package com.wehi;

import com.wehi.TableViewHelpers.*;
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
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.projects.Projects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private final String title = "Manual Gating";

    // The main scene to be displayed in the stage
    private VBox mainBox;
    // The middle bit
    private SplitPane splitPane;
    // The stage
    private Stage stage;

    // The phenotype hierarchy
    private TreeTableCreator<PhenotypeEntry> phenotypeHierarchy;

    // for selecting previously saved options
    private ComboBox<String> manualGatingOptionsBox;
    // button to confirm gating option
    private Button confirmManualGatingOptionButton;

    // apply threshold
    private Button applyThresholdButton;
    private HBox applyThresholdBox;

    // Where the pane for each phenotype is
    private VBox optionsColumn;

    // The markers in the image
    private ObservableList<String> markers;
    // The  available measurements
    private ObservableList<String> measurements;

    // The current phenotype
    private TreeItem<PhenotypeEntry> currentNode;

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

        // Update qupath data
        updateQupath();
        // Update title
        updateTitle();

        // Initialise Main Scene
        initialiseMainBox();


        /* For loading previously saved options */
        HBox loadOptionsBox = createHBox();
        updateManualGatingOptionsBox();
        confirmManualGatingOptionButton = new Button("Load Options");

        loadOptionsBox.getChildren().addAll(
                                            createLabel("Load saved options"),
                                            manualGatingOptionsBox,
                                            confirmManualGatingOptionButton
                                        );
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
        applyThresholdButton = new Button("Apply Threshold");
        applyThresholdButton.setOnAction(e -> {
            if (currentNode.getValue().getXAxisMarkerMeasurementName() != null &&
                    currentNode.getValue().getYAxisMarkerMeasurementName()!=null)
                createPhenotypes();
        });
        applyThresholdBox =  createHBox();
        applyThresholdBox.getChildren().add(applyThresholdButton);
        optionsColumn = createColumn(
                phenotypeHierarchy.getRoot().getValue().createPane(stage),
                applyThresholdBox
        );

        splitPane.getItems().add(
                optionsColumn
        );

        mainBox.getChildren().add(splitPane);
        stage.initOwner(QuPathGUI.getInstance().getStage());
        stage.setScene(new Scene(mainBox));
        stage.setWidth(850);
        stage.setHeight(500);
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
            stage.setTitle(title + " (" + imageData.getServer().getMetadata().getName() + ")");
        }catch(Exception e){
            stage.setTitle(title);
        }
    }

    private void updateManualGatingOptionsBox(){
        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), "Manual Gating Options");
        manualGatingOptionsBox = new ComboBox<>();
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        manualGatingOptionsBox.setItems(FXCollections.observableArrayList(folderName.list()));

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
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PhenotypeEntry rowData = row.getItem();
                    splitPane.getItems().remove(optionsColumn);

                    if (rowData.getPane() == null) {
                        optionsColumn = createColumn(
                                rowData.createPane(stage),
                                applyThresholdBox
                        );
                    } else{
                        optionsColumn = createColumn(
                                rowData.getPane(),
                                applyThresholdBox
                        );
                    }

                    splitPane.getItems().add(
                            optionsColumn
                    );
                    currentNode = row.getTreeItem();
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
                measurements
        );
        currentNode = new TreeItem<>(
                currentPhenotypeEntry
            );
        phenotypeHierarchy.setRoot(currentNode);
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
        ObservableList<TreeItem<PhenotypeEntry>> newPhenotypes = FXCollections.observableArrayList();
        for (PhenotypeCreationTableEntry entry : currentNode.getValue().getPhenotypeCreationTableCreator().getTable().getItems()){
            if (entry.getIsSelected() && entry.getPhenotypeName() != null){

                ArrayList<String> newPositiveMarkers;
                ArrayList<String> newNegativeMarkers;
                Collection<PathObject> filteredCells;

                if (entry.getMarkerCombination() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
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
                } else if (entry.getMarkerCombination() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_NEGATIVE){
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
                    if (!newNegativeMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerOne()))){
                        newNegativeMarkers.add(entry.getMarkerOne());
                    }
                    if (!newNegativeMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerTwo()))){
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
                    if (!newPositiveMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerOne()))){
                        newPositiveMarkers.add(entry.getMarkerOne());
                    }

                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newNegativeMarkers = new ArrayList<>(currentNode.getValue().getNegativeMarkers());
                    } else{
                        newNegativeMarkers = new ArrayList<>();
                    }
                    newNegativeMarkers.add(entry.getMarkerTwo());
                    // Checks if markerTwo is already in the negative array list
                    if (!newNegativeMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerTwo()))){
                        newNegativeMarkers.add(entry.getMarkerTwo());
                    }
                }



                PhenotypeEntry newPhenotype = new PhenotypeEntry(
                        filteredCells,
                        entry.getPhenotypeName(),
                        newPositiveMarkers,
                        newNegativeMarkers,
                        markers,
                        measurements
                );
                newPhenotypes.add(new TreeItem<>(newPhenotype));
                setCellPathClass(filteredCells, entry.getPhenotypeName());
            }
        }
        currentNode.getChildren().setAll(newPhenotypes);
    }


    // Method for setting cell path class
    private void setCellPathClass(Collection<PathObject> positive, String phenotypeName) {
        positive.forEach(it -> {
                    PathClass currentClass = it.getPathClass();
                    PathClass pathClass;

                    if (currentClass == null) {
                        pathClass = PathClassFactory.getPathClass(phenotypeName);
                    } else {
                        pathClass = PathClassFactory.getDerivedPathClass(
                                currentClass,
                                phenotypeName,
                                null);
                    }
                    it.setPathClass(pathClass);
                }
        );
    }
}

package com.wehi;

import com.wehi.TableTreeViewHelpers.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
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
import qupath.lib.projects.Projects;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ManualGatingWindow implements Runnable, ChangeListener<ImageData<BufferedImage>> {
    private QuPathGUI qupath;
    private QuPathViewer viewer;
    private ImageData<BufferedImage> imageData;
    private ImageServer<BufferedImage> imageServer;
    private Collection<PathObject> cells;

    private final String title = "Manual Gating";


    private VBox mainBox;
    private SplitPane splitPane;
    private Stage stage;
    private TreeTableCreator<PhenotypeEntry> phenotypeHierarchy;

    private ComboBox<String> manualGatingOptionsBox;
    private Button confirmManualGatingOptionButton;

    private Button applyThresholdButton;
    private HBox applyThresholdBox;
    private VBox optionsColumn;

    private ObservableList<String> markers;
    private ObservableList<String> measurements;

//    private PhenotypeEntry currentPhenotypeEntry;
    private TreeItem<PhenotypeEntry> currentNode;

    public ManualGatingWindow(QuPathGUI quPathGUI){
        this.qupath = quPathGUI;
    }

    @Override
    public void run() {
        createDialog();
        stage.show();
    }


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


    public void extractMarkers(){
        markers = FXCollections.observableArrayList();
        for (int i=0; i < imageServer.nChannels(); i++){
            markers.add(imageServer.getChannel(i).getName());
        }
    }

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



    public static Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    public static HBox createHBox(){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        return hBox;
    }

    public static VBox createVBox(){
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        return vBox;
    }


    public void createPhenotypes(){
        ObservableList<TreeItem<PhenotypeEntry>> newPhenotypes = FXCollections.observableArrayList();
        for (PhenotypeCreationTableEntry entry : currentNode.getValue().getPhenotypeCreationTableCreator().getItems()){
            if (entry.getIsSelected()){
                if (entry.getMARKERCOMBINATION() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_POSITIVE){
                    Collection<PathObject> filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) > entry.getThresholdTwo())
                            .collect(Collectors.toList());

                    ArrayList<String> newPositiveMarkers;
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

                    PhenotypeEntry newPhenotype = new PhenotypeEntry(
                            filteredCells,
                            entry.getPhenotypeName(),
                            newPositiveMarkers,
                            new ArrayList<>(currentNode.getValue().getNegativeMarkers()),
                            markers,
                            measurements
                    );
                    newPhenotypes.add(new TreeItem<>(newPhenotype));
                } else if (entry.getMARKERCOMBINATION() == PhenotypeCreationTableEntry.MARKER_COMBINATION.TWO_NEGATIVE){
                    Collection<PathObject> filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) < entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo())
                            .collect(Collectors.toList());

                    ArrayList<String> newNegativeMarkers;
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


                    PhenotypeEntry newPhenotype = new PhenotypeEntry(
                            filteredCells,
                            entry.getPhenotypeName(),
                            new ArrayList<>(currentNode.getValue().getPositiveMarkers()),
                            newNegativeMarkers,
                            markers,
                            measurements
                    );
                    newPhenotypes.add(new TreeItem<>(newPhenotype));
                } else {
                    Collection<PathObject> filteredCells = currentNode.getValue().getCells()
                            .stream()
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementOne()) > entry.getThresholdOne())
                            .filter(p -> p.getMeasurementList()
                                    .getMeasurementValue(entry.getMeasurementTwo()) < entry.getThresholdTwo())
                            .collect(Collectors.toList());


                    ArrayList<String> newPositiveMarkers;
                    if (currentNode.getValue().getPositiveMarkers() != null) {
                        newPositiveMarkers = new ArrayList<>(currentNode.getValue().getPositiveMarkers());
                    } else{
                        newPositiveMarkers = new ArrayList<>();
                    }

                    // Checks if markerOne is already in the positive array list
                    if (!newPositiveMarkers.stream().anyMatch(p -> p.equals(entry.getMarkerOne()))){
                        newPositiveMarkers.add(entry.getMarkerOne());
                    }


                    ArrayList<String> newNegativeMarkers;
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


                    PhenotypeEntry newPhenotype = new PhenotypeEntry(
                            filteredCells,
                            entry.getPhenotypeName(),
                            newPositiveMarkers,
                            newNegativeMarkers,
                            markers,
                            measurements
                    );
                    Dialogs.showInfoNotification(title, entry.getMeasurementOne());
                    Dialogs.showInfoNotification(title, entry.getMeasurementTwo());
                    Dialogs.showInfoNotification(title, String.valueOf(entry.getThresholdOne()));
                    Dialogs.showInfoNotification(title, String.valueOf(0 == entry.getThresholdTwo()));
                    newPhenotypes.add(new TreeItem<>(newPhenotype));
                }

            }
        }
        currentNode.getChildren().setAll(newPhenotypes);
    }


    // Tree view helpers *****

}

package com.wehi;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.FunctionalMarkerEntry;
import com.wehi.table.wrapper.FunctionalMarkerTreeTableWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONException;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.projects.Projects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.wehi.JavaFXHelpers.*;

/**
 * Runnable class for initialising the stage for the plugin
 */
public class FunctionalMarkerThresholdWindow implements Runnable{
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
    private final String TITLE = "Functional Marker Threshold";

    // The main scene to be displayed in the stage
    private VBox mainBox;
    // The middle bit
    private SplitPane splitPane;
    // The stage
    private Stage stage;
    // The scene
    private Scene scene;
    // The hBox for loading the options
    private HBox loadBox;
    // The left side of the splitPane
    private VBox leftHandSide;
    // The right side of the splitPane
    private VBox rightHandSide;
    // The current functional marker entry
    private FunctionalMarkerEntry currentFunctionalMarkerEntry;

    // Available markers
    private ObservableList<String> markers;
    // Available measurements
    private ObservableList<String> measurements;

    // List of all functional markers
    private FunctionalMarkerTreeTableWrapper functionalPhenotypeListTableWrapper;

    /**
     * Constructor
     * @param qupath the instance of qupath
     */
    public FunctionalMarkerThresholdWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }

    /**
     * Runnable
     */
    @Override
    public void run() {
        try {
            createDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PathClassHandler.storeClassification();
        stage.show();
    }

    /**
     * Creates the stage which will be displayed
     * @throws IOException May be thrown when the load box tries to load
     */
    public void createDialog() throws IOException {
        stage = new Stage();
        initialiseMainBox();
        updateQuPath();
        initialiseLoadBox();

        Separator sep1 = new Separator();
        sep1.setHalignment(HPos.CENTER);

        initialiseFunctionalPhenotypeTable();

        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        rightHandSide = createVBox();
        rightHandSide.getChildren().addAll(currentFunctionalMarkerEntry.getSplitPane(), thresholdBox());

        leftHandSide = createVBox();
        leftHandSide.getChildren().addAll(functionalPhenotypeListTableWrapper.getTreeTable(), addRowsBox());
        splitPane.getItems().addAll(leftHandSide, rightHandSide);

        mainBox.getChildren().addAll(loadBox, sep1, splitPane, saveRow());

        stage.initOwner(QuPathGUI.getInstance().getStage());

        scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(500);

    }


    //********* JavaFX Helper functions *************//
    private void initialiseMainBox(){
        mainBox = new VBox();
        mainBox.setFillWidth(true);
        mainBox.setSpacing(5);
        mainBox.setPadding(new Insets(10, 10, 10, 10));
    }

    //********* Update Methods *************//
    private void updateQuPath(){
        this.viewer = qupath.getViewer();
        this.imageData = this.viewer.getImageData();
        this.imageServer = this.viewer.getServer();
        this.cells = this.imageData.getHierarchy().getCellObjects();

        PathClassHandler.getInstance();
        PathClassHandler.setInstanceImageData(imageData);
    }

    public void initialiseFunctionalPhenotypeTable(){
        functionalPhenotypeListTableWrapper = new FunctionalMarkerTreeTableWrapper();
        functionalPhenotypeListTableWrapper.getTreeTable().prefHeightProperty().bind(stage.heightProperty());
        currentFunctionalMarkerEntry = new FunctionalMarkerEntry(
                cells,
                "Marker not selected",
                markers,
                measurements,
                stage
        );
        functionalPhenotypeListTableWrapper.add(currentFunctionalMarkerEntry);
        functionalPhenotypeListTableWrapper.getTreeTable().setRowFactory(tv -> {
            TreeTableRow<FunctionalMarkerEntry> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    if (!row.getItem().isDisplayable()){
                        return;
                    }
                    currentFunctionalMarkerEntry = row.getItem();
                    splitPane.getItems().remove(rightHandSide);
                    rightHandSide = createVBox();
                    rightHandSide.getChildren().addAll(currentFunctionalMarkerEntry.getSplitPane(), thresholdBox());
                    splitPane.getItems().add(
                            rightHandSide
                    );
                    PathClassHandler.restorePathClass();
                }
            });
            return row ;
        });
        functionalPhenotypeListTableWrapper.getTreeTable().refresh();
    }

    /**
     * Initialises the load box
     * @throws IOException Thrown when trying to load in the classifiers
     */
    public void initialiseLoadBox() throws IOException {
        Label loadLabel = createLabel("Load saved options");
        Button loadButton = new Button("Load Options");
        ComboBox<String> phenotypeOptions = new ComboBox<>();

        extractMarkerMeasurements();
        extractMarkers();

        Collection<String> options = qupath.getProject().getObjectClassifiers().getNames();
        ArrayList<String> arr = options.stream().filter(e -> e.endsWith(ClassifierSaver.SIGNATURE)).map(e -> e.substring(0, e.length()-1)).collect(Collectors
                .toCollection(ArrayList::new));

        phenotypeOptions.setItems(FXCollections.observableArrayList(arr));
        loadButton.setOnAction(e -> {
                    if (phenotypeOptions.getSelectionModel().isEmpty()) {
                        Dialogs.showErrorMessage(TITLE, "Please select a file");
                        return;
                    }

                    var objectClassifier = Projects.getBaseDirectory(qupath.getProject());
                    File folder = new File(objectClassifier.toString(), "classifiers");
                    File folder2 = new File(folder, "object_classifiers");

                    try {
                        functionalPhenotypeListTableWrapper.clearChildren();
                        ClassifierSaver.loadOptions(
                                folder2,
                                phenotypeOptions.getValue(),
                                markers,
                                measurements,
                                cells,
                                stage,
                                functionalPhenotypeListTableWrapper
                        );
                        functionalPhenotypeListTableWrapper.getTreeTable().refresh();
                    } catch (IOException | JSONException ioException) {
                        ioException.printStackTrace();
                    }
                }
            );

        loadBox = createHBox();
        loadBox.getChildren().addAll(loadLabel, phenotypeOptions, loadButton);
    }

    /**
     * Initialises the apply threshold button
     * @return
     */
    public HBox thresholdBox(){
        HBox subPhenotypeBox = createHBox();

        Button applyThreshold = new Button("Apply Threshold");
        applyThreshold.setOnAction(e ->{
            if (functionalPhenotypeListTableWrapper.contains(currentFunctionalMarkerEntry.getMarker(), currentFunctionalMarkerEntry)){
                Dialogs.showErrorMessage(
                        "Functional Marker",
                        "Threshold for " + currentFunctionalMarkerEntry.getMarker() + " already exists"
                );
                return;
            }
            currentFunctionalMarkerEntry.createPhenotypes();
            functionalPhenotypeListTableWrapper.getTreeTable().refresh();
        });


        subPhenotypeBox.getChildren().addAll(
                createLabel("Apply threshold to cells"),
                applyThreshold
        );
        return subPhenotypeBox;
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
    public void extractMarkerMeasurements(){
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
     * Method for creating the button for adding and removing rows
     * @return
     */
    public HBox addRowsBox(){
        HBox addRemoveRows = createHBox();

        Button addRowsButton = new Button("Add Functional Marker");
        addRowsButton.setOnAction(e -> {
            FunctionalMarkerEntry newMarkerEntry = new FunctionalMarkerEntry(
                    cells,
                    "Marker not selected",
                    markers,
                    measurements,
                    stage
            );
            functionalPhenotypeListTableWrapper.add(newMarkerEntry);
            functionalPhenotypeListTableWrapper.getTreeTable().refresh();
        });

        Button removeSelectedRow = new Button("Remove Selected row");

        removeSelectedRow.setOnAction(e -> {
            if (functionalPhenotypeListTableWrapper.checkIfToBeRemovedIsCurrent(currentFunctionalMarkerEntry)){
                splitPane.getItems().remove(rightHandSide);
                rightHandSide = createVBox();
                splitPane.getItems().add(rightHandSide);
            }
            functionalPhenotypeListTableWrapper.removeSelected();
        });
        addRemoveRows.getChildren().addAll(addRowsButton, removeSelectedRow);
        return addRemoveRows;
    }

    /**
     * Method for creating the button for saving the classifier
     * @return
     */
    public HBox saveRow(){
        HBox saveRowBox = createHBox();
        Label saveRowLabel = createLabel("Classifier Name");
        TextField enterFileName = new TextField();
        Button saveButton = new Button("Save");

        saveButton.setOnAction(e -> {
            var project = qupath.getProject();
            // Check if project is null
            if (project == null) {
                Dialogs.showErrorMessage(TITLE, "You need a project to save this classifier!");
                return;
            }
            // Get the classifier name
            String name = GeneralTools.stripInvalidFilenameChars(enterFileName.getText());
            if (name.isBlank()) {
                Dialogs.showErrorMessage(TITLE, "Please enter a name for the classifier!");
                return;
            }

            functionalPhenotypeListTableWrapper.saveTree(qupath, name);
        });

        saveRowBox.getChildren().addAll(saveRowLabel, enterFileName, saveButton);
        return saveRowBox;
    }

}

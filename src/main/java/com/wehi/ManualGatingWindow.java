package com.wehi;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.io.GatingIO;
import com.wehi.table.entry.PhenotypeEntry;

import com.wehi.table.wrapper.TreeTableWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONException;
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
import java.util.*;

import static com.wehi.JavaFXHelpers.*;

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
    private TreeTableWrapper<PhenotypeEntry> phenotypeHierarchy;

    // for selecting previously saved options
    private ComboBox<String> manualGatingOptionsBox;
    // button to confirm gating option
    private Button confirmManualGatingOptionButton;

    // apply threshold
    private Button createSubPhenotypeButton;
    private Button updateSubPhenotypeButton;
    private Button highlight;
    private HBox createSubPhenotypeBox;
    private HBox updateSubPhenotypeBox;

    // Where the pane for each phenotype is
    private VBox optionsColumn;

    // The markers in the image
    private ObservableList<String> markers;
    // The  available measurements
    private ObservableList<String> measurements;

    private PhenotypeEntry currentPhenotype;

    private HBox highlightCellBox;
    // The current phenotype
//    private TreeItem<PhenotypeEntry> currentNode;

    /**
     * Constructor for the ManualGatingWindow
     * @param quPathGUI
     */
    public ManualGatingWindow(QuPathGUI quPathGUI){
        this.qupath = quPathGUI;
    }

    @Override
    public void run() {
        if(Dialogs.showYesNoDialog("Manual Gating", "Do you wish to reset existing classifications on your cells?")){
            createDialog();
            initialisePathClassHandler();
            stage.show();
        }
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

        createCreateSubPhenotypeBox();
        createUpdateSubPhenotypeBox();
        createHighlightCellBox();



        optionsColumn = createColumn(
                phenotypeHierarchy.getRoot().getValue().getSplitPane(),
                createSubPhenotypeBox,
                updateSubPhenotypeBox,
                highlightCellBox
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
                GatingIO.writeTreeToJSON(phenotypeHierarchy.getTreeTable(), baseDir, fileName);
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

    }

    public void createHighlightCellBox(){
        highlight = new Button("Highlight");
        highlight.setOnAction(e -> {
            imageData.getHierarchy().getSelectionModel().selectObjects(currentPhenotype.getCells());
        });

        Button unHighlight = new Button("Un-Highlight");
        unHighlight.setOnAction(e -> {
           imageData.getHierarchy().getSelectionModel().deselectObjects(cells);
        });

        highlightCellBox = createHBox();
        highlightCellBox.getChildren().addAll(highlight, unHighlight);
    }

    public void createCreateSubPhenotypeBox(){
        /* Adding the main body to the scene */
        createSubPhenotypeButton = new Button("Create Subphenotypes");
        createSubPhenotypeButton.setOnAction(e -> {
            if (currentPhenotype.getXAxisMarkerMeasurementName() != null &&
                    currentPhenotype.getYAxisMarkerMeasurementName()!=null) {
                currentPhenotype.createPhenotypes();
                phenotypeHierarchy.getTreeTable().refresh();
            }
        });
        createSubPhenotypeBox =  createHBox();
        createSubPhenotypeBox.getChildren().addAll(
                createLabel("Creates subphenotypes. Deletes any existing subphenotypes."),
                createSubPhenotypeButton);
    }

    public void createUpdateSubPhenotypeBox(){
        updateSubPhenotypeButton = new Button("Update Subphenotypes");
        updateSubPhenotypeButton.setOnAction(e -> {
            if (currentPhenotype.getXAxisMarkerMeasurementName() != null &&
                    currentPhenotype.getYAxisMarkerMeasurementName()!=null) {
                currentPhenotype.updateSubPhenotypes();
                phenotypeHierarchy.getTreeTable().refresh();
            }
        });
        updateSubPhenotypeBox =  createHBox();
        updateSubPhenotypeBox.getChildren().addAll(
                createLabel("Update existing subphenotypes with new thresholds."),
                updateSubPhenotypeButton);
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
        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), GatingIO.FOLDER);
        manualGatingOptionsBox = new ComboBox<>();
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        manualGatingOptionsBox.setItems(FXCollections.observableArrayList(folderName.list()));
        confirmManualGatingOptionButton = new Button("Load Options");
        confirmManualGatingOptionButton.setOnAction(e -> {
            if (manualGatingOptionsBox.getSelectionModel().isEmpty()){
                Dialogs.showErrorMessage(TITLE, "Please select a file");
                return;
            }
            try {
                currentPhenotype = GatingIO.readLoadOptions(folderName, manualGatingOptionsBox.getValue(), markers, measurements, cells, stage, true);
                currentPhenotype.getXAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(currentPhenotype.getXAxis()));
                currentPhenotype.getYAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(currentPhenotype.getYAxis()));
                phenotypeHierarchy.setRoot(currentPhenotype.getTreeItem());


                splitPane.getItems().remove(optionsColumn);

                if (currentPhenotype.getPane() == null) {
                    optionsColumn = createColumn(
                            currentPhenotype.createPane(),
                            createSubPhenotypeBox,
                            updateSubPhenotypeBox,
                            highlightCellBox
                    );
                } else{
                    optionsColumn = createColumn(
                            currentPhenotype.getPane(),
                            createSubPhenotypeBox,
                            updateSubPhenotypeBox,
                            highlightCellBox
                    );
                }
                splitPane.getItems().add(
                        optionsColumn
                );
                PathClassHandler.restorePathClass();
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
        phenotypeHierarchy = new TreeTableWrapper<>();
        phenotypeHierarchy.getTreeTable().setRowFactory(tv -> {
            TreeTableRow<PhenotypeEntry> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {

                    currentPhenotype = row.getItem();
                    splitPane.getItems().remove(optionsColumn);
                    createCreateSubPhenotypeBox();
                    createUpdateSubPhenotypeBox();
                    if (currentPhenotype.getPane() == null) {
                        optionsColumn = createColumn(
                                currentPhenotype.createPane(),
                                createSubPhenotypeBox,
                                updateSubPhenotypeBox,
                                highlightCellBox
                        );
                    } else{
                        optionsColumn = createColumn(
                                currentPhenotype.getPane(),
                                createSubPhenotypeBox,
                                updateSubPhenotypeBox,
                                highlightCellBox
                        );
                    }

                    splitPane.getItems().add(
                            optionsColumn
                    );
                    PathClassHandler.restorePathClass();
                }
            });
            return row ;
        });

        phenotypeHierarchy.getTreeTable().prefHeightProperty().bind(stage.heightProperty());
        phenotypeHierarchy.addColumn("Phenotype", "name", 0.2);
        phenotypeHierarchy.addColumn("Positive Markers", "positiveMarkers", 0.4);
        phenotypeHierarchy.addColumn("Negative Markers", "negativeMarkers", 0.4);

        markers = ChannelInformationExtraction.extractMarkers(imageServer);
        measurements = ChannelInformationExtraction.extractMarkerMeasurements(cells, imageServer);

        currentPhenotype = new PhenotypeEntry(
                cells,
                "Cell",
                null,
                null,
                markers,
                measurements,
                stage,
                true
        );

        phenotypeHierarchy.setRoot(currentPhenotype.getTreeItem());
        currentPhenotype.getXAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(currentPhenotype.getXAxis()));
        currentPhenotype.getYAxisSlider().valueProperty().addListener((v, o, n) -> PathClassHandler.previewThreshold(currentPhenotype.getYAxis()));
    }

    public static VBox createColumn(Node... nodes){
        VBox column = createVBox();
        column.getChildren().addAll(nodes);
        column.setFillWidth(true);
        return column;
    }

    public void initialisePathClassHandler(){
        PathClassHandler.resetCellPathClass(cells);
        PathClassHandler.getInstance();
        PathClassHandler.setInstanceImageData(imageData);
        PathClassHandler.storeClassification();
    }


}

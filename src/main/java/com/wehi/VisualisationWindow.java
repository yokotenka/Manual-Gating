package com.wehi;


import com.wehi.io.FunctionalIO;
import com.wehi.io.GatingIO;
import com.wehi.io.VisualisationIO;
import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.PhenotypeEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONException;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewerPlus;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.projects.Projects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.wehi.JavaFXHelpers.*;

public class VisualisationWindow implements Runnable{

    public static final String TITLE = "Visualisation";

    private QuPathGUI qupath;

    private ComboBox<String> manualGatingOptionsBox;

    private PhenotypeEntry currentPhenotype;

    private Collection<PathObject> cells;
    private ObservableList<String> markers;
    private ObservableList<String> measurements;

    private Button addButton;
    private Button removeButton;

    private VBox mainBox;
    private Stage stage;

    private HBox loadOptionsHBox;

    private VisualisationThreeTablesSplitPaneWrapper visualisationThreeTables;

    private QuPathViewerPlus viewer;
    private ImageData<BufferedImage> imageData;
    private ImageServer<BufferedImage> imageServer;

    /**
     * Constructor for the ManualGatingWindow
     * @param quPathGUI
     */
    public VisualisationWindow(QuPathGUI quPathGUI){
        this.qupath = quPathGUI;
    }


    @Override
    public void run() {

        createDialog();

        initialisePathClassHandler();
        stage.show();
    }

    public void createDialog(){
        stage = new Stage();

        updateQupath();
        PathClassHandler.resetCellPathClass(cells);
        extractMeasurements();
        initialiseMainBox();



        visualisationThreeTables = new VisualisationThreeTablesSplitPaneWrapper();

        createLoadOptions();

        mainBox.getChildren().addAll(
                loadOptionsHBox,
                createLoadVisualisationOptions(),
                visualisationThreeTables.getSplitPane(),
                createSaveRow()
        );


        stage.initOwner(QuPathGUI.getInstance().getStage());

        Scene scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.setWidth(700);
        stage.setHeight(850);


    }

    private void initialiseMainBox(){
        mainBox = new VBox();
        mainBox.setFillWidth(true);
        mainBox.setSpacing(5);
        mainBox.setPadding(new Insets(10, 10, 10, 10));
    }

    private void updateQupath(){
        this.viewer = qupath.getViewer();
        this.imageData = this.viewer.getImageData();
        this.imageServer = this.viewer.getServer();
        this.cells = this.imageData.getHierarchy().getCellObjects();

        PathClassHandler.getInstance();
        PathClassHandler.setInstanceImageData(imageData);
    }

    private void createLoadOptions(){

        loadOptionsHBox = createHBox();
        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), GatingIO.FOLDER);
        manualGatingOptionsBox = new ComboBox<>();
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        manualGatingOptionsBox.setItems(FXCollections.observableArrayList(folderName.list()));
        Button confirmOptionsButton = new Button("Load Options");
        confirmOptionsButton.setOnAction(e -> {
            if (manualGatingOptionsBox.getSelectionModel().isEmpty()){
                Dialogs.showErrorMessage(TITLE, "Please select a file");
                return;
            }
            try {

                PathClassHandler.storeClassification();
                currentPhenotype = GatingIO.readLoadOptions(
                        folderName,
                        manualGatingOptionsBox.getValue(),
                        markers,
                        measurements, cells, stage, false);
                visualisationThreeTables.setRoot(currentPhenotype);
                FunctionalIO.loadOptions(
                        folderName,
                        manualGatingOptionsBox.getValue(),
                        markers,
                        measurements,
                        cells,
                        stage
                );
                visualisationThreeTables.setAvailableActivities(FunctionalIO.getPossibleActivities(folderName,
                        manualGatingOptionsBox.getValue()));

                currentPhenotype.hide();

            } catch (IOException | JSONException ioException) {
                ioException.printStackTrace();
            }
        });

        loadOptionsHBox.getChildren().addAll(
                createLabel("Load saved options"),
                manualGatingOptionsBox,
                confirmOptionsButton
        );
    }


    public HBox createLoadVisualisationOptions(){
        HBox loadVisualisationOptionsHBox = createHBox();
        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), VisualisationIO.FOLDER);
        ComboBox<String> visualisationOptionsBox = new ComboBox<>();
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        visualisationOptionsBox.setItems(FXCollections.observableArrayList(folderName.list()));
        Button confirmOptionsButton = new Button("Load Options");
        confirmOptionsButton.setOnAction(e -> {
            if (visualisationOptionsBox.getSelectionModel().isEmpty()){
                Dialogs.showErrorMessage(TITLE, "Please select a file");
                return;
            }

            if (visualisationThreeTables.getRoot()==null){
                Dialogs.showErrorMessage(TITLE, "Please load a phenotype options file");
                return;
            }

            try {
                visualisationThreeTables.clearList();
                VisualisationIO.load(Projects.getBaseDirectory(qupath.getProject()), visualisationOptionsBox.getValue(), visualisationThreeTables.getRoot());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            QuPathGUI.getInstance().getViewer().forceOverlayUpdate();
            visualisationThreeTables.refreshList();
        });
        loadVisualisationOptionsHBox.getChildren().addAll(
                createLabel("Load saved colour options"),
                visualisationOptionsBox,
                confirmOptionsButton
        );


        return loadVisualisationOptionsHBox;
    }



    public HBox createSaveRow(){
        HBox saveOptions = createHBox();
        TextField phenotypeHierarchyNameField = new TextField();
        Button saveButton = new Button("Save");
        saveOptions.getChildren().addAll(
                createLabel("Visualisation Options Name"),
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
                VisualisationIO.save(visualisationThreeTables.getRoot(), baseDir, fileName);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        });
        return saveOptions;
    }





    public void extractMeasurements(){
        markers = ChannelInformationExtraction.extractMarkers(imageServer);
        measurements = ChannelInformationExtraction.extractMarkerMeasurements(cells, imageServer);
    }

    public void initialisePathClassHandler(){
        PathClassHandler.resetCellPathClass(cells);
        PathClassHandler.getInstance();
        PathClassHandler.setInstanceImageData(imageData);
        PathClassHandler.storeClassification();
    }
}

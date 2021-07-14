package com.wehi;


import com.wehi.io.FunctionalIO;
import com.wehi.io.GatingIO;
import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.PhenotypeEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
        extractMeasurements();
        initialiseMainBox();



        visualisationThreeTables = new VisualisationThreeTablesSplitPaneWrapper();

        createLoadOptions();

        mainBox.getChildren().addAll(
                loadOptionsHBox,
                visualisationThreeTables.getSplitPane()
        );


        stage.initOwner(QuPathGUI.getInstance().getStage());

        Scene scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(500);


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
                currentPhenotype = GatingIO.readLoadOptions(
                        folderName,
                        manualGatingOptionsBox.getValue(),
                        markers,
                        measurements, cells, stage, false);
                visualisationThreeTables.setRoot(currentPhenotype);

                visualisationThreeTables.setAvailableActivities(FunctionalIO.getPossibleActivities(folderName,
                        manualGatingOptionsBox.getValue()));

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

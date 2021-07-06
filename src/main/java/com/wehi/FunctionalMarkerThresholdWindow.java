package com.wehi;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.FunctionalMarkerEntry;
import com.wehi.table.entry.PhenotypeEntry;
import com.wehi.table.wrapper.FunctionalPhenotypeListTableWrapper;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.wehi.JavaFXHelpers.*;

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
    public static final String TITLE = "Manual Gating";

    // The main scene to be displayed in the stage
    private VBox mainBox;
    // The middle bit
    private SplitPane splitPane;
    // The stage
    private Stage stage;

    private Scene scene;

    private PhenotypeEntry phenotypeEntry;

    private HBox loadBox;

    private FunctionalMarkerEntry currentFunctionalMarkerEntry;

    private ObservableList<String> markers;
    private ObservableList<String> measurements;

    private FunctionalPhenotypeListTableWrapper functionalPhenotypeListTableWrapper;

    public FunctionalMarkerThresholdWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }


    @Override
    public void run() {
        createDialog();
        PathClassHandler.resetCellPathClass(cells);
        PathClassHandler.storeClassification();
        stage.show();
    }

    public void createDialog(){
        stage = new Stage();
        initialiseMainBox();
        updateQupath();
        initialiseLoadBox();

        Separator sep1 = new Separator();
        sep1.setHalignment(HPos.CENTER);

        initialiseFunctionalPhenotypeTable();



        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        VBox rightHandSide = createVBox();
        rightHandSide.getChildren().addAll(currentFunctionalMarkerEntry.createPane(), createSubPhenotypeBox());
        splitPane.getItems().addAll(functionalPhenotypeListTableWrapper.getTreeTable(), rightHandSide);

        mainBox.getChildren().addAll(loadBox, sep1, splitPane);


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
    private void updateQupath(){
        this.viewer = qupath.getViewer();
        this.imageData = this.viewer.getImageData();
        this.imageServer = this.viewer.getServer();
        this.cells = this.imageData.getHierarchy().getCellObjects();

        PathClassHandler.getInstance();
        PathClassHandler.setInstanceImageData(imageData);
    }

    public void initialiseFunctionalPhenotypeTable(){
        functionalPhenotypeListTableWrapper = new FunctionalPhenotypeListTableWrapper();
        functionalPhenotypeListTableWrapper.getTreeTable().prefHeightProperty().bind(stage.heightProperty());
        currentFunctionalMarkerEntry = new FunctionalMarkerEntry(
                cells,
                markers,
                measurements,
                stage
        );
        functionalPhenotypeListTableWrapper.add(currentFunctionalMarkerEntry);
        functionalPhenotypeListTableWrapper.getTreeTable().refresh();
    }

    public void initialiseLoadBox(){
        Label loadLabel = createLabel("Load saved options");
        Button loadButton = new Button("Load Options");
        ComboBox<String> phenotypeOptions = new ComboBox<>();

        extractMarkerMeasurements();
        extractMarkers();

        File folderName = new File(Projects.getBaseDirectory(qupath.getProject()), JSONTreeSaver.FOLDER);
        if (!folderName.exists()){
            folderName.mkdirs();
        }
        phenotypeOptions.setItems(FXCollections.observableArrayList(folderName.list()));
        loadButton.setOnAction(e -> {
                    if (phenotypeOptions.getSelectionModel().isEmpty()) {
                        Dialogs.showErrorMessage(TITLE, "Please select a file");
                        return;
                    }

                    try {
                        phenotypeEntry = JSONTreeSaver.readLoadOptions(folderName, phenotypeOptions.getValue(), markers, measurements, cells, stage);
                    } catch (IOException | JSONException ioException) {
                        ioException.printStackTrace();
                    }
                }
            );

        loadBox = createHBox();
        loadBox.getChildren().addAll(loadLabel, phenotypeOptions, loadButton);
    }

    public HBox createSubPhenotypeBox(){
        HBox subPhenotypeBox = createHBox();

        Button applyThreshold = new Button("Apply Threshold");
        applyThreshold.setOnAction(e ->{
            currentFunctionalMarkerEntry.createPhenotypes();
            currentFunctionalMarkerEntry.setName(currentFunctionalMarkerEntry.getMarker());
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
    
    public void addRowsBox(){

    }


}

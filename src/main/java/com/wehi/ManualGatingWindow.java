package com.wehi;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.projects.Projects;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class ManualGatingWindow implements Runnable, ChangeListener<ImageData<BufferedImage>> {
    private QuPathGUI qupath;
    private QuPathViewer viewer;
    private ImageData<BufferedImage> imageData;
    private ImageServer<BufferedImage> imageServer;
    private Collection<PathObject> cells;

    private HashMap<String, TreeItem> phenotypeHierarchyNodeMap = new HashMap<>();

    private final String title = "Manual Gating";


    private VBox mainBox;
    private Stage stage;
    private VBox column1;
    private TreeView<String> phenotypeHierarchy;

    private ComboBox<String> dimensionsBox;
    private ComboBox<String> manualGatingOptionsBox;
    private Button confirmManualGatingOptionButton;
    private CytometryChart cytometryChart;

    private TableCreator<AxisTableEntry> axisOptionsTableCreator;
    private TableCreator<MarkerSignalCombinationTableEntry> markerSignalCombinationTableEntryTableCreator;


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
        HBox loadOptionsBox = new HBox();
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


        HBox contentBox = createHBox();

        /* Initialise Phenotype Hierarchy */
        Label phenotypeHierarchyLabel = createLabel("Phenotype Hierarchy");
        phenotypeHierarchy = new TreeView<>();
        phenotypeHierarchy.prefHeightProperty().bind(stage.heightProperty());
        contentBox.getChildren().add(createColumn(3, 1,phenotypeHierarchyLabel, phenotypeHierarchy));

        TreeItem<String> root = new TreeItem<>("Cell");
        phenotypeHierarchyNodeMap.put("Cell", root);


        /* ******** Vertical Separator ********** */
        Separator vertSeparator = new Separator();
        vertSeparator.setOrientation(Orientation.VERTICAL);
        contentBox.getChildren().add(vertSeparator);


        /* ***** Options column **************/

        /* Selecting dimensions */
        HBox dimensionsHBox = createHBox();
        dimensionsBox = new ComboBox<>();
        dimensionsBox.getItems().add("1-D");
        dimensionsBox.getItems().add("2-D");
        dimensionsHBox.getChildren().addAll(createLabel("Choose the dimension of the plot"), dimensionsBox);

        /* Axis tableview */
        axisOptionsTableCreator = new TableCreator<>();

        /* Marker Signal Combination */
        markerSignalCombinationTableEntryTableCreator = new TableCreator<>();


        /* Graph */
        cytometryChart = new CytometryChart(stage, "", "");


        /* Create column on the right */
        contentBox.getChildren().add(createColumn(3, 2,
                dimensionsHBox,
                axisOptionsTableCreator.getTable(),
                markerSignalCombinationTableEntryTableCreator.getTable(),
                cytometryChart.getPane()
        ));




        /* Adding the main body to the scene */
        mainBox.getChildren().add(contentBox);

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
//        this.cells = this.imageData.getHierarchy().getCellObjects();
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

    private VBox createColumn(int widthProportionDivisor, int widthProportionMultiplier, Node... nodes){
        column1 = createVBox();
        column1.getChildren().addAll(nodes);
        column1.setFillWidth(true);


        column1.prefWidthProperty().bind(
                Bindings.multiply(
                        Bindings.divide(stage.widthProperty(), widthProportionDivisor),
                        widthProportionMultiplier
                )
        );

        return column1;
    }


    public static Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private HBox createHBox(){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        return hBox;
    }

    private VBox createVBox(){
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        return vBox;
    }



    // Tree view helpers *****

}

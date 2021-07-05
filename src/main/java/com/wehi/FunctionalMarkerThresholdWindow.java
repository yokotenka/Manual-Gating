package com.wehi;

import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;

import java.awt.image.BufferedImage;
import java.util.Collection;

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


    public FunctionalMarkerThresholdWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }


    @Override
    public void run() {

    }
}

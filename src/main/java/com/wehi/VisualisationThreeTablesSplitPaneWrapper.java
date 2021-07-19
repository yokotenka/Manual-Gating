package com.wehi;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.ActivityCellTypeEntry;
import com.wehi.table.entry.IVisualisable;
import com.wehi.table.entry.PhenotypeEntry;
import com.wehi.table.entry.StringSelectEntry;
import com.wehi.table.wrapper.ActivityCellTypeTableWrapper;
import com.wehi.table.wrapper.ListActivityTableWrapper;
import com.wehi.table.wrapper.StringSelectTableWrapper;
import com.wehi.table.wrapper.VisualisationTreeTableWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeTableRow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.wehi.JavaFXHelpers.*;
import static java.lang.Double.MAX_VALUE;

public class VisualisationThreeTablesSplitPaneWrapper {

    private static final String TITLE = "Activity combination";
    private VBox functionalBox;
    private SplitPane leftHandSide;
    private SplitPane splitPane;


    private VisualisationTreeTableWrapper visualisationTreeTableWrapper;
    private ListActivityTableWrapper listActivityTableWrapper;
    private ActivityCellTypeTableWrapper activityCellTypeTableWrapper;

    private StringSelectTableWrapper stringSelectTableWrapper;

    private PhenotypeEntry currentPhenotype;

    private Stage secondaryStage;

    public VisualisationThreeTablesSplitPaneWrapper(){
        // Initialise the tables
        initialiseVisualisationTreeTableWrapper();
        listActivityTableWrapper = new ListActivityTableWrapper();
        activityCellTypeTableWrapper = new ActivityCellTypeTableWrapper();
        stringSelectTableWrapper = new StringSelectTableWrapper();

        splitPane = new SplitPane();
        createLeftHandSide();
        splitPane.getItems().addAll(leftHandSide, listActivityTableWrapper.getTable());

    }

    /**
     * Creates the left side of the split pane
     */
    public void createLeftHandSide(){
        leftHandSide = new SplitPane();
        leftHandSide.setOrientation(Orientation.VERTICAL);

        functionalBox = createVBox();
        functionalBox.getChildren().addAll(activityCellTypeTableWrapper.getTable(),
                createAddActivityCombinationBox());

        leftHandSide.getItems().addAll(
                visualisationTreeTableWrapper.getTreeTable(),
                functionalBox
        );
    }

    /**
     * Initialise visualisation table
     */
    public void initialiseVisualisationTreeTableWrapper(){
        visualisationTreeTableWrapper = new VisualisationTreeTableWrapper();
        visualisationTreeTableWrapper.getTreeTable().setRowFactory(tv -> {
            TreeTableRow<PhenotypeEntry> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    functionalBox.getChildren().remove(currentPhenotype.getActivityCellTypeTableWrapper().getTable());

                    currentPhenotype = row.getItem();

                    functionalBox.getChildren().add(0, currentPhenotype.getActivityCellTypeTableWrapper().getTable());
                }
            });
            return row ;
        });
    }


    /**
     * Sets the root for the visualisationTreeTableWrapper
     * @param root
     */
    public void setRoot(PhenotypeEntry root){
        visualisationTreeTableWrapper.setRoot(root);
        currentPhenotype = root;

        functionalBox.getChildren().remove(0);
        functionalBox.getChildren().add(0, root.getActivityCellTypeTableWrapper().getTable());

        listSelectedPhenotype();
    }

    /**
     * Getter for the splitPane
     * @return
     */
    public SplitPane getSplitPane(){
        return splitPane;
    }

    /**
     * Adds the activity to the current phenotype
     * @return
     */
    public HBox createAddActivityCombinationBox(){
        HBox addActivityBox = createHBox();
        Button add = new Button("Add");
        add.setOnAction(e -> addFunctionalCombinationPopup());

        Button remove = new Button("Remove");
        remove.setOnAction(e -> currentPhenotype.removeActivity());

        Button removeFromAll = new Button("Remove from all phenotypes");
        removeFromAll.setOnAction(e -> {
            removeActivityFromAllPhenotypes();
        });

        addActivityBox.getChildren().addAll(add, remove, removeFromAll);
        return addActivityBox;
    }

    /**
     * Lists the currently showing phenotypes
     */
    public void listSelectedPhenotype(){
        PhenotypeEntry root = visualisationTreeTableWrapper.getRoot().getValue();
        createMoveToListForChildren(root);
    }

    /**
     * Creates the action which moves the phenotype to the list
     * @param entry
     */
    private void createMoveToListForChildren(PhenotypeEntry entry){

        createMoveToList(entry);

        for (PhenotypeEntry child : entry.getChildren()){

            createMoveToListForChildren(child);
        }
    }

    public void createMoveToList(IVisualisable entry){
        entry.getShow().setOnAction(e -> {
            if (entry.getShow().isSelected()) {
                listActivityTableWrapper.addRow(entry);
                entry.show();
                entry.setColorDownTree(entry.getColor());
            } else{
                listActivityTableWrapper.removeRow(entry);
                entry.hideButShowUpTree();
            }
            listActivityTableWrapper.getTable().refresh();
            currentPhenotype.getActivityCellTypeTableWrapper().getTable().refresh();
            visualisationTreeTableWrapper.getTreeTable().refresh();
//            QuPathGUI.getInstance().getViewer().repaintEntireImage();
            QuPathGUI.getInstance().getViewer().forceOverlayUpdate();
        });

        entry.getColorPicker().setOnAction(e -> {
            entry.setColorDownTree(entry.getColor());
            QuPathGUI.getInstance().getViewer().forceOverlayUpdate();

        });
    }

    /**
     * Pop up which will be shown when "add" is pressed.
     */
    public void addFunctionalCombinationPopup(){
        if (secondaryStage==null) {
            secondaryStage = new Stage();
            secondaryStage.setAlwaysOnTop(true);
            VBox mainBox = createVBox();
            mainBox.setFillWidth(true);
            mainBox.setSpacing(5);
            mainBox.setPadding(new Insets(10, 10, 10, 10));
            Label label = createLabel("Create a combination");


            HBox selectionBox = createHBox();
            selectionBox.setMaxWidth(MAX_VALUE);

            Button selectAll = new Button("Select all");
            selectAll.setMaxWidth(MAX_VALUE);

            selectAll.setOnAction(e ->
                    stringSelectTableWrapper.selectAll()
            );

            Button deSelectAll = new Button("Deselect all");
            deSelectAll.setMaxWidth(MAX_VALUE);

            deSelectAll.setOnAction(e -> stringSelectTableWrapper.deSelectAll());

            selectionBox.getChildren().addAll(selectAll, deSelectAll);

            Button addToAllButton = new Button("Add to all phenotypes");
            addToAllButton.setMaxWidth(MAX_VALUE);
            addToAllButton.setOnAction(e -> {
                if (stringSelectTableWrapper.collectSelected().isEmpty()){
                    Dialogs.showErrorMessage(TITLE, "Nothing was selected!");
                    return;
                }
                createNewActivityForAll();
//                secondaryStage.close();
            });

            Button addButton = new Button("Only add to current phenotype");
            addButton.setMaxWidth(MAX_VALUE);
            addButton.setOnAction(e -> {
                if (stringSelectTableWrapper.collectSelected().isEmpty()){
                    Dialogs.showErrorMessage(TITLE, "Nothing was selected!");
                    return;
                }
                createNewActivity(currentPhenotype);
//                secondaryStage.close();
            });

            Button cancelButton = new Button("Close");
            cancelButton.setMaxWidth(MAX_VALUE);

            mainBox.getChildren().addAll(
                    label,
                    stringSelectTableWrapper.getTable(),
                    selectionBox,
                    addToAllButton,
                    addButton,
                    cancelButton
            );

            cancelButton.setOnAction(e -> {
                secondaryStage.close();
            });

            Scene scene = new Scene(mainBox);
            secondaryStage.setScene(scene);
        }
        if (!secondaryStage.isShowing()) {
            secondaryStage.showAndWait();
        }
    }


    public void createNewActivity(PhenotypeEntry entry){
        if (!entry.containsActivity(stringSelectTableWrapper.collectSelected())) {
            ActivityCellTypeEntry activityCellTypeEntry = new ActivityCellTypeEntry(
                    entry,
                    stringSelectTableWrapper.collectSelected());
            entry.addActivity(activityCellTypeEntry);

            createMoveToList(activityCellTypeEntry);
        }


    }

    public void createNewActivityForAll(){
        PhenotypeEntry root = visualisationTreeTableWrapper.getRoot().getValue();

        createNewActivityForChildren(root);
    }

    public void createNewActivityForChildren(PhenotypeEntry entry){
        createNewActivity(entry);

        for (PhenotypeEntry child : entry.getChildren()){
            createNewActivityForChildren(child);
        }
    }



    public void setAvailableActivities(ObservableList<StringSelectEntry> activities){
        stringSelectTableWrapper.setItems(activities);
    }

    public void removeActivityFromAllPhenotypes(){
        ActivityCellTypeEntry selected = currentPhenotype.getActivityCellTypeTableWrapper().getSelectedItem();
        PhenotypeEntry root = visualisationTreeTableWrapper.getRoot().getValue();

        removeActivity(root, selected.getActivities());
    }

    public void removeActivity(PhenotypeEntry entry, ArrayList<String> activities){
        entry.removeActivity(activities);
        for(PhenotypeEntry child : entry.getChildren()){
            removeActivity(child, activities);
        }
    }


}

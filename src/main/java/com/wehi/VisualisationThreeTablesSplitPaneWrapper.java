package com.wehi;

import com.wehi.pathclasshandler.PathClassHandler;
import com.wehi.table.entry.ActivityCellTypeEntry;
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

import java.util.ArrayList;

import static com.wehi.JavaFXHelpers.*;
import static java.lang.Double.MAX_VALUE;

public class VisualisationThreeTablesSplitPaneWrapper {

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

        functionalBox.getChildren().remove(activityCellTypeTableWrapper.getTable());
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

        addActivityBox.getChildren().addAll(add, remove);
        return addActivityBox;
    }

    /**
     * Lists the currently showing phenotypes
     */
    public void listSelectedPhenotype(){
        PhenotypeEntry root = visualisationTreeTableWrapper.getRoot().getValue();
        createMoveToList(root);
    }

    /**
     * Creates the action which moves the phenotype to the list
     * @param entry
     */
    private void createMoveToList(PhenotypeEntry entry){
        for (PhenotypeEntry child : entry.getChildren()){
            child.getShowButton().setOnAction(e ->
            {
                listActivityTableWrapper.addRow(child);
                listActivityTableWrapper.getTable().refresh();
                visualisationTreeTableWrapper.getTreeTable().refresh();
            });

            child.getHideButton().setOnAction(e ->
            {
                listActivityTableWrapper.removeRow(child);
                listActivityTableWrapper.getTable().refresh();
                visualisationTreeTableWrapper.getTreeTable().refresh();
            });
            createMoveToList(child);
        }
    }

    /**
     * Pop up which will be shown when "add" is pressed.
     */
    public void addFunctionalCombinationPopup(){
        if (secondaryStage==null) {
            secondaryStage = new Stage();

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
            Button addButton = new Button("Only add to current phenotype");
            addButton.setMaxWidth(MAX_VALUE);
            addButton.setOnAction(e -> {

                ActivityCellTypeEntry activityCellTypeEntry = new ActivityCellTypeEntry(
                        currentPhenotype.getCells(),
                        currentPhenotype.getName(),
                        stringSelectTableWrapper.collectSelected());
                currentPhenotype.addActivity(activityCellTypeEntry);

                activityCellTypeEntry.getShowButton().setOnAction(i ->
                {
                    listActivityTableWrapper.addRow(activityCellTypeEntry);
                    currentPhenotype.getActivityCellTypeTableWrapper().getTable().refresh();
                    visualisationTreeTableWrapper.getTreeTable().refresh();
                });

                activityCellTypeEntry.getHideButton().setOnAction(i ->
                {
                    listActivityTableWrapper.removeRow(activityCellTypeEntry);
                    currentPhenotype.getActivityCellTypeTableWrapper().getTable().refresh();
                    visualisationTreeTableWrapper.getTreeTable().refresh();
                });
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




    public void setAvailableActivities(ObservableList<StringSelectEntry> activities){
        stringSelectTableWrapper.setItems(activities);
    }


}

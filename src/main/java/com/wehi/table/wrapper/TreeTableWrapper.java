package com.wehi.table.wrapper;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;


/**
 * Wrapper class for the TreeTableView
 * @param <T>
 */
public class TreeTableWrapper<T> {
    // Tree table it is wrapping
    private TreeTableView<T> treeTable;

    /**
     * Constructor
     */
    public TreeTableWrapper(){
        treeTable = new TreeTableView<>();
    }

    /**
     * Getter for the tree table
     * @return treeTable
     */
    public TreeTableView<T> getTreeTable(){
       return treeTable;
    }

    /**
     * Getter for the root item
     * @return rootItem
     */
    public TreeItem<T> getRoot(){
        return treeTable.getRoot();
    }

    /**
     * Setter for the root item
     * @param root the item to be set as root
     */
    public void setRoot(TreeItem<T> root){
        treeTable.setRoot(root);
    }

    /**
     * Add column to the tree table view
     * @param title title of the column
     * @param variableName name of the variable within the class T to be displayed
     * @param proportion the proportion of space the column should take up
     */
    public void addColumn(String title, String variableName, double proportion){
        TreeTableColumn<T, Object> col = createColumn(title, variableName);
        col.prefWidthProperty().bind(treeTable.widthProperty().multiply(proportion));
        treeTable.getColumns().add(col);
    }


    // Helper method to create the column
    private TreeTableColumn<T, Object> createColumn(String title, String variableName){
        TreeTableColumn<T, Object> treeTableColumn = new TreeTableColumn<>(title);
        treeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>(variableName));
        return treeTableColumn;
    }
}

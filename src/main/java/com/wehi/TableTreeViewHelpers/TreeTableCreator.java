package com.wehi.TableTreeViewHelpers;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public class TreeTableCreator<T> {
    private TreeTableView<T> treeTable;

    public TreeTableCreator(){
        treeTable = new TreeTableView<>();
    }

    public TreeTableView<T> getTreeTable(){
       return treeTable;
    }

    public TreeItem<T> getRoot(){
        return treeTable.getRoot();
    }

    public void setRoot(TreeItem<T> root){
        treeTable.setRoot(root);
    }

    public void addColumn(String title, String variableName, double proportion){
        TreeTableColumn<T, Object> col = createColumn(title, variableName);
        col.prefWidthProperty().bind(treeTable.widthProperty().multiply(proportion));
        treeTable.getColumns().add(col);
    }

    public TreeTableColumn<T, Object> createColumn(String title, String variableName){
        TreeTableColumn<T, Object> treeTableColumn = new TreeTableColumn<>(title);
        treeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>(variableName));
        return treeTableColumn;
    }
}

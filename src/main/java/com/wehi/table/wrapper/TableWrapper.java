package com.wehi.table.wrapper;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Class which helps create a javafx TableView class
 */
public class TableWrapper<T> {
    // The TableView instance
    private TableView<T> table;

    /**
     * Constructor
     */
    public TableWrapper(){
        this.table = new TableView<>();
    }
    /**
     * Constructor
     */
    public TableWrapper(TableView<T> table){
        this.table = new TableView<>();
    }

    /**
     * Set items to the TableView
     * @param rows
     */
    public void setItems(ObservableList<T> rows){
        table.setItems(rows);
    }

    /**
     * Adding columns to the table
     * @param title Title of the column
     * @param variableName variable name in MarkerTableEntry
     */
    public void addColumn(String title, String variableName, double proportion){
        TableColumn<T, Object> col = createColumn(title, variableName);
        col.prefWidthProperty().bind(table.widthProperty().multiply(proportion));

        table.getColumns().add(col);
    }

    /**
     * Getter for table
     * @return table
     */
    public TableView<T> getTable(){
        return table;
    }

    /**
     * Static creator for a table column
     * @param title Title of the column
     * @param variableName Name of the variable in MarkerTableEntry
     * @return Instance of TableColumn
     */
    public TableColumn<T, Object> createColumn(String title, String variableName){
        TableColumn<T, Object> nameCol = new TableColumn<>(title);
        nameCol.setCellValueFactory(new PropertyValueFactory<>(variableName));

        return nameCol;
    }


    /**
     * Adds a row to the table
     * @param row Row to be added
     */
    public void addRow(T row){
        table.getItems().add(row);
        table.refresh();
    }

    /**
     * Removes a row from table
     */
    public T removeRow(){
        if (table.getSelectionModel().getSelectedItem() != null) {
            T toBeRemoved = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(
                    toBeRemoved
            );
            return toBeRemoved;
        }
        return null;
    }

    public void removeRow(T entry){
        table.getItems().remove(entry);
    }

    /**
     * Gets the item
     * @return items
     */
    public ObservableList<T> getItems(){
        return table.getItems();
    }


    public boolean isEmpty(){
        return table.getItems().isEmpty();
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vilimpoc;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 *
 * @author Max
 */
public class MainDocumentController implements Initializable {
    
    // Absolute file paths are unique.
    protected final ObservableList<String> filenames = FXCollections.observableArrayList();
    
    @FXML
    protected ListView<String> documentListView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        documentListView.setItems(filenames);
    }
    
    @FXML
    protected void handleNewAction(ActionEvent e) {
    }
    
    @FXML
    protected void handleDragOver(DragEvent e) {
        if (e.getGestureSource() != documentListView) {
            e.acceptTransferModes(TransferMode.ANY);
        }
        
        e.consume();
    }
    
    @FXML
    protected void handleDragDropped(DragEvent e) {
        Dragboard b = e.getDragboard();
        boolean success = false;

        if (b.hasFiles()) {
            Logger.getGlobal().warning("Files dropped!");
            
            success = true;
            for (File f : b.getFiles()) {
                Logger.getGlobal().warning(f.getAbsolutePath());
                
                if (filenames.contains(f.getAbsolutePath())) {
                    Logger.getGlobal().warning("Don't add duplicate path.");
                }
                else {
                    filenames.add(f.getAbsolutePath());
                }
            }
        }
        
        e.setDropCompleted(success);
        e.consume();
    }
    
    @FXML
    protected void handleMouseClicked(MouseEvent e) {
        Logger.getGlobal().warning(documentListView.getSelectionModel().getSelectedItem());
    }
            
}

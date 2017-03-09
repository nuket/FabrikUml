/**
    Copyright (c) 2017 Max Vilimpoc

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package org.vilimpoc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

public class MainController implements Initializable {

    // Absolute file paths are unique.
    private final ObservableList<String> filenames = FXCollections.observableArrayList();
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Label   elapsedTimeMs;

    private int     untitledId = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {    
        // Set up change listener.
        tabPane.getTabs().addListener(new ListChangeListener<Tab>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Tab> c) {
                // Every time the Tag list changes, save all of the Tab model data.
                Common.saveTabConfig(tabPane.getTabs());
            }
        });
        
        // TODO: Recreate Window with open all previous files in Tabs
        LinkedList<TabModel> models = Common.loadTabConfig();

        for (TabModel model : models) {
            createNewTab(model);
        }

        // If there were no tabs restored, then create one untitled tab.
        if (tabPane.getTabs().isEmpty()) {
            createUntitledTab();
        }
    }
    
    @FXML
    protected void handleGlobalShortcuts(KeyEvent e) {
        if (Common.NEW_.match(e)) {
            createUntitledTab();
            e.consume();
        }
        else
        if (Common.SAVE.match(e)) {
            // Tell the currently-selected TabController to process CTRL-S.
            tabPane.getSelectionModel().getSelectedItem().getContent().fireEvent(e);
            e.consume();
            
            // Save tab config after creating, saving, or closing any of the tabs.
             Common.saveTabConfig(tabPane.getTabs());
        }
        else
        if (Common.CLOSE.match(e)) {
            // Tell the currently-selected Tab to close.
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            EventHandler<Event> handler = tab.getOnCloseRequest();
            if (handler != null) {
                handler.handle(null);
            }
            tabPane.getTabs().remove(tab);
        }
    }
    
    protected void createNewTab(TabModel tabModel) {
        if (!tabModel.backingFile.exists()) {
            System.err.println("File does not exist, will not open new editing tab.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "Tab.fxml"
                )
            );

            Tab tab = (Tab) loader.load();
            TabController tabController = loader.<TabController>getController();
            
            // System.out.println(tabModel);
            
            tab.setText(tabModel.tabText);
            tab.setUserData(tabModel);
            tabController.setTabModel(tabModel);
            
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void createUntitledTab() {
        try {
            TabModel tabModel = new TabModel(true, Common.getUntitledFile(), "Untitled " + untitledId++);
            createNewTab(tabModel);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Could not create temporary file.", ex);
        }
    }
    
    private void createNewTab(File file) {
        TabModel tabModel = new TabModel(false, file, file.getName());
        createNewTab(tabModel);
    }
    
    @FXML
    protected void handleNewAction(ActionEvent e) {
        System.out.println("Fun times.");
        createUntitledTab();
    }
    
    @FXML
    protected void handleDragOver(DragEvent e) {
        // System.out.println("Drag over: " + e.getGestureTarget());
        e.acceptTransferModes(TransferMode.ANY);        
        e.consume();
    }
    
    @FXML
    protected void handleDragDropped(DragEvent e) {
        // TODO: If the file does not exist or is read-only or access controlled
        //       and we can tell that right here, then DO NOT OPEN IT?
        
        Dragboard b = e.getDragboard();
        boolean success = false;

        if (b.hasFiles()) {
            Logger.getGlobal().warning("Files dropped!");
            
            // TODO: Fix up duplicate file open tracking.
            
            success = true;
            for (File f : b.getFiles()) {
                if (filenames.contains(f.getAbsolutePath())) {
                    Logger.getGlobal().warning("Don't add duplicate path.");
                }
                else {
                    filenames.add(f.getAbsolutePath());
                    
                    // 
                    createNewTab(f);
                }
            }
        }
        
        e.setDropCompleted(success);
        e.consume();
    }
    
}

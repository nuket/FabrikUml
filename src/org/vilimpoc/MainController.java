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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import net.sourceforge.plantuml.SourceStringReader;

/**
 *
 * @author Max
 */
class MainController implements Initializable {

    // Absolute file paths are unique.
    private final ObservableList<String> filenames = FXCollections.observableArrayList();

    @FXML
    private TabPane tabPane;
    
    @FXML
    private Label   elapsedTimeMs;

    
    private final KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    
    // Observe the filenames list and open new tabs accordingly.
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // tabPane.getTabs().
        
//        documentListView.setItems(filenames);
        // tabPane.getTabs()

//        // TODO: Properly set up keyboard shortcuts (CTRL-S saves the file and rerenders 
//        // the PlantUML preview)
//        codeArea.setOnKeyReleased((KeyEvent event) -> {
//            // if (event.getCode() == KeyCode.S && event.isControlDown()) {
//            if (save.match(event)) {
//                try {
//                    generatePng(codeArea.getText());
//                } catch (IOException ex) {
//                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
    }
    
//    @FXML
//    protected void handleGlobalShortcuts(KeyEvent e) {
//        System.out.println("Key pressed.");
//    }
    
    static int i = 0;
    
    protected void createNewTab(String filename) {
        File f = new File(filename);

        if (!f.exists()) {
            System.err.println("File does not exist.");
            return;
        }
        
        try {
            // Create a new Tab, configure it up.
            // ResourceBundle rb = ResourceBundle.getBundle("");
            
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "Tab.fxml"
                )
            );

//            Tab tab = FXMLLoader.load(getClass().getResource("Tab.fxml"));
//            tab.setText("Tab " + Integer.toString(i++));
            
//            Tab tab = FXMLLoader.FXMLLoader.load(getClass().getResource("Tab.fxml"), null);
            // tab.setText(name);
            
            TabController tabController = loader.<TabController>getController();
            tabController.openFile(f);
            
            Tab tab = (Tab) loader.load();
            tabPane.getTabs().add(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    protected void handleNewAction(ActionEvent e) {
        System.out.println("Fun times.");

        createNewTab("Tab " + Integer.toString(i++));
    }
    
    @FXML
    protected void handleDragOver(DragEvent e) {
        System.out.println("Drag over: " + e.getGestureTarget());
//        if (e.getGestureSource() != documentListView) {
            e.acceptTransferModes(TransferMode.ANY);
//        }
        
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
            
            createNewTab(filenames.get(filenames.size() - 1));
        }
        
        e.setDropCompleted(success);
        e.consume();
    }
    
    @FXML
    protected void handleMouseClicked(MouseEvent e) {
//        String filename = documentListView.getSelectionModel().getSelectedItem();
//        Logger.getGlobal().warning(filename);
//        
//        openFile(filename);
    }

//    protected void openFile(String filename) {
//        try {
//            // Open file in editor.
//            String data = new String(Files.readAllBytes(Paths.get(filename)));
//
//            // codeArea.replaceText(data);
//
//            // Go ahead and generate an Image to display.
//            generatePng(data);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    protected void generatePng(String uml) throws IOException
    {
        // Time the image generation.
        long startTime = System.nanoTime();

        ByteArrayOutputStream png = new ByteArrayOutputStream(1000000);
        SourceStringReader reader = new SourceStringReader(uml);

        // Write the first image to "png"
        String desc = reader.generateImage(png);
        
        Logger.getGlobal().warning(desc);
        
        InputStream pngLoad = new ByteArrayInputStream(png.toByteArray());
        
        Image diagram = new Image(pngLoad);
        // imagePane.getBackground().getImages().add(new BackgroundImage(diagram, null, null, null, null));
        
//        imageView.setImage(diagram);
        
        // Return a null string if no generation.
        
        long stopTime = System.nanoTime();
        long elapsed = (stopTime - startTime) / 10000000;

        Logger.getGlobal().log(Level.WARNING, "{0}ms", Long.toString(elapsed));

        elapsedTimeMs.setText(Long.toString(elapsed) + "ms");
    }

}

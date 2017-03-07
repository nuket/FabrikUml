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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import net.sourceforge.plantuml.SourceStringReader;

/**
 *
 * @author Max
 */
public class MainController implements Initializable {

    // Absolute file paths are unique.
    protected final ObservableList<String> filenames = FXCollections.observableArrayList();
    
//    @FXML
//    protected ListView<String> documentListView;
//    
//    @FXML
//    protected StackPane codeAreaPane;
//    
//    @FXML
//    protected StackPane imagePane;
//    
//    @FXML
//    protected ImageView imageView;
    
    @FXML
    protected TabPane tabPane;
    
    @FXML
    protected Label elapsedTimeMs;

    // This actually matches any time S is pressed. LOL.
    final KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
    
    static int i = 0;
    
    @FXML
    protected void handleNewAction(ActionEvent e) {
        System.out.println("Fun times.");
        
//        Tab tab = new Tab("Tab" + Integer.toString(i));
//        SplitPane splitPane = new SplitPane();
        
        try {
            // Create a new Tab, configure it up.
            Tab tab = FXMLLoader.load(getClass().getResource("Tab.fxml"), null);
            tab.setText("Tab " + Integer.toString(i++));
            
            tabPane.getTabs().add(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    protected void handleDragOver(DragEvent e) {
//        if (e.getGestureSource() != documentListView) {
//            e.acceptTransferModes(TransferMode.ANY);
//        }
        
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
            
            openFile(filenames.get(filenames.size() - 1));
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

    protected void openFile(String filename) {
        try {
            // Open file in editor.
            String data = new String(Files.readAllBytes(Paths.get(filename)));

            // codeArea.replaceText(data);

            // Go ahead and generate an Image to display.
            generatePng(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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

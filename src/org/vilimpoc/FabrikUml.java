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

import java.io.IOException;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FabrikUml extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("org.vilimpoc.resources.FabrikUml");
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"), bundle);
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("FabrikUml");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Without this, the program will not terminate clearly, as the executor
        // threads aren't cleaned up.
        TabController.shutdownAll();
        
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Set up the temporary folder.
            Files.createDirectories(Common.getWorkFolder());

            // Start the UI.
            launch(args);
            
            // Read the settings file:
            // 
            // Contains last-opened file list: These should be reopened.
            // 
            // Tell the MainController to open these files.
            

        } catch (IOException ex) {
            Logger.getLogger(FabrikUml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

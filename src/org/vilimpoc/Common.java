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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

class Common {
    // Unfortunately, we have to track this ourselves.
//    static Tab           currentTab;
//    static TabController currentTabController;
    private static final HashMap<String, TabModel> tabs = new HashMap<String, TabModel>();
    
    private static final Properties settings = new Properties();
    
    private static final String UNTITLED_FILE_PREFIX = "FabrikUml-";
    private static final String UNTITLED_FILE_SUFFIX = ".txt";

    private static final String WORK_FOLDER = ".FabrikUml";
    private static final String SETTINGS_FILE = "FabrikUml.settings";

    static final KeyCombination NEW_  = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    static final KeyCombination SAVE  = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    static final KeyCombination CLOSE = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    
    // Figure out what $HOME folder is.
    //
    // Ref: http://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
    // Requires Java 8.
    static Path getWorkFolder() {
        return Paths.get(System.getProperty("user.home"), Common.WORK_FOLDER);
    }
    
    static Path getSettingsFile() {
        return Paths.get(getWorkFolder().toString(), SETTINGS_FILE);
    }
    
    static File getUntitledFile() throws IOException {
        return File.createTempFile(UNTITLED_FILE_PREFIX, UNTITLED_FILE_SUFFIX, getWorkFolder().toFile());
    }
    
    static File getLastUsedFolder() {
        return new File(settings.getProperty("LAST_USED_FOLDER"));
    }
    
    static void setLastUsedFolder(String folder) {
        settings.setProperty("LAST_USED_FOLDER", folder);
        save();
    }
    
    private static void save() {
        try {
            settings.store(new FileOutputStream(getSettingsFile().toFile()), "FabrikUml Settings");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static {
        try {
            settings.load(new FileInputStream(getSettingsFile().toFile()));
        } 
        catch (IOException ex) {
            // Set default property values, if file not found, etc.
            settings.setProperty("LAST_USED_FOLDER", System.getProperty("user.home"));                    
        }
    }

    static void saveTabConfig(ObservableList<Tab> tabs) {
        int fileId = 0;
        
        // Clear all of the file properties.
        for (int f = 0; f < 99; f++) {
            if (settings.containsKey("file" + f)) {
                settings.remove("file" + f);
            }
        }
            
        for (Tab t : tabs) {
            System.out.println(t.getUserData().toString());
            
            settings.setProperty("file" + fileId, t.getUserData().toString());
            fileId++;
        };
        
        save();
    }
    
    static LinkedList<TabModel> loadTabConfig() {
        LinkedList<TabModel> models = new LinkedList<>();
        
        for (int fileId = 0; fileId < 99; fileId++) {
            String key = "file" + fileId;
            
            if (settings.keySet().contains(key)) {
                String value = settings.getProperty(key);
                String[] fields = value.split(",");
                
                System.out.println(fields[0]);
                System.out.println(fields[1]);
                System.out.println(fields[2]);
                
                TabModel model = new TabModel(fields[0], fields[1], fields[2]);
                models.add(model);
            }
        }
        
        return models;
    }
}

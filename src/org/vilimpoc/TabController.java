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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.sourceforge.plantuml.SourceStringReader;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class TabController implements Initializable {

    @FXML
    private Tab       tab;
    
    @FXML
    private StackPane codeAreaPane;
    
    @FXML
    private CodeArea  codeArea;
    
    @FXML
    private StackPane previewPane;
    
    @FXML
    private ImageView preview;

    private final KeyCombination refresh = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    
    private TabModel model;
    
    private ExecutorService executor;
    
    private static List<ExecutorService> executors = new LinkedList<>();
    
    protected static void shutdownAll() {
        for (ExecutorService e : executors) {
            System.out.println("Shutting down ExecutorService: " + e);
            if (!e.isShutdown()) e.shutdown();
        }
    }
    
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }
    
    // TODO: PATTERN matcher could be returned from PlantUmlSyntax class.
    
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PlantUmlSyntax.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("AT")         != null ? "at"        :
                    matcher.group("PREPROC")    != null ? "preproc"   :
                    matcher.group("TYPE")       != null ? "type"      :
                    matcher.group("KEYWORD")    != null ? "keyword"   :
                    
                    matcher.group("PAREN")      != null ? "paren"     :
                    matcher.group("BRACE")      != null ? "brace"     :
                    matcher.group("BRACKET")    != null ? "bracket"   :
                    matcher.group("SEMICOLON")  != null ? "semicolon" :
                    matcher.group("STRING")     != null ? "string"    :
                    matcher.group("COMMENT")    != null ? "comment"   :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Attach the CodeArea.
        executor = Executors.newSingleThreadExecutor();
        
        // Add the Executor to the overall executors list, for 
        // cleanly shutting down later.
        executors.add(executor);
        
        // codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.richChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);
        
        codeArea.replaceText(0, 0, PlantUmlSyntax.SAMPLE_CODE);
        
        // TODO: Rerender after no keystroke has been entered in 500ms.
        
        codeAreaPane.getStylesheets().add(FabrikUml.class.getResource("PlantUmlSyntax.css").toExternalForm());
//        
        preview.fitWidthProperty().bind(previewPane.widthProperty());
        preview.fitHeightProperty().bind(previewPane.heightProperty());
        preview.setPreserveRatio(true);
 
        generatePng();
    }
    
    private void openFile(File f) {
        try {
            // Open file in editor.
            String data = new String(Files.readAllBytes(f.toPath()));

            codeArea.replaceText(data);

            // Go ahead and generate an Image to display.
            // generatePng(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveData() {
        if (model.untitled) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PlantUML File");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("PlantUML Files", "*.plantuml"),
                    new ExtensionFilter("All Files", "*.*"));
            fileChooser.setInitialDirectory(Common.getLastUsedFolder());
            
            File selectedFile = fileChooser.showSaveDialog(codeAreaPane.getScene().getWindow());
            
            if (selectedFile == null) {
                return;
            }
            else {
                model.backingFile = selectedFile;
                
                // Save the last-used folder.
                Common.setLastUsedFolder(selectedFile.getParent());
            }
        }

        System.out.println("Save Tab: " + model.backingFile);
        
        try {
            Files.write(
                model.backingFile.toPath(), 
                codeArea.getText().getBytes(StandardCharsets.UTF_8), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            
            tab.setText(model.backingFile.getName());

            // No longer Untitled, so skip the dialog from now on.
            model.untitled = false;

            generatePng();
            
            // codeAreaPane.getParent()).setText(model.backingFile.getName();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void generatePng()
    {
        // Time the image generation.
        long startTime = System.nanoTime();

        ByteArrayOutputStream png = new ByteArrayOutputStream(1000000);
        SourceStringReader reader = new SourceStringReader(codeArea.getText());

        // Write the first image to "png"
        String desc;
        
        try {
            desc = reader.generateImage(png);
            System.out.println(desc);
        } catch (IOException ex) {
            Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        if (desc.equals("(Error)")) {
//            
//        }
        
        InputStream pngLoad = new ByteArrayInputStream(png.toByteArray());
        
        Image diagram = new Image(pngLoad);
       
        preview.setImage(diagram);
        
        // Return a null string if no generation.
        
        long stopTime = System.nanoTime();
        long elapsed = (stopTime - startTime) / 10000000;

        System.out.println(Long.toString(elapsed) + "ms");

        // TODO: Update status bar.
        // elapsedTimeMs.setText(Long.toString(elapsed) + "ms");
    }
    
    @FXML
    protected void handleOnCloseRequest() {
        executors.remove(executor);

        System.out.println("Shutting down ExecutorService: " + executor);
        if (!executor.isShutdown()) executor.shutdown();
    }
  
    @FXML
    protected void handleShortcuts(KeyEvent e) {        
        if (refresh.match(e)) {
            generatePng();
            e.consume();
        }
        else
        if (Common.SAVE.match(e)) {            
            // Check the current TabModel and save data
            // to that file where possible.
            saveData();
            e.consume();
        }
    }

    protected void setTabModel(TabModel model) {
        this.model = model;
    }

}

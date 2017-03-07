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
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import net.sourceforge.plantuml.SourceStringReader;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Max
 */
public class MainController implements Initializable {

    // PlantUML syntax is defined in the LanguageDescriptor class:
    // https://raw.githubusercontent.com/plantuml/plantuml/master/src/net/sourceforge/plantuml/syntax/LanguageDescriptor.java
    
    protected static final String[] PUML_ATS = new String[] {
        "@startuml", "@enduml", "@startdot", "@enddot", "@startsalt", 
        "@endsalt"
    };

    protected static final String[] PUML_PREPROCS = new String[] {
        "!include", "!pragma", "!define", "!undef", "!ifdef", 
        "!endif", "!ifndef", "!else", "!definelong", "!enddefinelong"
    };
    
    protected static final String[] PUML_TYPES = new String[] {
        "actor", "participant", "usecase", "class", "interface", 
        "abstract", "enum", "component", "state", "object", 
        "artifact", "folder", "rectangle", "node", "frame", "cloud", 
        "database", "storage", "agent", "boundary", "control", "entity", 
        "card", "file", "package", "queue"
    };
    
    protected static final String[] PUML_KEYWORDS = new String[] {
        "as", "also", "autonumber", "caption", "title", 
        "newpage", "box", "alt", "else", "opt", "loop", "par", "break", 
        "critical", "note", "rnote", "hnote", "legend", "group", "left", 
        "right", "of", "on", "link", "over", "end", "activate", "deactivate", 
        "destroy", "create", "footbox", "hide", "show", "skinparam", "skin", 
        "top", "bottom", "top to bottom direction", "package", "namespace", 
        "page", "up", "down", "if", "else", "elseif", "endif", "partition", 
        "footer", "header", "center", "rotate", "ref", "return", "is", 
        "repeat", "start", "stop", "while", "endwhile", "fork", "again", 
        "kill"
    };

    private static final String ATS_PATTERN        = "("    + String.join("|", PUML_ATS) + ")\\b";
    private static final String PREPROC_PATTERN    = "("    + String.join("|", PUML_PREPROCS) + ")\\b";
    private static final String TYPES_PATTERN      = "\\b(" + String.join("|", PUML_TYPES)       + ")\\b";
    private static final String KEYWORD_PATTERN    = "\\b(" + String.join("|", PUML_KEYWORDS)    + ")\\b";
    
    private static final String PAREN_PATTERN      = "\\(|\\)";
    private static final String BRACE_PATTERN      = "\\{|\\}";
    private static final String BRACKET_PATTERN    = "\\[|\\]";
    private static final String SEMICOLON_PATTERN  = "\\;";
    private static final String STRING_PATTERN     = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN    = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
               "(?<AT>"        + ATS_PATTERN        + ")"
            + "|(?<PREPROC>"   + PREPROC_PATTERN    + ")"
            + "|(?<TYPE>"      + TYPES_PATTERN      + ")"
            + "|(?<KEYWORD>"   + KEYWORD_PATTERN    + ")"

            + "|(?<PAREN>"     + PAREN_PATTERN      + ")"
            + "|(?<BRACE>"     + BRACE_PATTERN      + ")"
            + "|(?<BRACKET>"   + BRACKET_PATTERN    + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN  + ")"
            + "|(?<STRING>"    + STRING_PATTERN     + ")"
            + "|(?<COMMENT>"   + COMMENT_PATTERN    + ")"
    );

    private static final String sampleCode = String.join("\n", new String[] {
        "!define",
        "",
        "@startuml",
        "",
        "Alice -> Bob: Authentication Request",
        "Bob --> Alice: Authentication Response",
        "",
        "Alice -> Bob: Another authentication Request",
        "Alice <-- Bob: Another authentication Response",
        "",
        "Alice -> Bob: Request",
        "Alice <-- Bob: Response",
        "",
        "Alice -> Bob: Request",
        "Alice <-- Bob: Response",
        "",
        "Alice -> Bob: Request",
        "Alice <-- Bob: Response",
        "@enduml"
    });

    private       CodeArea        codeArea;
    public static ExecutorService executor;

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

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
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


        // Attach the CodeArea.
        executor = Executors.newSingleThreadExecutor();
        
        codeArea = new CodeArea();
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
        codeArea.replaceText(0, 0, sampleCode);
        
        // TODO: Rerender after no keystroke has been entered in 500ms.
        
        // TODO: Properly set up keyboard shortcuts (CTRL-S saves the file and rerenders 
        // the PlantUML preview)
        codeArea.setOnKeyReleased((KeyEvent event) -> {
            // if (event.getCode() == KeyCode.S && event.isControlDown()) {
            if (save.match(event)) {
                try {
                    generatePng(codeArea.getText());
                } catch (IOException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
//        codeAreaPane.getChildren().add(codeArea);
//        codeAreaPane.getStylesheets().add(FabrikUml.class.getResource("plantuml-keywords.css").toExternalForm());
//        
//        imageView.fitWidthProperty().bind(imagePane.widthProperty());
//        imageView.fitHeightProperty().bind(imagePane.heightProperty());
//        imageView.setPreserveRatio(true);
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

            codeArea.replaceText(data);

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

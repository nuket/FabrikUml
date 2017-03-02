/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vilimpoc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.sourceforge.plantuml.SourceStringReader;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Max
 */
public class MainController implements Initializable {
    
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private static final String sampleCode = String.join("\n", new String[] {
            "package com.example;",
            "",
            "import java.util.*;",
            "",
            "public class Foo extends Bar implements Baz {",
            "",
            "    /*",
            "     * multi-line comment",
            "     */",
            "    public static void main(String[] args) {",
            "        // single-line comment",
            "        for(String arg: args) {",
            "            if(arg.length() != 0)",
            "                System.out.println(arg);",
            "            else",
            "                System.err.println(\"Warning: empty string as argument\");",
            "        }",
            "    }",
            "",
            "}"
    });

    private CodeArea codeArea;
    public static ExecutorService executor;

//    @Override
//    public void start(Stage primaryStage) {
//        executor = Executors.newSingleThreadExecutor();
//        codeArea = new CodeArea();
//        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
//        codeArea.richChanges()
//                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
//                .successionEnds(Duration.ofMillis(500))
//                .supplyTask(this::computeHighlightingAsync)
//                .awaitLatest(codeArea.richChanges())
//                .filterMap(t -> {
//                    if(t.isSuccess()) {
//                        return Optional.of(t.get());
//                    } else {
//                        t.getFailure().printStackTrace();
//                        return Optional.empty();
//                    }
//                })
//                .subscribe(this::applyHighlighting);
//        codeArea.replaceText(0, 0, sampleCode);
//
//        Scene scene = new Scene(new StackPane(new VirtualizedScrollPane<>(codeArea)), 600, 400);
//        scene.getStylesheets().add(FabrikUml.class.getResource("java-keywords.css").toExternalForm());
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Java Keywords Async Demo");
//        primaryStage.show();
//    }
//
//    @Override
//    public void stop() {
//        executor.shutdown();
//    }

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
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
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
    
    @FXML
    protected ListView<String> documentListView;
    
    @FXML
    protected StackPane codeAreaPane;
    
    @FXML
    protected StackPane imagePane;
    
    @FXML
    protected ImageView imageView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        documentListView.setItems(filenames);
        
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

//        Scene scene = new Scene(new StackPane(new VirtualizedScrollPane<>(codeArea)), 600, 400);
//        scene.getStylesheets().add(FabrikUml.class.getResource("java-keywords.css").toExternalForm());

//        codeArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        codeAreaPane.getChildren().add(codeArea);
//        codeAreaPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // codeAreaPane.resize(600, 400);
        codeAreaPane.getStylesheets().add(FabrikUml.class.getResource("java-keywords.css").toExternalForm());
        
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Java Keywords Async Demo");
//        primaryStage.show();
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
            
            openFile(filenames.get(filenames.size() - 1));
        }
        
        e.setDropCompleted(success);
        e.consume();
    }
    
    @FXML
    protected void handleMouseClicked(MouseEvent e) {
        String filename = documentListView.getSelectionModel().getSelectedItem();
        Logger.getGlobal().warning(filename);
        
        openFile(filename);
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
        ByteArrayOutputStream png = new ByteArrayOutputStream(1000000);
        SourceStringReader reader = new SourceStringReader(uml);

        // Write the first image to "png"
        String desc = reader.generateImage(png);
        
        Logger.getGlobal().warning(desc);
        
        InputStream pngLoad = new ByteArrayInputStream(png.toByteArray());
        
        Image diagram = new Image(pngLoad);
        // imagePane.set
        // imagePane.getBackground().getImages().add(new BackgroundImage(diagram, null, null, null, null));
        
        imageView.setImage(diagram);
        
        // Return a null string if no generation.
    }

}

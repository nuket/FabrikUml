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

import java.util.regex.Pattern;

public class PlantUmlSyntax {

    // PlantUML syntax is defined in the LanguageDescriptor class:
    // https://raw.githubusercontent.com/plantuml/plantuml/master/src/net/sourceforge/plantuml/syntax/LanguageDescriptor.java
    protected static final String[] PUML_ATS = new String[] {
        "@startuml", "@enduml", "@startdot", "@enddot", "@startsalt", "@endsalt"
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

    static final Pattern PATTERN = Pattern.compile(
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

    static final String SAMPLE_CODE = String.join("\n", new String[] {
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
        "",
        "@enduml"
    });
    
}

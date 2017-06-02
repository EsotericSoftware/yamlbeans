package com.esotericsoftware.yamlbeans.parser;

public class ParserException extends RuntimeException {
    
    private Parser parser;

    public ParserException(Parser parser, String message) {
        super("Line " + parser.tokenizer.getLineNumber() + ", column " + parser.tokenizer.getColumn() + ": " + message);
        this.parser = parser;
    }
}

package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.DocumentStartEvent;
import com.esotericsoftware.yamlbeans.tokenizer.Token;

import static com.esotericsoftware.yamlbeans.tokenizer.TokenType.DOCUMENT_START;

public class DocumentStartStrategy extends AbstractEventStrategy {

    public DocumentStartStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        Token token = parser.peekNextToken();
        DocumentStartEvent documentStartEvent = parser.processDirectives(true);
        if (parser.peekNextTokenType() != DOCUMENT_START)
            throw new Parser.ParserException(parser, "Expected 'document start' but found: " + token.getType());
        
        parser.getNextToken();
        return documentStartEvent;
    }
}

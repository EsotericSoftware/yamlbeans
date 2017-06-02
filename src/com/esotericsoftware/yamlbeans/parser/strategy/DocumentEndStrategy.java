package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class DocumentEndStrategy extends AbstractEventStrategy {

    public DocumentEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        boolean explicit = false;

        while (parser.peekNextTokenType() == TokenType.DOCUMENT_END) {
            parser.getNextToken();
            explicit = true;
        }

        return explicit ? Event.DOCUMENT_END_TRUE : Event.DOCUMENT_END_FALSE;
    }
}

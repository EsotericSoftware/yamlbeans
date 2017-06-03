package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowEntryMarkerStrategy extends AbstractEventStrategy {

    public FlowEntryMarkerStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() == TokenType.FLOW_ENTRY) {
            parser.getNextToken();
        }
        return null;
    }
}

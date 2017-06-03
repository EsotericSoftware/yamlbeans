package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowSequenceEntryStrategy extends AbstractEventStrategy {

    public FlowSequenceEntryStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() != TokenType.FLOW_SEQUENCE_END) {
            if (parser.peekNextTokenType() == TokenType.KEY) {
                parser.pushParseStack(new FlowSequenceEntryStrategy(parser));
                parser.pushParseStack(new FlowEntryMarkerStrategy(parser));
                parser.pushParseStack(new FlowInternalMappingEndStrategy(parser));
                parser.pushParseStack(new FlowInternalValueStrategy(parser));
                parser.pushParseStack(new FlowInternalContentStrategy(parser));
                parser.pushParseStack(new FlowInternalMappingStartStrategy(parser));
            } else {
                parser.pushParseStack(new FlowSequenceEntryStrategy(parser));
                parser.pushParseStack(new FlowNodeStrategy(parser));
                parser.pushParseStack(new FlowEntryMarkerStrategy(parser));
            }
        }
        return null;
    }
}

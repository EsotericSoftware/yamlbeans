package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowMappingEntryStrategy extends AbstractEventStrategy {

    public FlowMappingEntryStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() != TokenType.FLOW_MAPPING_END) {
            if (parser.peekNextTokenType() == TokenType.KEY) {
                parser.pushParseStack(new FlowMappingEntryStrategy(parser));
                parser.pushParseStack(new FlowEntryMarkerStrategy(parser));
                parser.pushParseStack(new FlowMappingInternalValueStrategy(parser));
                parser.pushParseStack(new FlowMappingInternalContentStrategy(parser));
            } else {
                parser.pushParseStack(new FlowMappingEntryStrategy(parser));
                parser.pushParseStack(new FlowNodeStrategy(parser));
                parser.pushParseStack(new FlowEntryMarkerStrategy(parser));
            }
        }
        return null;
    }
}

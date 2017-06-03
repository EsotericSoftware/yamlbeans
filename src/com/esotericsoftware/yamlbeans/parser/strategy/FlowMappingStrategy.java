package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class FlowMappingStrategy extends AbstractEventStrategy {

    public FlowMappingStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new FlowMappingEndStrategy(parser));
        parser.pushParseStack(new FlowMappingEntryStrategy(parser));
        parser.pushParseStack(new FlowMappingStartStrategy(parser));
        return null;
    }
}

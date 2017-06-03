package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.MappingStartEvent;

public class FlowInternalMappingStartStrategy extends AbstractEventStrategy {

    public FlowInternalMappingStartStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.getNextToken();
        return new MappingStartEvent(null, null, true, true);
    }
}

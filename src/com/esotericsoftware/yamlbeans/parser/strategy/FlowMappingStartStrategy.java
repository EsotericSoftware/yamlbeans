package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.MappingStartEvent;

public class FlowMappingStartStrategy extends AbstractEventStrategy {

    public FlowMappingStartStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        boolean implicit = parser.getTag(0) == null || parser.getTag(0).equals("!");
        parser.getNextToken();
        return new MappingStartEvent(parser.getAnchor(0), parser.getTag(0), implicit, true);
    }
}

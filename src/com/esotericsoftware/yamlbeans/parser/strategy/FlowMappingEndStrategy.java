package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class FlowMappingEndStrategy extends AbstractEventStrategy {

    public FlowMappingEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.getNextToken();
        return Event.MAPPING_END;
    }
}

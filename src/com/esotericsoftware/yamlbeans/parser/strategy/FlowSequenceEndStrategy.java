package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class FlowSequenceEndStrategy extends AbstractEventStrategy {

    public FlowSequenceEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.getNextToken();
        return Event.SEQUENCE_END;
    }
}

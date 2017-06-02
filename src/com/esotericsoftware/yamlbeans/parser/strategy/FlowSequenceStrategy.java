package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class FlowSequenceStrategy extends AbstractEventStrategy {

    public FlowSequenceStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new FlowSequenceEndStrategy(parser));
        parser.pushParseStack(new FlowSequenceEntryStrategy(parser));
        parser.pushParseStack(new FlowSequenceStartStrategy(parser));
        return null;
    }
}

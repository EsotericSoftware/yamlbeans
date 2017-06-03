package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class BlockIndentlessSequenceEndStrategy extends AbstractEventStrategy {

    public BlockIndentlessSequenceEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        return Event.SEQUENCE_END;
    }
}

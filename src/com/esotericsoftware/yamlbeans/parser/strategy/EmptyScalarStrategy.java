package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.ScalarEvent;

public class EmptyScalarStrategy extends AbstractEventStrategy {

    public EmptyScalarStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        return new ScalarEvent(null, null, new boolean[]{true, false}, null, (char) 0);
    }
}

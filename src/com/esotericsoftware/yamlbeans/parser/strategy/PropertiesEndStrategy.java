package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class PropertiesEndStrategy extends AbstractEventStrategy {

    public PropertiesEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.removeAnchor(0);
        parser.removeTag(0);
        return null;
    }
}

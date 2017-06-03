package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class DocumentStartImplicitStrategy extends AbstractEventStrategy {

    public DocumentStartImplicitStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        return parser.processDirectives(false);
    }
}

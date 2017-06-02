package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class StreamStrategy extends AbstractEventStrategy {

    public StreamStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new StreamEndStrategy(parser));
        parser.pushParseStack(new ExplicitDocumentStrategy(parser));
        parser.pushParseStack(new ImplicitDocumentStrategy(parser));
        parser.pushParseStack(new StreamStartStrategy(parser));
        return null;
    }
}

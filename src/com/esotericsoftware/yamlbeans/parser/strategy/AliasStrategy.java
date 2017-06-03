package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.AliasEvent;
import com.esotericsoftware.yamlbeans.tokenizer.AliasToken;

public class AliasStrategy extends AbstractEventStrategy {

    public AliasStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        AliasToken token = (AliasToken) parser.getNextToken();
        return new AliasEvent(token.getInstanceName());
    }
}

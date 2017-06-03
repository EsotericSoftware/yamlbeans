package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowNodeStrategy extends AbstractEventStrategy {

    public FlowNodeStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() == TokenType.ALIAS) {
            parser.pushParseStack(new AliasStrategy(parser));
        } else {
            parser.pushParseStack(new PropertiesEndStrategy(parser));
            parser.pushParseStack(new FlowContentStrategy(parser));
            parser.pushParseStack(new PropertiesStrategy(parser));
        }
        return null;
    }
}

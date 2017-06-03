package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowInternalValueStrategy extends AbstractEventStrategy {

    public FlowInternalValueStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() == TokenType.VALUE) {
            parser.getNextToken();
            if (parser.peekNextTokenType() == TokenType.FLOW_ENTRY
                    || parser.peekNextTokenType() == TokenType.FLOW_SEQUENCE_END) {
                parser.pushParseStack(new EmptyScalarStrategy(parser));
            } else {
                parser.pushParseStack(new FlowNodeStrategy(parser));
            }
        } else {
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        }
        return null;
    }
}

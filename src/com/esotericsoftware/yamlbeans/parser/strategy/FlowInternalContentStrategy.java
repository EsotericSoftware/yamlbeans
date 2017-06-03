package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowInternalContentStrategy extends AbstractEventStrategy {

    public FlowInternalContentStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();
        if (type == TokenType.VALUE || type == TokenType.FLOW_ENTRY
                || type == TokenType.FLOW_SEQUENCE_END) {
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        } else {
            parser.pushParseStack(new FlowNodeStrategy(parser));
        }
        return null;
    }
}

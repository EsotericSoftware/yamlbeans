package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowMappingInternalContentStrategy extends AbstractEventStrategy {

    public FlowMappingInternalContentStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();
        if (type == TokenType.VALUE || type == TokenType.FLOW_ENTRY
                || type == TokenType.FLOW_MAPPING_END) {
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        } else {
            parser.getNextToken();
            parser.pushParseStack(new FlowNodeStrategy(parser));
        }
        return null;
    }
}

package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowMappingInternalValueStrategy extends AbstractEventStrategy {

    public FlowMappingInternalValueStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() == TokenType.VALUE) {
            parser.getNextToken();
            if (parser.peekNextTokenType() == TokenType.FLOW_ENTRY
                    || parser.peekNextTokenType() == TokenType.FLOW_MAPPING_END) {
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

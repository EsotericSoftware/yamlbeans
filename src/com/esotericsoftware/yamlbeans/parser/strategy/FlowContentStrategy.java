package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.ParserException;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class FlowContentStrategy extends AbstractEventStrategy {

    public FlowContentStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();

        if (type == TokenType.FLOW_SEQUENCE_START) {
            parser.pushParseStack(new FlowSequenceStrategy(parser));
        } else if (type == TokenType.FLOW_MAPPING_START) {
            parser.pushParseStack(new FlowMappingStrategy(parser));
        } else if (type == TokenType.SCALAR) {
            parser.pushParseStack(new ScalarStrategy(parser));
        } else {
            throw new ParserException(parser, "Expected a sequence, mapping, or scalar but found: " + type);
        }

        return null;
    }
}

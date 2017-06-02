package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.ParserException;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class BlockMappingEndStrategy extends AbstractEventStrategy {

    public BlockMappingEndStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() != TokenType.BLOCK_END) {
            throw new ParserException(parser, "Expected a 'block end' but found: " + parser.peekNextTokenType());
        }

        parser.getNextToken();
        return Event.MAPPING_END;
    }
}

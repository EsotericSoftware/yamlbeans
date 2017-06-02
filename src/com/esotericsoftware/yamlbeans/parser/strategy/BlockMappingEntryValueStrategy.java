package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class BlockMappingEntryValueStrategy extends AbstractEventStrategy {

    public BlockMappingEntryValueStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();

        if (type == TokenType.VALUE) {
            parser.getNextToken();
            type = parser.peekNextTokenType();

            if (type == TokenType.KEY || type == TokenType.VALUE
                    || type == TokenType.BLOCK_END)
                parser.pushParseStack(new EmptyScalarStrategy(parser));
            else {
                parser.pushParseStack(new BlockNodeOrIndentlessSequenceStrategy(parser));
                parser.pushParseStack(new PropertiesStrategy(parser));
            }
        } else if (type == TokenType.KEY) {
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        }

        return null;
    }
}

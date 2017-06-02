package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class BlockMappingEntryStrategy extends AbstractEventStrategy {

    public BlockMappingEntryStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();

        if (type == TokenType.KEY) {
            parser.getNextToken();
            type = parser.peekNextTokenType();

            if (type == TokenType.KEY || type == TokenType.VALUE
                    || type == TokenType.BLOCK_END) {
                parser.pushParseStack(new BlockMappingEntryStrategy(parser));
                parser.pushParseStack(new BlockMappingEntryValueStrategy(parser));
                parser.pushParseStack(new EmptyScalarStrategy(parser));
            } else {
                parser.pushParseStack(new BlockMappingEntryStrategy(parser));
                parser.pushParseStack(new BlockMappingEntryValueStrategy(parser));
                parser.pushParseStack(new BlockNodeOrIndentlessSequenceStrategy(parser));
                parser.pushParseStack(new PropertiesStrategy(parser));
            }
        } else if (type == TokenType.VALUE) {
            parser.pushParseStack(new BlockMappingEntryStrategy(parser));
            parser.pushParseStack(new BlockMappingEntryValueStrategy(parser));
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        }

        return null;
    }
}

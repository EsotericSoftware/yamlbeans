package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class BlockNodeOrIndentlessSequenceStrategy extends AbstractEventStrategy {

    public BlockNodeOrIndentlessSequenceStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();

        if (type == TokenType.ALIAS)
            parser.pushParseStack(new AliasStrategy(parser));
        else if (type == TokenType.BLOCK_ENTRY) {
            parser.pushParseStack(new IndentlessBlockSequenceStrategy(parser));
        } else {
            parser.pushParseStack(new BlockContentStrategy(parser));
        }

        return null;
    }
}

package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class IndentlessBlockSequenceEntryStrategy extends AbstractEventStrategy {

    public IndentlessBlockSequenceEntryStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() == TokenType.BLOCK_ENTRY) {
            parser.getNextToken();
            TokenType type = parser.peekNextTokenType();

            if (type == TokenType.BLOCK_ENTRY || type == TokenType.KEY
                    || type == TokenType.VALUE || type == TokenType.BLOCK_END) {
                parser.pushParseStack(new IndentlessBlockSequenceEntryStrategy(parser));
                parser.pushParseStack(new EmptyScalarStrategy(parser));
            } else {
                parser.pushParseStack(new IndentlessBlockSequenceEntryStrategy(parser));
                parser.pushParseStack(new BlockNodeStrategy(parser));
            }
        }

        return null;
    }
}

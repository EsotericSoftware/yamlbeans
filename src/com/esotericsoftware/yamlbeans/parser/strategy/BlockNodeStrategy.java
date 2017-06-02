package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class BlockNodeStrategy extends AbstractEventStrategy {

    public BlockNodeStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();

        if (type == TokenType.DIRECTIVE || type == TokenType.DOCUMENT_START
                || type == TokenType.DOCUMENT_END || type == TokenType.STREAM_END) {
            parser.pushParseStack(new EmptyScalarStrategy(parser));
        } else if (type == TokenType.ALIAS) {
            parser.pushParseStack(new AliasStrategy(parser));
        } else {
            parser.pushParseStack(new PropertiesEndStrategy(parser));
            parser.pushParseStack(new BlockContentStrategy(parser));
            parser.pushParseStack(new PropertiesStrategy(parser));
        }

        return null;
    }
}

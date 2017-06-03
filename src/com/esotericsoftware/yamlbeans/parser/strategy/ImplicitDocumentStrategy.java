package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class ImplicitDocumentStrategy extends AbstractEventStrategy {

    public ImplicitDocumentStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        TokenType type = parser.peekNextTokenType();
        if (!(type == TokenType.DIRECTIVE || type == TokenType.DOCUMENT_START
                || type == TokenType.STREAM_END)) {
            parser.pushParseStack(new DocumentEndStrategy(parser));
            parser.pushParseStack(new BlockNodeStrategy(parser));
            parser.pushParseStack(new DocumentStartImplicitStrategy(parser));
        }
        return null;
    }
}

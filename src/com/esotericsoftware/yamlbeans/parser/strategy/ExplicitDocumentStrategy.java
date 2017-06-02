package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class ExplicitDocumentStrategy extends AbstractEventStrategy {

    public ExplicitDocumentStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        if (parser.peekNextTokenType() != TokenType.STREAM_END) {
            parser.pushParseStack(new ExplicitDocumentStrategy(parser));
            parser.pushParseStack(new DocumentEndStrategy(parser));
            parser.pushParseStack(new BlockNodeStrategy(parser));
            parser.pushParseStack(new DocumentStartStrategy(parser));
        }
        return null;
    }
}

package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class IndentlessBlockSequenceStrategy extends AbstractEventStrategy {

    public IndentlessBlockSequenceStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new BlockIndentlessSequenceEndStrategy(parser));
        parser.pushParseStack(new IndentlessBlockSequenceEntryStrategy(parser));
        parser.pushParseStack(new BlockIndentlessSequenceStartStrategy(parser));
        return null;
    }
}

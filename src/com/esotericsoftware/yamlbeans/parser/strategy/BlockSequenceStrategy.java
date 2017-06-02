package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class BlockSequenceStrategy extends AbstractEventStrategy {

    public BlockSequenceStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new BlockSequenceEndStrategy(parser));
        parser.pushParseStack(new BlockSequenceEntryStrategy(parser));
        parser.pushParseStack(new BlockSequenceStartStrategy(parser));
        return null;
    }
}

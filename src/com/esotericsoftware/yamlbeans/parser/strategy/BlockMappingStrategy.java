package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;

public class BlockMappingStrategy extends AbstractEventStrategy {

    public BlockMappingStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        parser.pushParseStack(new BlockMappingEndStrategy(parser));
        parser.pushParseStack(new BlockMappingEntryStrategy(parser));
        parser.pushParseStack(new BlockMappingStartStrategy(parser));
        return null;
    }
}

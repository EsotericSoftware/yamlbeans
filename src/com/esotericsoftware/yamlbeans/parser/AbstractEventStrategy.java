package com.esotericsoftware.yamlbeans.parser;

public abstract class AbstractEventStrategy implements EventStrategy {

    protected final Parser parser;

    public AbstractEventStrategy(Parser parser) {
        this.parser = parser;
    }
}

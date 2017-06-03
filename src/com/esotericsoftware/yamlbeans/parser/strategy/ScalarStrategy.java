package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.event.ScalarEvent;
import com.esotericsoftware.yamlbeans.tokenizer.ScalarToken;

public class ScalarStrategy extends AbstractEventStrategy {

    public ScalarStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        boolean[] implicit;
        ScalarToken token = (ScalarToken) parser.getNextToken();

        if (token.getPlain() && parser.getTag(0) == null || "!".equals(parser.getTag(0))) {
            implicit = new boolean[]{true, false};
        } else if (parser.getTag(0) == null) {
            implicit = new boolean[]{false, true};
        } else {
            implicit = new boolean[]{false, false};
        }

        return new ScalarEvent(parser.getAnchor(0), parser.getTag(0), implicit, token.getValue(), token.getStyle());
    }
}

package com.esotericsoftware.yamlbeans.parser.strategy;

import com.esotericsoftware.yamlbeans.parser.AbstractEventStrategy;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.tokenizer.AnchorToken;
import com.esotericsoftware.yamlbeans.tokenizer.TagToken;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;

public class PropertiesStrategy extends AbstractEventStrategy {

    public PropertiesStrategy(Parser parser) {
        super(parser);
    }

    public Event getEvent() {
        String anchor = null;
        String tag = null;
        String tagHandle = null;
        String tagSuffix = null;

        if (parser.peekNextTokenType() == TokenType.ANCHOR) {
            anchor = ((AnchorToken) parser.getNextToken()).getInstanceName();
            if (parser.peekNextTokenType() == TokenType.TAG) {
                TagToken tagToken = (TagToken) parser.getNextToken();
                tagHandle = tagToken.getHandle();
                tagSuffix = tagToken.getSuffix();
            }
        } else if (parser.peekNextTokenType() == TokenType.TAG) {
            TagToken tagToken = (TagToken) parser.getNextToken();
            tagHandle = tagToken.getHandle();
            tagSuffix = tagToken.getSuffix();
            if (parser.peekNextTokenType() == TokenType.ANCHOR) {
                anchor = ((AnchorToken) parser.getNextToken()).getInstanceName();
            }
        }

        if (tagHandle != null && !tagHandle.equals("!")) {
            if (!parser.containsTagHandle(tagHandle)) {
                throw new Parser.ParserException(parser, "Undefined tag handle: " + tagHandle);
            }

            tag = parser.getTagHandle(tagHandle) + tagSuffix;
        } else {
            tag = tagSuffix;
        }

        parser.addAnchor(0, anchor);
        parser.addTag(0, tag);

        return null;
    }
}

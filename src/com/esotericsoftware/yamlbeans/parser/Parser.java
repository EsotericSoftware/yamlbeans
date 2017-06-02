/*
 * Copyright (c) 2008 Nathan Sweet, Copyright (c) 2006 Ola Bini
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.esotericsoftware.yamlbeans.parser;

import com.esotericsoftware.yamlbeans.Version;
import com.esotericsoftware.yamlbeans.parser.event.DocumentStartEvent;
import com.esotericsoftware.yamlbeans.parser.strategy.StreamStrategy;
import com.esotericsoftware.yamlbeans.tokenizer.DirectiveToken;
import com.esotericsoftware.yamlbeans.tokenizer.Token;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer.TokenizerException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esotericsoftware.yamlbeans.tokenizer.TokenType.DIRECTIVE;

/**
 * Parses a stream of tokens into events.
 *
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a>
 */
public class Parser {

    private static final Map<String, String> DEFAULT_TAGS_1_0 = new HashMap<String, String>();
    private static final Map<String, String> DEFAULT_TAGS_1_1 = new HashMap<String, String>();

    static {
        DEFAULT_TAGS_1_0.put("!", "tag:yaml.org,2002:");

        DEFAULT_TAGS_1_1.put("!", "!");
        DEFAULT_TAGS_1_1.put("!!", "tag:yaml.org,2002:");
    }

    private Tokenizer tokenizer = null;
    private Version defaultVersion;
    private Version documentVersion;
    private Event peekedEvent;
    private final List<EventStrategy> parseStack;
    private final List<String> tags;
    private final List<String> anchors;
    private final Map<String, String> tagHandles;

    public Parser(Reader reader) {
        this(reader, new Version(1, 1));
    }

    public Parser(Reader reader, Version defaultVersion) {
        if (reader == null) throw new IllegalArgumentException("reader cannot be null.");
        if (defaultVersion == null) throw new IllegalArgumentException("defaultVersion cannot be null.");

        this.tokenizer = new Tokenizer(reader);
        this.defaultVersion = defaultVersion;
        this.parseStack = new ArrayList<EventStrategy>();
        this.tags = new ArrayList<String>();
        this.anchors = new ArrayList<String>();
        this.tagHandles = new HashMap<String, String>();

        pushParseStack(new StreamStrategy(this));
    }

    public Event getNextEvent() throws ParserException, TokenizerException {
        if (peekedEvent != null) {
            try {
                return peekedEvent;
            } finally {
                peekedEvent = null;
            }
        }
        while (!parseStack.isEmpty()) {
            Event event = parseStack.remove(0).getEvent();
            if (event != null) {
                return event;
            }
        }
        return null;
    }

    public Event peekNextEvent() throws ParserException, TokenizerException {
        if (peekedEvent != null) return peekedEvent;
        peekedEvent = getNextEvent();
        return peekedEvent;
    }

    public TokenType peekNextTokenType() {
        return tokenizer.peekNextTokenType();
    }

    public Token peekNextToken() {
        return tokenizer.peekNextToken();
    }

    public Token getNextToken() {
        return tokenizer.getNextToken();
    }

    public void pushParseStack(EventStrategy eventStrategy) {
        parseStack.add(0, eventStrategy);
    }

    public void addTag(int index, String tag) {
        tags.add(index, tag);
    }

    public String getTag(int index) {
        return tags.get(index);
    }

    public void removeTag(int index) {
        tags.remove(index);
    }

    public void addAnchor(int index, String anchor) {
        anchors.add(index, anchor);
    }

    public String getAnchor(int index) {
        return anchors.get(index);
    }

    public void removeAnchor(int index) {
        anchors.remove(index);
    }

    public boolean containsTagHandle(String tagHandle) {
        return tagHandles.containsKey(tagHandle);
    }

    public String getTagHandle(String tagHandle) {
        return tagHandles.get(tagHandle);
    }

    public int getLineNumber() {
        return tokenizer.getLineNumber();
    }

    public int getColumn() {
        return tokenizer.getColumn();
    }

    public void close() throws IOException {
        tokenizer.close();
    }

    public DocumentStartEvent processDirectives(boolean explicit) {
        documentVersion = null;
        while (tokenizer.peekNextTokenType() == DIRECTIVE) {
            DirectiveToken token = (DirectiveToken) tokenizer.getNextToken();
            if (token.getDirective().equals("YAML")) {
                if (documentVersion != null) throw new ParserException(this, "Duplicate YAML directive.");
                documentVersion = new Version(token.getValue());
                if (documentVersion.major != 1)
                    throw new ParserException(this, "Unsupported YAML version (1.x is required): " + documentVersion);
            } else if (token.getDirective().equals("TAG")) {
                String[] values = token.getValue().split(" ");
                String handle = values[0];
                String prefix = values[1];
                if (tagHandles.containsKey(handle))
                    throw new ParserException(this, "Duplicate tag directive: " + handle);
                tagHandles.put(handle, prefix);
            }
        }

        Version version;
        if (documentVersion != null)
            version = documentVersion;
        else
            version = defaultVersion;

        Map<String, String> tags = null;
        if (!tagHandles.isEmpty()) tags = new HashMap(tagHandles);
        Map<String, String> baseTags = version.minor == 0 ? DEFAULT_TAGS_1_0 : DEFAULT_TAGS_1_1;
        for (String key : baseTags.keySet())
            if (!tagHandles.containsKey(key)) tagHandles.put(key, baseTags.get(key));
        return new DocumentStartEvent(explicit, version, tags);
    }

    public static class ParserException extends RuntimeException {
        public ParserException(Parser parser, String message) {
            super("Line " + parser.tokenizer.getLineNumber() + ", column " + parser.tokenizer.getColumn() + ": " + message);
        }
    }

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser(new FileReader("test/test.yml"));
        while (true) {
            Event event = parser.getNextEvent();
            if (event == null) break;
            System.out.println(event);
        }
    }
}

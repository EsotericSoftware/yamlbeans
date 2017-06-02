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

import static com.esotericsoftware.yamlbeans.tokenizer.TokenType.*;
import static com.esotericsoftware.yamlbeans.parser.ProductionType.*;

import com.esotericsoftware.yamlbeans.Version;
import com.esotericsoftware.yamlbeans.parser.event.*;
import com.esotericsoftware.yamlbeans.tokenizer.AliasToken;
import com.esotericsoftware.yamlbeans.tokenizer.AnchorToken;
import com.esotericsoftware.yamlbeans.tokenizer.DirectiveToken;
import com.esotericsoftware.yamlbeans.tokenizer.ScalarToken;
import com.esotericsoftware.yamlbeans.tokenizer.TagToken;
import com.esotericsoftware.yamlbeans.tokenizer.Token;
import com.esotericsoftware.yamlbeans.tokenizer.TokenType;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer.TokenizerException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Parses a stream of tokens into events.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a> */
public class Parser {
	Tokenizer tokenizer = null;
	List<Production> parseStack = null;
	final List<String> tags = new LinkedList();
	final List<String> anchors = new LinkedList();
	Map<String, String> tagHandles = new HashMap();
	Version defaultVersion, documentVersion;
	final Production[] table = new Production[46];
	Event peekedEvent;

	static {
		DEFAULT_TAGS_1_0.put("!", "tag:yaml.org,2002:");

		DEFAULT_TAGS_1_1.put("!", "!");
		DEFAULT_TAGS_1_1.put("!!", "tag:yaml.org,2002:");
	}
	
	public Parser (Reader reader) {
		this(reader, new Version(1, 1));
	}

	public Parser (Reader reader, Version defaultVersion) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null.");
		if (defaultVersion == null) throw new IllegalArgumentException("defaultVersion cannot be null.");

		tokenizer = new Tokenizer(reader);

		this.defaultVersion = defaultVersion;

		initProductionTable();

		parseStack = new LinkedList();
		parseStack.add(0, table[P_STREAM]);
	}

	public Event getNextEvent () throws ParserException, TokenizerException {
		if (peekedEvent != null) {
			try {
				return peekedEvent;
			} finally {
				peekedEvent = null;
			}
		}
		while (!parseStack.isEmpty()) {
			Event event = parseStack.remove(0).produce();
			if (event != null) {
				return event;
			}
		}
		return null;
	}

	public Event peekNextEvent () throws ParserException, TokenizerException {
		if (peekedEvent != null) return peekedEvent;
		peekedEvent = getNextEvent();
		return peekedEvent;
	}

	public int getLineNumber () {
		return tokenizer.getLineNumber();
	}

	public int getColumn () {
		return tokenizer.getColumn();
	}

	public void close () throws IOException {
		tokenizer.close();
	}
	
	public int addTable_pStream() {
		table[P_STREAM] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_STREAM_END]);
				parseStack.add(0, table[P_EXPLICIT_DOCUMENT]);
				parseStack.add(0, table[P_IMPLICIT_DOCUMENT]);
				parseStack.add(0, table[P_STREAM_START]);
				return null;
			}
		};
		if(table[P_STREAM] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pStreamStart() {
		table[P_STREAM_START] = new Production() {
			public Event produce () {
				tokenizer.getNextToken();
				return Event.STREAM_START;
			}
		};
		if(table[P_STREAM_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pStreamEnd() {
		table[P_STREAM_END] = new Production() {
			public Event produce () {
				tokenizer.getNextToken();
				return Event.STREAM_END;
			}
		};
		
		if(table[P_STREAM_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pImplicitDoucment() {
		table[P_IMPLICIT_DOCUMENT] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (!(type == DIRECTIVE || type == DOCUMENT_START || type == STREAM_END)) {
					parseStack.add(0, table[P_DOCUMENT_END]);
					parseStack.add(0, table[P_BLOCK_NODE]);
					parseStack.add(0, table[P_DOCUMENT_START_IMPLICIT]);
				}
				return null;
			}
		};
		
		if(table[P_IMPLICIT_DOCUMENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pExplicitDoucment() {
		table[P_EXPLICIT_DOCUMENT] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() != STREAM_END) {
					parseStack.add(0, table[P_EXPLICIT_DOCUMENT]);
					parseStack.add(0, table[P_DOCUMENT_END]);
					parseStack.add(0, table[P_BLOCK_NODE]);
					parseStack.add(0, table[P_DOCUMENT_START]);
				}
				return null;
			}
		};
		
		if(table[P_EXPLICIT_DOCUMENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pDocumentStart() {
		table[P_DOCUMENT_START] = new Production() {
			public Event produce () {
				Token token = tokenizer.peekNextToken();
				DocumentStartEvent documentStartEvent = processDirectives(true);
				if (tokenizer.peekNextTokenType() != DOCUMENT_START)
					throw new ParserException("Expected 'document start' but found: " + token.getType());
				tokenizer.getNextToken();
				return documentStartEvent;
			}
		};
		
		if(table[P_DOCUMENT_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pDocument_StartImplict() {
		table[P_DOCUMENT_START_IMPLICIT] = new Production() {
			public Event produce () {
				return processDirectives(false);
			}
		};
		
		if(table[P_DOCUMENT_START_IMPLICIT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pDoucmentEnd() {
		table[P_DOCUMENT_END] = new Production() {
			public Event produce () {
				boolean explicit = false;
				while (tokenizer.peekNextTokenType() == DOCUMENT_END) {
					tokenizer.getNextToken();
					explicit = true;
				}
				return explicit ? Event.DOCUMENT_END_TRUE : Event.DOCUMENT_END_FALSE;
			}
		};
		
		if(table[P_DOCUMENT_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockNode() {
		table[P_BLOCK_NODE] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == DIRECTIVE || type == DOCUMENT_START || type == DOCUMENT_END || type == STREAM_END)
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				else if (type == ALIAS)
					parseStack.add(0, table[P_ALIAS]);
				else {
					parseStack.add(0, table[P_PROPERTIES_END]);
					parseStack.add(0, table[P_BLOCK_CONTENT]);
					parseStack.add(0, table[P_PROPERTIES]);
				}
				return null;
			}
		};
		
		if(table[P_BLOCK_NODE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockContent() {
		table[P_BLOCK_CONTENT] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == BLOCK_SEQUENCE_START)
					parseStack.add(0, table[P_BLOCK_SEQUENCE]);
				else if (type == BLOCK_MAPPING_START)
					parseStack.add(0, table[P_BLOCK_MAPPING]);
				else if (type == FLOW_SEQUENCE_START)
					parseStack.add(0, table[P_FLOW_SEQUENCE]);
				else if (type == FLOW_MAPPING_START)
					parseStack.add(0, table[P_FLOW_MAPPING]);
				else if (type == SCALAR)
					parseStack.add(0, table[P_SCALAR]);
				else
					throw new ParserException("Expected a sequence, mapping, or scalar but found: " + type);
				return null;
			}
		};
		
		if(table[P_BLOCK_CONTENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pProperties() {
		table[P_PROPERTIES] = new Production() {
			public Event produce () {
				String anchor = null, tagHandle = null, tagSuffix = null;
				if (tokenizer.peekNextTokenType() == ANCHOR) {
					anchor = ((AnchorToken)tokenizer.getNextToken()).getInstanceName();
					if (tokenizer.peekNextTokenType() == TAG) {
						TagToken tagToken = (TagToken)tokenizer.getNextToken();
						tagHandle = tagToken.getHandle();
						tagSuffix = tagToken.getSuffix();
					}
				} else if (tokenizer.peekNextTokenType() == TAG) {
					TagToken tagToken = (TagToken)tokenizer.getNextToken();
					tagHandle = tagToken.getHandle();
					tagSuffix = tagToken.getSuffix();
					if (tokenizer.peekNextTokenType() == ANCHOR) anchor = ((AnchorToken)tokenizer.getNextToken()).getInstanceName();
				}
				String tag = null;
				if (tagHandle != null && !tagHandle.equals("!")) {
					if (!tagHandles.containsKey(tagHandle)) throw new ParserException("Undefined tag handle: " + tagHandle);
					tag = tagHandles.get(tagHandle) + tagSuffix;
				} else
					tag = tagSuffix;
				anchors.add(0, anchor);
				tags.add(0, tag);
				return null;
			}
		};
		
		if(table[P_PROPERTIES] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pPropertiesEnd() {
		table[P_PROPERTIES_END] = new Production() {
			public Event produce () {
				anchors.remove(0);
				tags.remove(0);
				return null;
			}
		};
		
		if(table[P_PROPERTIES_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowContent() {
		table[P_FLOW_CONTENT] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == FLOW_SEQUENCE_START)
					parseStack.add(0, table[P_FLOW_SEQUENCE]);
				else if (type == FLOW_MAPPING_START)
					parseStack.add(0, table[P_FLOW_MAPPING]);
				else if (type == SCALAR)
					parseStack.add(0, table[P_SCALAR]);
				else
					throw new ParserException("Expected a sequence, mapping, or scalar but found: " + type);
				return null;
			}
		};
		
		if(table[P_FLOW_CONTENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockSequence() {
		table[P_BLOCK_SEQUENCE] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_BLOCK_SEQUENCE_END]);
				parseStack.add(0, table[P_BLOCK_SEQUENCE_ENTRY]);
				parseStack.add(0, table[P_BLOCK_SEQUENCE_START]);
				return null;
			}
		};
		
		if(table[P_BLOCK_SEQUENCE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockMapping() {
		table[P_BLOCK_MAPPING] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_BLOCK_MAPPING_END]);
				parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY]);
				parseStack.add(0, table[P_BLOCK_MAPPING_START]);
				return null;
			}
		};
		if(table[P_BLOCK_MAPPING] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowSequence() {
		table[P_FLOW_SEQUENCE] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_FLOW_SEQUENCE_END]);
				parseStack.add(0, table[P_FLOW_SEQUENCE_ENTRY]);
				parseStack.add(0, table[P_FLOW_SEQUENCE_START]);
				return null;
			}
		};
		
		if(table[P_FLOW_SEQUENCE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowMapping() {
		table[P_FLOW_MAPPING] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_FLOW_MAPPING_END]);
				parseStack.add(0, table[P_FLOW_MAPPING_ENTRY]);
				parseStack.add(0, table[P_FLOW_MAPPING_START]);
				return null;
			}
		};
		
		if(table[P_FLOW_MAPPING] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pScalar() {
		table[P_SCALAR] = new Production() {
			public Event produce () {
				ScalarToken token = (ScalarToken)tokenizer.getNextToken();
				boolean[] implicit = null;
				if (token.getPlain() && tags.get(0) == null || "!".equals(tags.get(0)))
					implicit = new boolean[] {true, false};
				else if (tags.get(0) == null)
					implicit = new boolean[] {false, true};
				else
					implicit = new boolean[] {false, false};
				return new ScalarEvent(anchors.get(0), tags.get(0), implicit, token.getValue(), token.getStyle());
			}
		};
		
		if(table[P_SCALAR] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pBlockSequecne_Entry() {
		table[P_BLOCK_SEQUENCE_ENTRY] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == BLOCK_ENTRY) {
					tokenizer.getNextToken();
					TokenType type = tokenizer.peekNextTokenType();
					if (type == BLOCK_ENTRY || type == BLOCK_END) {
						parseStack.add(0, table[P_BLOCK_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					} else {
						parseStack.add(0, table[P_BLOCK_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_BLOCK_NODE]);
					}
				}
				return null;
			}
		};
		
		if(table[P_BLOCK_SEQUENCE_ENTRY] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockMapping_Entry() {
		table[P_BLOCK_MAPPING_ENTRY] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == KEY) {
					tokenizer.getNextToken();
					type = tokenizer.peekNextTokenType();
					if (type == KEY || type == VALUE || type == BLOCK_END) {
						parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY]);
						parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY_VALUE]);
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					} else {
						parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY]);
						parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY_VALUE]);
						parseStack.add(0, table[P_BLOCK_NODE_OR_INDENTLESS_SEQUENCE]);
						parseStack.add(0, table[P_PROPERTIES]);
					}
				} else if (type == VALUE) {
					parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY]);
					parseStack.add(0, table[P_BLOCK_MAPPING_ENTRY_VALUE]);
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				}
				return null;
			}
		};
		
		if(table[P_BLOCK_MAPPING_ENTRY] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pBlockMapping_EntryValue() {
		table[P_BLOCK_MAPPING_ENTRY_VALUE] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == VALUE) {
					tokenizer.getNextToken();
					type = tokenizer.peekNextTokenType();
					if (type == KEY || type == VALUE || type == BLOCK_END)
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					else {
						parseStack.add(0, table[P_BLOCK_NODE_OR_INDENTLESS_SEQUENCE]);
						parseStack.add(0, table[P_PROPERTIES]);
					}
				} else if (type == KEY) parseStack.add(0, table[P_EMPTY_SCALAR]);
				return null;
			}
		};
		
		if(table[P_BLOCK_MAPPING_ENTRY_VALUE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockNode_Or_IndentlessSequence() {
		table[P_BLOCK_NODE_OR_INDENTLESS_SEQUENCE] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == ALIAS)
					parseStack.add(0, table[P_ALIAS]);
				else if (type == BLOCK_ENTRY) {
					parseStack.add(0, table[P_INDENTLESS_BLOCK_SEQUENCE]);
				} else {
					parseStack.add(0, table[P_BLOCK_CONTENT]);
				}
				return null;
			}
		};
		
		if(table[P_BLOCK_NODE_OR_INDENTLESS_SEQUENCE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockSequecne_Start() {
		table[P_BLOCK_SEQUENCE_START] = new Production() {
			public Event produce () {
				boolean implicit = tags.get(0) == null || tags.get(0).equals("!");
				tokenizer.getNextToken();
				return new SequenceStartEvent(anchors.get(0), tags.get(0), implicit, false);
			}
		};
		
		if(table[P_BLOCK_SEQUENCE_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockSequecne_End() {
		table[P_BLOCK_SEQUENCE_END] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() != BLOCK_END)
					throw new ParserException("Expected a 'block end' but found: " + tokenizer.peekNextTokenType());
				tokenizer.getNextToken();
				return Event.SEQUENCE_END;
			}
		};
		
		if(table[P_BLOCK_SEQUENCE_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pBlockMapping_Start() {
		table[P_BLOCK_MAPPING_START] = new Production() {
			public Event produce () {
				boolean implicit = tags.get(0) == null || tags.get(0).equals("!");
				tokenizer.getNextToken();
				return new MappingStartEvent(anchors.get(0), tags.get(0), implicit, false);
			}
		};
		
		if(table[P_BLOCK_MAPPING_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	

	
	public int addTable_pBlockMapping_End() {
		table[P_BLOCK_MAPPING_END] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() != BLOCK_END)
					throw new ParserException("Expected a 'block end' but found: " + tokenizer.peekNextTokenType());
				tokenizer.getNextToken();
				return Event.MAPPING_END;
			}
		};
		
		if(table[P_BLOCK_MAPPING_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pIndentless_BlockSequence() {
		table[P_INDENTLESS_BLOCK_SEQUENCE] = new Production() {
			public Event produce () {
				parseStack.add(0, table[P_BLOCK_INDENTLESS_SEQUENCE_END]);
				parseStack.add(0, table[P_INDENTLESS_BLOCK_SEQUENCE_ENTRY]);
				parseStack.add(0, table[P_BLOCK_INDENTLESS_SEQUENCE_START]);
				return null;
			}
		};
		
		if(table[P_INDENTLESS_BLOCK_SEQUENCE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pIndentless_BlockSequence_Start() {
		table[P_BLOCK_INDENTLESS_SEQUENCE_START] = new Production() {
			public Event produce () {
				boolean implicit = tags.get(0) == null || tags.get(0).equals("!");
				return new SequenceStartEvent(anchors.get(0), tags.get(0), implicit, false);
			}
		};
		
		if(table[P_BLOCK_INDENTLESS_SEQUENCE_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pIndentless_BlockSequence_Entry() {
		table[P_INDENTLESS_BLOCK_SEQUENCE_ENTRY] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == BLOCK_ENTRY) {
					tokenizer.getNextToken();
					TokenType type = tokenizer.peekNextTokenType();
					if (type == BLOCK_ENTRY || type == KEY || type == VALUE || type == BLOCK_END) {
						parseStack.add(0, table[P_INDENTLESS_BLOCK_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					} else {
						parseStack.add(0, table[P_INDENTLESS_BLOCK_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_BLOCK_NODE]);
					}
				}
				return null;
			}
		};
		
		if(table[P_INDENTLESS_BLOCK_SEQUENCE_ENTRY] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pIndentless_BlockSequence_End() {
		table[P_BLOCK_INDENTLESS_SEQUENCE_END] = new Production() {
			public Event produce () {
				return Event.SEQUENCE_END;
			}
		};
		
		if(table[P_BLOCK_INDENTLESS_SEQUENCE_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pFlowSequence_Start() {
		table[P_FLOW_SEQUENCE_START] = new Production() {
			public Event produce () {
				boolean implicit = tags.get(0) == null || tags.get(0).equals("!");
				tokenizer.getNextToken();
				return new SequenceStartEvent(anchors.get(0), tags.get(0), implicit, true);
			}
		};
		
		if(table[P_FLOW_SEQUENCE_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pFlowSequence_Entry() {
		table[P_FLOW_SEQUENCE_ENTRY] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() != FLOW_SEQUENCE_END) {
					if (tokenizer.peekNextTokenType() == KEY) {
						parseStack.add(0, table[P_FLOW_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_FLOW_ENTRY_MARKER]);
						parseStack.add(0, table[P_FLOW_INTERNAL_MAPPING_END]);
						parseStack.add(0, table[P_FLOW_INTERNAL_VALUE]);
						parseStack.add(0, table[P_FLOW_INTERNAL_CONTENT]);
						parseStack.add(0, table[P_FLOW_INTERNAL_MAPPING_START]);
					} else {
						parseStack.add(0, table[P_FLOW_SEQUENCE_ENTRY]);
						parseStack.add(0, table[P_FLOW_NODE]);
						parseStack.add(0, table[P_FLOW_ENTRY_MARKER]);
					}
				}
				return null;
			}
		};
		
		if(table[P_FLOW_SEQUENCE_ENTRY] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pFlowSequence_End() {
		table[P_FLOW_SEQUENCE_END] = new Production() {
			public Event produce () {
				tokenizer.getNextToken();
				return Event.SEQUENCE_END;
			}
		};
		
		if(table[P_FLOW_SEQUENCE_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowMapping_Start() {
		table[P_FLOW_MAPPING_START] = new Production() {
			public Event produce () {
				boolean implicit = tags.get(0) == null || tags.get(0).equals("!");
				tokenizer.getNextToken();
				return new MappingStartEvent(anchors.get(0), tags.get(0), implicit, true);
			}
		};
		
		if(table[P_FLOW_MAPPING_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowMapping_Entry() {
		table[P_FLOW_MAPPING_ENTRY] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() != FLOW_MAPPING_END) {
					if (tokenizer.peekNextTokenType() == KEY) {
						parseStack.add(0, table[P_FLOW_MAPPING_ENTRY]);
						parseStack.add(0, table[P_FLOW_ENTRY_MARKER]);
						parseStack.add(0, table[P_FLOW_MAPPING_INTERNAL_VALUE]);
						parseStack.add(0, table[P_FLOW_MAPPING_INTERNAL_CONTENT]);
					} else {
						parseStack.add(0, table[P_FLOW_MAPPING_ENTRY]);
						parseStack.add(0, table[P_FLOW_NODE]);
						parseStack.add(0, table[P_FLOW_ENTRY_MARKER]);
					}
				}
				return null;
			}
		};
		
		if(table[P_FLOW_MAPPING_ENTRY] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowMapping_End() {
		table[P_FLOW_MAPPING_END] = new Production() {
			public Event produce () {
				tokenizer.getNextToken();
				return Event.MAPPING_END;
			}
		};
		
		if(table[P_FLOW_MAPPING_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowInternal_MappingStart() {
		table[P_FLOW_INTERNAL_MAPPING_START] = new Production() {
			public Event produce () {
				tokenizer.getNextToken();
				return new MappingStartEvent(null, null, true, true);
			}
		};
		
		if(table[P_FLOW_INTERNAL_MAPPING_START] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowInternal_Content() {
		table[P_FLOW_INTERNAL_CONTENT] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == VALUE || type == FLOW_ENTRY || type == FLOW_SEQUENCE_END)
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				else
					parseStack.add(0, table[P_FLOW_NODE]);
				return null;
			}
		};
		
		if(table[P_FLOW_INTERNAL_CONTENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowInternal_Value() {
		table[P_FLOW_INTERNAL_VALUE] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == VALUE) {
					tokenizer.getNextToken();
					if (tokenizer.peekNextTokenType() == FLOW_ENTRY || tokenizer.peekNextTokenType() == FLOW_SEQUENCE_END)
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					else
						parseStack.add(0, table[P_FLOW_NODE]);
				} else
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				return null;
			}
		};
		
		if(table[P_FLOW_INTERNAL_VALUE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowInternal_MappingEnd() {
		table[P_FLOW_INTERNAL_MAPPING_END] = new Production() {
			public Event produce () {
				return Event.MAPPING_END;
			}
		};
		
		if(table[P_FLOW_INTERNAL_MAPPING_END] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pFlowEntry_Maker() {
		table[P_FLOW_ENTRY_MARKER] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == FLOW_ENTRY) tokenizer.getNextToken();
				return null;
			}
		};
		
		if(table[P_FLOW_ENTRY_MARKER] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pFlowNode() {
		table[P_FLOW_NODE] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == ALIAS)
					parseStack.add(0, table[P_ALIAS]);
				else {
					parseStack.add(0, table[P_PROPERTIES_END]);
					parseStack.add(0, table[P_FLOW_CONTENT]);
					parseStack.add(0, table[P_PROPERTIES]);
				}
				return null;
			}
		};
		
		if(table[P_FLOW_NODE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}

	
	public int addTable_pFlowMapping_InternalContent() {
		table[P_FLOW_MAPPING_INTERNAL_CONTENT] = new Production() {
			public Event produce () {
				TokenType type = tokenizer.peekNextTokenType();
				if (type == VALUE || type == FLOW_ENTRY || type == FLOW_MAPPING_END)
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				else {
					tokenizer.getNextToken();
					parseStack.add(0, table[P_FLOW_NODE]);
				}
				return null;
			}
		};
		
		if(table[P_FLOW_MAPPING_INTERNAL_CONTENT] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	
	public int addTable_pFlowMapping_InternalValue() {
		table[P_FLOW_MAPPING_INTERNAL_VALUE] = new Production() {
			public Event produce () {
				if (tokenizer.peekNextTokenType() == VALUE) {
					tokenizer.getNextToken();
					if (tokenizer.peekNextTokenType() == FLOW_ENTRY || tokenizer.peekNextTokenType() == FLOW_MAPPING_END)
						parseStack.add(0, table[P_EMPTY_SCALAR]);
					else
						parseStack.add(0, table[P_FLOW_NODE]);
				} else
					parseStack.add(0, table[P_EMPTY_SCALAR]);
				return null;
			}
		};
		
		if(table[P_FLOW_MAPPING_INTERNAL_VALUE] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pAlias() {
		table[P_ALIAS] = new Production() {
			public Event produce () {
				AliasToken token = (AliasToken)tokenizer.getNextToken();
				return new AliasEvent(token.getInstanceName());
			}
		};
		
		if(table[P_ALIAS] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	
	public int addTable_pEmptyScalar() {
		table[P_EMPTY_SCALAR] = new Production() {
			public Event produce () {
				return new ScalarEvent(null, null, new boolean[] {true, false}, null, (char)0);
			}
		};
		if(table[P_EMPTY_SCALAR] == null)
			return P_ALLOCATION_FAIL;
		
		return P_ALLOCATION_SUCESS;
	}
	


	private void initProductionTable () {
		addTable_pStream();
		addTable_pStreamStart();
		addTable_pStreamEnd();
		
		addTable_pImplicitDoucment();
		addTable_pExplicitDoucment();
		addTable_pDocumentStart();
		addTable_pDocument_StartImplict(); 
		addTable_pDoucmentEnd();
		
		addTable_pBlockNode();
		addTable_pBlockContent();
		
		addTable_pProperties();
		addTable_pPropertiesEnd();
		
		addTable_pFlowContent();
		
		addTable_pBlockSequence(); 
		addTable_pBlockMapping(); 
		
		addTable_pFlowSequence();
		addTable_pFlowMapping(); 
		
		addTable_pScalar();
		addTable_pBlockSequecne_Entry();
		addTable_pBlockMapping_Entry();
		
		addTable_pBlockMapping_EntryValue();
		addTable_pBlockNode_Or_IndentlessSequence();
		addTable_pBlockSequecne_Start();
		addTable_pBlockSequecne_End();
		addTable_pBlockMapping_Start();
		addTable_pBlockMapping_End();
		
		addTable_pIndentless_BlockSequence(); 
		addTable_pIndentless_BlockSequence_Start();
		addTable_pIndentless_BlockSequence_Entry();
		addTable_pIndentless_BlockSequence_End();
		
		addTable_pFlowSequence_Start();
		addTable_pFlowSequence_Entry();
		addTable_pFlowSequence_End();
		addTable_pFlowMapping_Start();
		addTable_pFlowMapping_Entry();
		addTable_pFlowMapping_End();
		addTable_pFlowInternal_MappingStart();
		addTable_pFlowInternal_Content();
		addTable_pFlowInternal_Value();
		addTable_pFlowInternal_MappingEnd();
		addTable_pFlowEntry_Maker();
		addTable_pFlowNode();
		addTable_pFlowMapping_InternalContent();
		addTable_pFlowMapping_InternalValue();
		
		addTable_pAlias(); 
		addTable_pEmptyScalar();
	}

	DocumentStartEvent processDirectives (boolean explicit) {
		documentVersion = null;
		while (tokenizer.peekNextTokenType() == DIRECTIVE) {
			DirectiveToken token = (DirectiveToken)tokenizer.getNextToken();
			if (token.getDirective().equals("YAML")) {
				if (documentVersion != null) throw new ParserException("Duplicate YAML directive.");
				documentVersion = new Version(token.getValue());
				if (documentVersion.major != 1)
					throw new ParserException("Unsupported YAML version (1.x is required): " + documentVersion);
			} else if (token.getDirective().equals("TAG")) {
				String[] values = token.getValue().split(" ");
				String handle = values[0];
				String prefix = values[1];
				if (tagHandles.containsKey(handle)) throw new ParserException("Duplicate tag directive: " + handle);
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

	public class ParserException extends RuntimeException {
		public ParserException (String message) {
			super("Line " + tokenizer.getLineNumber() + ", column " + tokenizer.getColumn() + ": " + message);
		}
	}

	public static void main (String[] args) throws Exception {
		Parser parser = new Parser(new FileReader("test/test.yml"));
		while (true) {
			Event event = parser.getNextEvent();
			if (event == null) break;
			System.out.println(event);
		}
	}
}

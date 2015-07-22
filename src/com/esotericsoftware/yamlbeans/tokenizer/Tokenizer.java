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

package com.esotericsoftware.yamlbeans.tokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interprets a YAML document as a stream of tokens.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a> */
public class Tokenizer {
	private final static String LINEBR = "\n\u0085\u2028\u2029";
	private final static String NULL_BL_LINEBR = "\0 \r\n\u0085";
	private final static String NULL_BL_T_LINEBR = "\0 \t\r\n\u0085";
	private final static String NULL_OR_OTHER = NULL_BL_T_LINEBR;
	private final static String NULL_OR_LINEBR = "\0\r\n\u0085";
	private final static String FULL_LINEBR = "\r\n\u0085";
	private final static String BLANK_OR_LINEBR = " \r\n\u0085";
	private final static String S4 = "\0 \t\r\n\u0028[]{}";
	private final static String ALPHA = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
	private final static String STRANGE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][-';/?:@&=+$,.!~*()%";
	private final static String RN = "\r\n";
	private final static String BLANK_T = " \t";
	private final static String SPACES_AND_STUFF = "'\"\\\0 \t\r\n\u0085";
	private final static String DOUBLE_ESC = "\"\\";
	private final static String NON_ALPHA_OR_NUM = "\0 \t\r\n\u0085?:,]}%@`";
	private final static Pattern NON_PRINTABLE = Pattern.compile("[^\u0009\n\r\u0020-\u007E\u0085\u00A0-\u00FF]");
	private final static Pattern NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");
	private final static Pattern NON_ALPHA = Pattern.compile("[^-0-9A-Za-z_]");
	private final static Pattern R_FLOWZERO = Pattern.compile("[\0 \t\r\n\u0085]|(:[\0 \t\r\n\u0028])");
	private final static Pattern R_FLOWNONZERO = Pattern.compile("[\0 \t\r\n\u0085\\[\\]{},:?]");
	private final static Pattern END_OR_START = Pattern.compile("^(---|\\.\\.\\.)[\0 \t\r\n\u0085]$");
	private final static Pattern ENDING = Pattern.compile("^---[\0 \t\r\n\u0085]$");
	private final static Pattern START = Pattern.compile("^\\.\\.\\.[\0 \t\r\n\u0085]$");
	private final static Pattern BEG = Pattern
		.compile("^([^\0 \t\r\n\u0085\\-?:,\\[\\]{}#&*!|>'\"%@]|([\\-?:][^\0 \t\r\n\u0085]))");

	private final static Map<Character, String> ESCAPE_REPLACEMENTS = new HashMap();
	private final static Map<Character, Integer> ESCAPE_CODES = new HashMap();

	static {
		ESCAPE_REPLACEMENTS.put('0', "\0");
		ESCAPE_REPLACEMENTS.put('a', "\u0007");
		ESCAPE_REPLACEMENTS.put('b', "\u0008");
		ESCAPE_REPLACEMENTS.put('t', "\u0009");
		ESCAPE_REPLACEMENTS.put('\t', "\u0009");
		ESCAPE_REPLACEMENTS.put('n', "\n");
		ESCAPE_REPLACEMENTS.put('v', "\u000B");
		ESCAPE_REPLACEMENTS.put('f', "\u000C");
		ESCAPE_REPLACEMENTS.put('r', "\r");
		ESCAPE_REPLACEMENTS.put('e', "\u001B");
		ESCAPE_REPLACEMENTS.put(' ', "\u0020");
		ESCAPE_REPLACEMENTS.put('"', "\"");
		ESCAPE_REPLACEMENTS.put('\\', "\\");
		ESCAPE_REPLACEMENTS.put('N', "\u0085");
		ESCAPE_REPLACEMENTS.put('_', "\u00A0");
		ESCAPE_REPLACEMENTS.put('L', "\u2028");
		ESCAPE_REPLACEMENTS.put('P', "\u2029");

		ESCAPE_CODES.put('x', 2);
		ESCAPE_CODES.put('u', 4);
		ESCAPE_CODES.put('U', 8);
	}

	private boolean done = false;
	private int flowLevel = 0;
	private int tokensTaken = 0;
	private int indent = -1;
	private boolean allowSimpleKey = true;
	private boolean eof;
	private int lineNumber = 0;
	private int column = 0;
	private int pointer = 0;
	private final StringBuilder buffer;
	private final Reader reader;
	private final List<Token> tokens = new LinkedList();
	private final List<Integer> indents = new LinkedList();
	private final Map<Integer, SimpleKey> possibleSimpleKeys = new HashMap();
	private boolean docStart = false;

	public Tokenizer (Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null.");
		if (!(reader instanceof BufferedReader)) reader = new BufferedReader(reader);
		this.reader = reader;
		buffer = new StringBuilder();
		eof = false;
		fetchStreamStart();
	}

	public Tokenizer (String yaml) {
		this(new StringReader(yaml));
	}

	public Token peekNextToken () throws TokenizerException {
		while (needMoreTokens())
			fetchMoreTokens();
		return tokens.isEmpty() ? null : tokens.get(0);
	}

	public TokenType peekNextTokenType () throws TokenizerException {
		Token token = peekNextToken();
		if (token == null) return null;
		return token.type;
	}

	public Token getNextToken () throws TokenizerException {
		while (needMoreTokens())
			fetchMoreTokens();
		if (!tokens.isEmpty()) {
			tokensTaken++;
			Token token = tokens.remove(0);
			return token;
		}
		return null;
	}

	public Iterator iterator () {
		return new Iterator() {
			public boolean hasNext () {
				return null != peekNextToken();
			}

			public Object next () {
				return getNextToken();
			}

			public void remove () {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int getLineNumber () {
		return lineNumber;
	}

	public int getColumn () {
		return column;
	}

	public void close () throws IOException {
		reader.close();
	}

	private char peek () {
		if (pointer + 1 > buffer.length()) update(1);
		return buffer.charAt(pointer);
	}

	private char peek (int index) {
		if (pointer + index + 1 > buffer.length()) update(index + 1);
		return buffer.charAt(pointer + index);
	}

	private String prefix (int length) {
		if (pointer + length >= buffer.length()) update(length);
		if (pointer + length > buffer.length()) return buffer.substring(pointer, buffer.length());
		return buffer.substring(pointer, pointer + length);
	}

	private String prefixForward (int length) {
		if (pointer + length + 1 >= buffer.length()) update(length + 1);
		String buff = null;
		if (pointer + length > buffer.length())
			buff = buffer.substring(pointer, buffer.length());
		else
			buff = buffer.substring(pointer, pointer + length);
		char ch = 0;
		for (int i = 0, j = buff.length(); i < j; i++) {
			ch = buff.charAt(i);
			pointer++;
			if (LINEBR.indexOf(ch) != -1 || ch == '\r' && buff.charAt(i + 1) != '\n') {
				column = 0;
				lineNumber++;
			} else if (ch != '\uFEFF') column++;
		}
		return buff;
	}

	private void forward () {
		if (pointer + 2 >= buffer.length()) update(2);
		char ch1 = buffer.charAt(pointer);
		pointer++;
		if (ch1 == '\n' || ch1 == '\u0085' || ch1 == '\r' && buffer.charAt(pointer) != '\n') {
			column = 0;
			lineNumber++;
		} else
			column++;
	}

	private void forward (int length) {
		if (pointer + length + 1 >= buffer.length()) update(length + 1);
		char ch = 0;
		for (int i = 0; i < length; i++) {
			ch = buffer.charAt(pointer);
			pointer++;
			if (LINEBR.indexOf(ch) != -1 || ch == '\r' && buffer.charAt(pointer) != '\n') {
				column = 0;
				lineNumber++;
			} else if (ch != '\uFEFF') column++;
		}
	}

	private void update (int length) {
		buffer.delete(0, pointer);
		pointer = 0;
		while (buffer.length() < length) {
			String rawData = "";
			if (!eof) {
				char[] data = new char[1024];
				int converted = -2;
				try {
					converted = reader.read(data);
				} catch (IOException ioe) {
					throw new TokenizerException("Error reading from stream.", ioe);
				}
				if (converted == -1)
					eof = true;
				else
					rawData = String.valueOf(data, 0, converted);
			}
			buffer.append(rawData);
			if (eof) {
				buffer.append('\0');
				break;
			}
		}
	}

	private boolean needMoreTokens () {
		if (done) return false;
		return tokens.isEmpty() || nextPossibleSimpleKey() == tokensTaken;
	}

	private Token fetchMoreTokens () {
		scanToNextToken();
		unwindIndent(column);
		char ch = peek();
		boolean colz = column == 0;
		switch (ch) {
		case '\0':
			return fetchStreamEnd();
		case '\'':
			return fetchSingle();
		case '"':
			return fetchDouble();
		case '?':
			if (flowLevel != 0 || NULL_OR_OTHER.indexOf(peek(1)) != -1) return fetchKey();
			break;
		case ':':
			if (flowLevel != 0 || NULL_OR_OTHER.indexOf(peek(1)) != -1) return fetchValue();
			break;
		case '%':
			if (colz) return fetchDirective();
			break;
		case '-':
			if ((colz || docStart) && ENDING.matcher(prefix(4)).matches())
				return fetchDocumentStart();
			else if (NULL_OR_OTHER.indexOf(peek(1)) != -1) return fetchBlockEntry();
			break;
		case '.':
			if (colz && START.matcher(prefix(4)).matches()) return fetchDocumentEnd();
			break;
		case '[':
			return fetchFlowSequenceStart();
		case '{':
			return fetchFlowMappingStart();
		case ']':
			return fetchFlowSequenceEnd();
		case '}':
			return fetchFlowMappingEnd();
		case ',':
			return fetchFlowEntry();
		case '*':
			return fetchAlias();
		case '&':
			return fetchAnchor();
		case '!':
			return fetchTag();
		case '|':
			if (flowLevel == 0) return fetchLiteral();
			break;
		case '>':
			if (flowLevel == 0) return fetchFolded();
			break;
		}
		if (BEG.matcher(prefix(2)).find()) return fetchPlain();
		if (ch == '\t') throw new TokenizerException("Tabs cannot be used for indentation.");
		throw new TokenizerException("While scanning for the next token, a character that cannot begin a token was found: "
			+ ch(ch));
	}

	private int nextPossibleSimpleKey () {
		for (Iterator iter = possibleSimpleKeys.values().iterator(); iter.hasNext();) {
			SimpleKey key = (SimpleKey)iter.next();
			if (key.tokenNumber > 0) return key.tokenNumber;
		}
		return -1;
	}

	private void savePossibleSimpleKey () {
		if (allowSimpleKey) possibleSimpleKeys.put(flowLevel, new SimpleKey(tokensTaken + tokens.size(), column));
	}

	private void unwindIndent (int col) {
		if (flowLevel != 0) return;

		while (indent > col) {
			indent = indents.remove(0);
			tokens.add(Token.BLOCK_END);
		}
	}

	private boolean addIndent (int col) {
		if (indent < col) {
			indents.add(0, indent);
			indent = col;
			return true;
		}
		return false;
	}

	private Token fetchStreamStart () {
		docStart = true;
		tokens.add(Token.STREAM_START);
		return Token.STREAM_START;
	}

	private Token fetchStreamEnd () {
		unwindIndent(-1);
		allowSimpleKey = false;
		possibleSimpleKeys.clear();
		tokens.add(Token.STREAM_END);
		done = true;
		return Token.STREAM_END;
	}

	private Token fetchDirective () {
		unwindIndent(-1);
		allowSimpleKey = false;
		Token tok = scanDirective();
		tokens.add(tok);
		return tok;
	}

	private Token fetchDocumentStart () {
		docStart = false;
		return fetchDocumentIndicator(Token.DOCUMENT_START);
	}

	private Token fetchDocumentEnd () {
		return fetchDocumentIndicator(Token.DOCUMENT_END);
	}

	private Token fetchDocumentIndicator (Token tok) {
		unwindIndent(-1);
		allowSimpleKey = false;
		forward(3);
		tokens.add(tok);
		return tok;
	}

	private Token fetchFlowSequenceStart () {
		return fetchFlowCollectionStart(Token.FLOW_SEQUENCE_START);
	}

	private Token fetchFlowMappingStart () {
		return fetchFlowCollectionStart(Token.FLOW_MAPPING_START);
	}

	private Token fetchFlowCollectionStart (Token tok) {
		savePossibleSimpleKey();
		flowLevel++;
		allowSimpleKey = true;
		forward(1);
		tokens.add(tok);
		return tok;
	}

	private Token fetchFlowSequenceEnd () {
		return fetchFlowCollectionEnd(Token.FLOW_SEQUENCE_END);
	}

	private Token fetchFlowMappingEnd () {
		return fetchFlowCollectionEnd(Token.FLOW_MAPPING_END);
	}

	private Token fetchFlowCollectionEnd (Token tok) {
		flowLevel--;
		allowSimpleKey = false;
		forward(1);
		tokens.add(tok);
		return tok;
	}

	private Token fetchFlowEntry () {
		allowSimpleKey = true;
		forward(1);
		tokens.add(Token.FLOW_ENTRY);
		return Token.FLOW_ENTRY;
	}

	private Token fetchBlockEntry () {
		if (flowLevel == 0) {
			if (!allowSimpleKey) throw new TokenizerException("Found a sequence entry where it is not allowed.");
			if (addIndent(column)) tokens.add(Token.BLOCK_SEQUENCE_START);
		}
		allowSimpleKey = true;
		forward();
		tokens.add(Token.BLOCK_ENTRY);
		return Token.BLOCK_ENTRY;
	}

	private Token fetchKey () {
		if (flowLevel == 0) {
			if (!allowSimpleKey) throw new TokenizerException("Found a mapping key where it is not allowed.");
			if (addIndent(column)) tokens.add(Token.BLOCK_MAPPING_START);
		}
		allowSimpleKey = flowLevel == 0;
		forward();
		tokens.add(Token.KEY);
		return Token.KEY;
	}

	private Token fetchValue () {
		SimpleKey key = possibleSimpleKeys.get(flowLevel);
		if (null == key) {
			if (flowLevel == 0 && !allowSimpleKey) throw new TokenizerException("Found a mapping value where it is not allowed.");
		} else {
			possibleSimpleKeys.remove(flowLevel);
			tokens.add(key.tokenNumber - tokensTaken, Token.KEY);
			if (flowLevel == 0 && addIndent(key.column)) tokens.add(key.tokenNumber - tokensTaken, Token.BLOCK_MAPPING_START);
			allowSimpleKey = false;
		}
		forward();
		tokens.add(Token.VALUE);
		return Token.VALUE;
	}

	private Token fetchAlias () {
		savePossibleSimpleKey();
		allowSimpleKey = false;
		Token tok = scanAnchor(new AliasToken());
		tokens.add(tok);
		return tok;
	}

	private Token fetchAnchor () {
		savePossibleSimpleKey();
		allowSimpleKey = false;
		Token tok = scanAnchor(new AnchorToken());
		tokens.add(tok);
		return tok;
	}

	private Token fetchTag () {
		savePossibleSimpleKey();
		allowSimpleKey = false;
		Token tok = scanTag();
		tokens.add(tok);
		return tok;
	}

	private Token fetchLiteral () {
		return fetchBlockScalar('|');
	}

	private Token fetchFolded () {
		return fetchBlockScalar('>');
	}

	private Token fetchBlockScalar (char style) {
		allowSimpleKey = true;
		Token tok = scanBlockScalar(style);
		tokens.add(tok);
		return tok;
	}

	private Token fetchSingle () {
		return fetchFlowScalar('\'');
	}

	private Token fetchDouble () {
		return fetchFlowScalar('"');
	}

	private Token fetchFlowScalar (char style) {
		savePossibleSimpleKey();
		allowSimpleKey = false;
		Token tok = scanFlowScalar(style);
		tokens.add(tok);
		return tok;
	}

	private Token fetchPlain () {
		savePossibleSimpleKey();
		allowSimpleKey = false;
		Token tok = scanPlain();
		tokens.add(tok);
		return tok;
	}

	private void scanToNextToken () {
		for (;;) {
			while (peek() == ' ')
				forward();
			if (peek() == '#') while (NULL_OR_LINEBR.indexOf(peek()) == -1)
				forward();
			if (scanLineBreak().length() != 0) {
				if (flowLevel == 0) allowSimpleKey = true;
			} else
				break;
		}
	}

	private Token scanDirective () {
		forward();
		String name = scanDirectiveName();
		String value = null;
		if (name.equals("YAML"))
			value = scanYamlDirectiveValue();
		else if (name.equals("TAG"))
			value = scanTagDirectiveValue();
		else {
			StringBuilder buffer = new StringBuilder();
			while (true) {
				char ch = peek();
				if (NULL_OR_LINEBR.indexOf(ch) != -1) break;
				buffer.append(ch);
				forward();
			}
			value = buffer.toString().trim();
		}
		scanDirectiveIgnoredLine();
		return new DirectiveToken(name, value);
	}

	private String scanDirectiveName () {
		int length = 0;
		char ch = peek(length);
		boolean zlen = true;
		while (ALPHA.indexOf(ch) != -1) {
			zlen = false;
			length++;
			ch = peek(length);
		}
		if (zlen)
			throw new TokenizerException("While scanning for a directive name, expected an alpha or numeric character but found: "
				+ ch(ch));
		String value = prefixForward(length);
		// forward(length);
		if (NULL_BL_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning for a directive name, expected an alpha or numeric character but found: "
				+ ch(ch));
		return value;
	}

	private String scanYamlDirectiveValue () {
		while (peek() == ' ')
			forward();
		String major = scanYamlDirectiveNumber();
		if (peek() != '.')
			throw new TokenizerException("While scanning for a directive value, expected a digit or '.' but found: " + ch(peek()));
		forward();
		String minor = scanYamlDirectiveNumber();
		if (NULL_BL_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning for a directive value, expected a digit or '.' but found: " + ch(peek()));
		return major + "." + minor;
	}

	private String scanYamlDirectiveNumber () {
		char ch = peek();
		if (!Character.isDigit(ch))
			throw new TokenizerException("While scanning for a directive number, expected a digit but found: " + ch(ch));
		int length = 0;
		while (Character.isDigit(peek(length)))
			length++;
		String value = prefixForward(length);
		// forward(length);
		return value;
	}

	private String scanTagDirectiveValue () {
		while (peek() == ' ')
			forward();
		String handle = scanTagDirectiveHandle();
		while (peek() == ' ')
			forward();
		String prefix = scanTagDirectivePrefix();
		return handle + " " + prefix;
	}

	private String scanTagDirectiveHandle () {
		String value = scanTagHandle("directive");
		if (peek() != ' ')
			throw new TokenizerException("While scanning for a directive tag handle, expected ' ' but found: " + ch(peek()));
		return value;
	}

	private String scanTagDirectivePrefix () {
		String value = scanTagUri("directive");
		if (NULL_BL_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning for a directive tag prefix, expected ' ' but found: " + ch(peek()));
		return value;
	}

	private String scanDirectiveIgnoredLine () {
		while (peek() == ' ')
			forward();
		if (peek() == '"') while (NULL_OR_LINEBR.indexOf(peek()) == -1)
			forward();
		char ch = peek();
		if (NULL_OR_LINEBR.indexOf(ch) == -1)
			throw new TokenizerException("While scanning a directive, expected a comment or line break but found: " + ch(peek()));
		return scanLineBreak();
	}

	private Token scanAnchor (Token tok) {
		char indicator = peek();
		String name = indicator == '*' ? "alias" : "anchor";
		forward();
		int length = 0;
		int chunk_size = 16;
		Matcher m = null;
		for (;;) {
			String chunk = prefix(chunk_size);
			if ((m = NON_ALPHA.matcher(chunk)).find()) break;
			chunk_size += 16;
		}
		length = m.start();
		if (length == 0)
			throw new TokenizerException("While scanning an " + name + ", a non-alpha, non-numeric character was found.");
		String value = prefixForward(length);
		// forward(length);
		if (NON_ALPHA_OR_NUM.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning an " + name + ", expected an alpha or numeric character but found: "
				+ ch(peek()));
		if (tok instanceof AnchorToken)
			((AnchorToken)tok).setInstanceName(value);
		else
			((AliasToken)tok).setInstanceName(value);
		return tok;
	}

	private Token scanTag () {
		char ch = peek(1);
		String handle = null;
		String suffix = null;
		if (ch == '<') {
			forward(2);
			suffix = scanTagUri("tag");
			if (peek() != '>') throw new TokenizerException("While scanning a tag, expected '>' but found: " + ch(peek()));
			forward();
		} else if (NULL_BL_T_LINEBR.indexOf(ch) != -1) {
			suffix = "!";
			forward();
		} else {
			int length = 1;
			boolean useHandle = false;
			while (NULL_BL_T_LINEBR.indexOf(ch) == -1) {
				if (ch == '!') {
					useHandle = true;
					break;
				}
				length++;
				ch = peek(length);
			}
			handle = "!";
			if (useHandle)
				handle = scanTagHandle("tag");
			else {
				handle = "!";
				forward();
			}
			suffix = scanTagUri("tag");
		}
		if (NULL_BL_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning a tag, expected ' ' but found: " + ch(peek()));
		return new TagToken(handle, suffix);
	}

	private Token scanBlockScalar (char style) {
		boolean folded = style == '>';
		StringBuilder chunks = new StringBuilder();
		forward();
		Object[] chompi = scanBlockScalarIndicators();
		boolean chomping = ((Boolean)chompi[0]).booleanValue();
		int increment = ((Integer)chompi[1]).intValue();
		scanBlockScalarIgnoredLine();
		int minIndent = indent + 1;
		if (minIndent < 1) minIndent = 1;
		String breaks = null;
		int maxIndent = 0;
		int ind = 0;
		if (increment == -1) {
			Object[] brme = scanBlockScalarIndentation();
			breaks = (String)brme[0];
			maxIndent = ((Integer)brme[1]).intValue();
			if (minIndent > maxIndent)
				ind = minIndent;
			else
				ind = maxIndent;
		} else {
			ind = minIndent + increment - 1;
			breaks = scanBlockScalarBreaks(ind);
		}

		String lineBreak = "";
		while (column == ind && peek() != '\0') {
			chunks.append(breaks);
			boolean leadingNonSpace = BLANK_T.indexOf(peek()) == -1;
			int length = 0;
			while (NULL_OR_LINEBR.indexOf(peek(length)) == -1)
				length++;
			chunks.append(prefixForward(length));
			// forward(length);
			lineBreak = scanLineBreak();
			breaks = scanBlockScalarBreaks(ind);
			if (column == ind && peek() != '\0') {
				if (folded && lineBreak.equals("\n") && leadingNonSpace && BLANK_T.indexOf(peek()) == -1) {
					if (breaks.length() == 0) chunks.append(" ");
				} else
					chunks.append(lineBreak);
			} else
				break;
		}

		if (chomping) {
			chunks.append(lineBreak);
			chunks.append(breaks);
		}

		return new ScalarToken(chunks.toString(), false, style);
	}

	private Object[] scanBlockScalarIndicators () {
		boolean chomping = false;
		int increment = -1;
		char ch = peek();
		if (ch == '-' || ch == '+') {
			chomping = ch == '+';
			forward();
			ch = peek();
			if (Character.isDigit(ch)) {
				increment = Integer.parseInt(("" + ch));
				if (increment == 0)
					throw new TokenizerException(
						"While scanning a black scaler, expected indentation indicator between 1 and 9 but found: 0");
				forward();
			}
		} else if (Character.isDigit(ch)) {
			increment = Integer.parseInt(("" + ch));
			if (increment == 0)
				throw new TokenizerException(
					"While scanning a black scaler, expected indentation indicator between 1 and 9 but found: 0");
			forward();
			ch = peek();
			if (ch == '-' || ch == '+') {
				chomping = ch == '+';
				forward();
			}
		}
		if (NULL_BL_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning a block scalar, expected chomping or indentation indicators but found: "
				+ ch(peek()));
		return new Object[] {Boolean.valueOf(chomping), increment};
	}

	private String scanBlockScalarIgnoredLine () {
		while (peek() == ' ')
			forward();
		if (peek() == '#') while (NULL_OR_LINEBR.indexOf(peek()) == -1)
			forward();
		if (NULL_OR_LINEBR.indexOf(peek()) == -1)
			throw new TokenizerException("While scanning a block scalar, expected a comment or line break but found: " + ch(peek()));
		return scanLineBreak();
	}

	private Object[] scanBlockScalarIndentation () {
		StringBuilder chunks = new StringBuilder();
		int maxIndent = 0;
		while (BLANK_OR_LINEBR.indexOf(peek()) != -1)
			if (peek() != ' ')
				chunks.append(scanLineBreak());
			else {
				forward();
				if (column > maxIndent) maxIndent = column;
			}
		return new Object[] {chunks.toString(), maxIndent};
	}

	private String scanBlockScalarBreaks (int indent) {
		StringBuilder chunks = new StringBuilder();
		while (column < indent && peek() == ' ')
			forward();
		while (FULL_LINEBR.indexOf(peek()) != -1) {
			chunks.append(scanLineBreak());
			while (column < indent && peek() == ' ')
				forward();
		}
		return chunks.toString();
	}

	private Token scanFlowScalar (char style) {
		boolean dbl = style == '"';
		StringBuilder chunks = new StringBuilder();
		char quote = peek();
		forward();
		chunks.append(scanFlowScalarNonSpaces(dbl));
		while (peek() != quote) {
			chunks.append(scanFlowScalarSpaces());
			chunks.append(scanFlowScalarNonSpaces(dbl));
		}
		forward();
		return new ScalarToken(chunks.toString(), false, style);
	}

	private String scanFlowScalarNonSpaces (boolean dbl) {
		StringBuilder chunks = new StringBuilder();
		for (;;) {
			int length = 0;
			while (SPACES_AND_STUFF.indexOf(peek(length)) == -1)
				length++;
			if (length != 0) chunks.append(prefixForward(length));
			// forward(length);
			char ch = peek();
			if (!dbl && ch == '\'' && peek(1) == '\'') {
				chunks.append("'");
				forward(2);
			} else if (dbl && ch == '\'' || !dbl && DOUBLE_ESC.indexOf(ch) != -1) {
				chunks.append(ch);
				forward();
			} else if (dbl && ch == '\\') {
				forward();
				ch = peek();
				if (ESCAPE_REPLACEMENTS.containsKey(ch)) {
					chunks.append(ESCAPE_REPLACEMENTS.get(ch));
					forward();
				} else if (ESCAPE_CODES.containsKey(ch)) {
					length = ESCAPE_CODES.get(ch);
					forward();
					String val = prefix(length);
					if (NOT_HEXA.matcher(val).find())
						throw new TokenizerException("While scanning a double quoted scalar, expected an escape sequence of " + length
							+ " hexadecimal numbers but found: " + ch(peek()));
					chunks.append(Character.toChars(Integer.parseInt(val, 16)));
					forward(length);
				} else if (FULL_LINEBR.indexOf(ch) != -1) {
					scanLineBreak();
					chunks.append(scanFlowScalarBreaks());
				} else
					throw new TokenizerException("While scanning a double quoted scalar, found unknown escape character: " + ch(ch));
			} else
				return chunks.toString();
		}
	}

	private String scanFlowScalarSpaces () {
		StringBuilder chunks = new StringBuilder();
		int length = 0;
		while (BLANK_T.indexOf(peek(length)) != -1)
			length++;
		String whitespaces = prefixForward(length);
		// forward(length);
		char ch = peek();
		if (ch == '\0')
			throw new TokenizerException("While scanning a quoted scalar, found unexpected end of stream.");
		else if (FULL_LINEBR.indexOf(ch) != -1) {
			String lineBreak = scanLineBreak();
			String breaks = scanFlowScalarBreaks();
			if (!lineBreak.equals("\n"))
				chunks.append(lineBreak);
			else if (breaks.length() == 0) chunks.append(" ");
			chunks.append(breaks);
		} else
			chunks.append(whitespaces);
		return chunks.toString();
	}

	private String scanFlowScalarBreaks () {
		StringBuilder chunks = new StringBuilder();
		String pre = null;
		for (;;) {
			pre = prefix(3);
			if ((pre.equals("---") || pre.equals("...")) && NULL_BL_T_LINEBR.indexOf(peek(3)) != -1)
				throw new TokenizerException("While scanning a quoted scalar, found unexpected document separator.");
			while (BLANK_T.indexOf(peek()) != -1)
				forward();
			if (FULL_LINEBR.indexOf(peek()) != -1)
				chunks.append(scanLineBreak());
			else
				return chunks.toString();
		}
	}

	private Token scanPlain () {
		/*
		 * See the specification for details. We add an additional restriction for the flow context: plain scalars in the flow
		 * context cannot contain ',', ':' and '?'. We also keep track of the `allow_simple_key` flag here. Indentation rules are
		 * loosed for the flow context.
		 */
		StringBuilder chunks = new StringBuilder();
		int ind = indent + 1;
		String spaces = "";
		boolean f_nzero = true;
		Pattern r_check = R_FLOWNONZERO;
		if (flowLevel == 0) {
			f_nzero = false;
			r_check = R_FLOWZERO;
		}
		while (peek() != '#') {
			int length = 0;
			int chunkSize = 32;
			Matcher m = null;
			while (!(m = r_check.matcher(prefix(chunkSize))).find())
				chunkSize += 32;
			length = m.start();
			char ch = peek(length);
			if (f_nzero && ch == ':' && S4.indexOf(peek(length + 1)) == -1) {
				forward(length);
				throw new TokenizerException(
					"While scanning a plain scalar, found unexpected ':'. See: http://pyyaml.org/wiki/YAMLColonInFlowContext");
			}
			if (length == 0) break;
			allowSimpleKey = false;
			chunks.append(spaces);
			chunks.append(prefixForward(length));
			// forward(length);
			spaces = scanPlainSpaces();
			if (spaces == null || flowLevel == 0 && column < ind) break;
		}
		return new ScalarToken(chunks.toString(), true);
	}

	private String scanPlainSpaces () {
		StringBuilder chunks = new StringBuilder();
		int length = 0;
		while (peek(length) == ' ')
			length++;
		String whitespaces = prefixForward(length);
		// forward(length);
		char ch = peek();
		if (FULL_LINEBR.indexOf(ch) != -1) {
			String lineBreak = scanLineBreak();
			allowSimpleKey = true;
			if (END_OR_START.matcher(prefix(4)).matches()) return "";
			StringBuilder breaks = new StringBuilder();
			while (BLANK_OR_LINEBR.indexOf(peek()) != -1)
				if (' ' == peek())
					forward();
				else {
					breaks.append(scanLineBreak());
					if (END_OR_START.matcher(prefix(4)).matches()) return "";
				}
			if (!lineBreak.equals("\n"))
				chunks.append(lineBreak);
			else if (breaks.length() == 0) chunks.append(" ");
			chunks.append(breaks);
		} else
			chunks.append(whitespaces);
		return chunks.toString();
	}

	private String scanTagHandle (String name) {
		char ch = peek();
		if (ch != '!') throw new TokenizerException("While scanning a " + name + ", expected '!' but found: " + ch(ch));
		int length = 1;
		ch = peek(length);
		if (ch != ' ') {
			while (ALPHA.indexOf(ch) != -1) {
				length++;
				ch = peek(length);
			}
			if ('!' != ch) {
				forward(length);
				throw new TokenizerException("While scanning a " + name + ", expected '!' but found: " + ch(ch));
			}
			length++;
		}
		String value = prefixForward(length);
		// forward(length);
		return value;
	}

	private String scanTagUri (String name) {
		StringBuilder chunks = new StringBuilder();
		int length = 0;
		char ch = peek(length);
		while (STRANGE_CHAR.indexOf(ch) != -1) {
			if ('%' == ch) {
				chunks.append(prefixForward(length));
				// forward(length);
				length = 0;
				chunks.append(scanUriEscapes(name));
			} else
				length++;
			ch = peek(length);
		}
		if (length != 0) chunks.append(prefixForward(length));
		// forward(length);

		if (chunks.length() == 0)
			throw new TokenizerException("While scanning a " + name + ", expected a URI but found: " + ch(ch));
		return chunks.toString();
	}

	private String scanUriEscapes (String name) {
		StringBuilder bytes = new StringBuilder();
		while (peek() == '%') {
			forward();
			try {
				bytes.append(Integer.parseInt(prefix(2), 16));
			} catch (NumberFormatException nfe) {
				throw new TokenizerException("While scanning a " + name
					+ ", expected a URI escape sequence of 2 hexadecimal numbers but found: " + ch(peek(1)) + " and " + ch(peek(2)));
			}
			forward(2);
		}
		return bytes.toString();
	}

	private String scanLineBreak () {
		// Transforms:
		// '\r\n' : '\n'
		// '\r' : '\n'
		// '\n' : '\n'
		// '\x85' : '\n'
		// default : ''
		char val = peek();
		if (FULL_LINEBR.indexOf(val) != -1) {
			if (RN.equals(prefix(2)))
				forward(2);
			else
				forward();
			return "\n";
		}
		return "";
	}

	private String ch (char ch) {
		return "'" + ch + "' (" + (int)ch + ")";
	}

	public class TokenizerException extends RuntimeException {
		public TokenizerException (String message, Throwable cause) {
			super("Line " + getLineNumber() + ", column " + getColumn() + ": " + message, cause);
		}

		public TokenizerException (String message) {
			this(message, null);
		}
	}

	static class SimpleKey {
		public final int tokenNumber;
		public final int column;

		public SimpleKey (int tokenNumber, int column) {
			this.tokenNumber = tokenNumber;
			this.column = column;
		}
	}

	public static void main (String[] args) throws Exception {
		for (Iterator iter = new Tokenizer(new FileReader("test/test.yml")).iterator(); iter.hasNext();)
			System.out.println(iter.next());
	}
}

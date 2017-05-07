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

package com.esotericsoftware.yamlbeans.emitter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a> */
class EmitterWriter {
	private static final Map<Integer, String> ESCAPE_REPLACEMENTS = new HashMap();
	static {
		ESCAPE_REPLACEMENTS.put((int)'\0', "0");
		ESCAPE_REPLACEMENTS.put((int)'\u0007', "a");
		ESCAPE_REPLACEMENTS.put((int)'\u0008', "b");
		ESCAPE_REPLACEMENTS.put((int)'\u0009', "t");
		ESCAPE_REPLACEMENTS.put((int)'\n', "n");
		ESCAPE_REPLACEMENTS.put((int)'\u000b', "v");
		ESCAPE_REPLACEMENTS.put((int)'\u000c', "f");
		ESCAPE_REPLACEMENTS.put((int)'\r', "r");
		ESCAPE_REPLACEMENTS.put((int)'\u001b', "e");
		ESCAPE_REPLACEMENTS.put((int)'"', "\"");
		ESCAPE_REPLACEMENTS.put((int)'\\', "\\");
		ESCAPE_REPLACEMENTS.put((int)'\u0085', "N");
		ESCAPE_REPLACEMENTS.put((int)'\u00a0', "_");
	}

	private final Writer writer;
	private boolean whitespace = true;

	private int column = 0;
	private boolean indentation = true;

	public EmitterWriter (Writer stream) {
		this.writer = stream;
	}

	public void writeStreamEnd () throws IOException {
		flushStream();
	}

	public void writeIndicator (String indicator, boolean needWhitespace, boolean whitespace, boolean indentation)
		throws IOException {
		String data = null;
		if (this.whitespace || !needWhitespace)
			data = indicator;
		else
			data = " " + indicator;
		this.whitespace = whitespace;
		this.setIndentation(this.getIndentation() && indentation);
		setColumn(getColumn() + data.length());
		writer.write(data);
	}

	public void writeIndent (int indent) throws IOException {
		if (indent == -1)
			indent = 0;
		if (!getIndentation() || getColumn() > indent || getColumn() == indent && !whitespace)
			writeLineBreak(null);
		if (getColumn() < indent) {
			whitespace = true;
			StringBuffer data = new StringBuffer();
			for (int i = 0, j = indent - getColumn(); i < j; i++)
				data.append(" ");
			setColumn(indent);
			writer.write(data.toString());
		}
	}

	public void writeVersionDirective (String version_text) throws IOException {
		writer.write("%YAML " + version_text);
		writeLineBreak(null);
	}

	public void writeTagDirective (String handle, String prefix) throws IOException {
		writer.write("%TAG " + handle + " " + prefix);
		writeLineBreak(null);
	}

	public void writeDoubleQuoted (String text, boolean split, int indent, int wrapColumn, boolean escapeUnicode)
		throws IOException {
		writeIndicator("\"", true, false, false);
		int start = 0;
		int ending = 0;
		String data = null;
		while (ending <= text.length()) {
			int ch = 0;
			if (ending < text.length())
				ch = text.codePointAt(ending);
			if (ch == 0 || "\"\\\u0085".indexOf(ch) != -1 || !isSpaceToTilde(ch)) {
				if (start < ending) {
					data = text.substring(start, ending);
					setColumn(getColumn() + data.length());
					writer.write(data);
					start = ending;
				}
				if (ch != 0) {
					if (ESCAPE_REPLACEMENTS.containsKey(ch))
						data = "\\" + ESCAPE_REPLACEMENTS.get(ch);
					else {
						data = unicodeCaseByLength(escapeUnicode, ch);
					}
					setColumn(getColumn() + data.length());
					writer.write(data);
					start = ending + 1;
				}
			}
			if ((0 < ending && ending < (text.length() - 1)) && (ch == ' ' || start <= ending)
				&& (getColumn() + (ending - start)) > wrapColumn && split) {
				if (start < ending) {
					data = text.substring(start, ending) + '\\';
					start = ending;
				}
				else
					data = "\\";
				setColumn(getColumn() + data.length());
				writer.write(data);
				writeIndent(indent);
				whitespace = false;
				setIndentation(false);
				if (isSpace(text.charAt(start))) {
					data = "\\";
					setColumn(getColumn() + data.length());
					writer.write(data);
				}
			}
			ending += 1;
		}

		writeIndicator("\"", false, false, false);
	}

	private boolean isSpaceToTilde(int ch) {
		return '\u0020' <= ch && ch <= '\u007E';
	}

	private String unicodeCaseByLength(boolean escapeUnicode, int ch) {
		String data;
		if (escapeUnicode) {
			data = Integer.toString(ch, 16);
			if (data.length() == 1)
				data = "000" + data;
			else if (data.length() == 2)
				data = "00" + data;
			else if (data.length() == 3) {
				data = "0" + data;
			}
			data = "\\u" + data;
		} else
			data = new String(Character.toChars(ch));
		return data;
	}

	public void writeSingleQuoted (String text, boolean split, int indent, int wrapColumn) throws IOException {
		writeIndicator("'", true, false, false);
		boolean spaces = false;
		boolean breaks = false;
		int start = 0, ending = 0;
		char ceh = 0;
		String data = null;
		while (ending <= text.length()) {
			ceh = 0;
			if (ending < text.length())
				ceh = text.charAt(ending);
			if (spaces) {
				if (isZero(ceh) || ceh != 32) {
					if (start + 1 == ending && getColumn() > wrapColumn && split && start != 0 && ending != text.length())
						writeIndent(indent);
					else {
						data = text.substring(start, ending);
						setColumn(getColumn() + data.length());
						writer.write(data);
					}
					start = ending;
				}
			} else if (breaks) {
				if (isZero(ceh) || !(isNewLine(ceh) || isNextLine(ceh))) {
					data = text.substring(start, ending);
					setLineBreak(data);
					writeIndent(indent);
					start = ending;
				}
			} else if (isZero(ceh) || !(isNewLine(ceh) || isNextLine(ceh))) {
				if (start < ending) {
					data = text.substring(start, ending);
					setColumn(getColumn() + data.length());
					writer.write(data);
					start = ending;
				}
			}
			if (ceh == '\'') {
				data = "''";
				setColumn(getColumn() + 2);
				writer.write(data);
				start = ending + 1;
			}
			if (isNotZero(ceh)) {
				spaces = isSpace(ceh);
				breaks = isNewLine(ceh) || isNextLine(ceh);
			}
			ending++;
		}
		writeIndicator("'", false, false, false);
	}

	private boolean isNotZero(char ceh) {
		return ceh != 0;
	}

	private boolean isNextLine(char ceh) {
		return ceh == '\u0085';
	}

	private boolean isNewLine(char ceh) {
		return ceh == '\n';
	}

	private boolean isZero(char ceh) {
		return ceh == 0;
	}

	public void writeFolded (String text, int indent, int wrapColumn) throws IOException {
		String chomp = determineChomp(text);
		writeIndicator(">" + chomp, true, false, false);
		writeIndent(indent);
		boolean leadingSpace = false;
		boolean spaces = false;
		boolean breaks = false;
		int start = 0, ending = 0;
		String data = null;
		while (ending <= text.length()) {
			char ceh = 0;
			if (ending < text.length())
				ceh = text.charAt(ending);
			if (breaks) {
				if (isZero(ceh) || !(isNewLine(ceh) || isNextLine(ceh))) {
					if (!leadingSpace && isNotZero(ceh) && isNotSpace(ceh) && isNewLine(text.charAt(start)))
						writeLineBreak(null);
					leadingSpace = isSpace(ceh);
					data = text.substring(start, ending);
					setLineBreak(data);
					if (isNotZero(ceh))
						writeIndent(indent);
					start = ending;
				}
			} else if (spaces) {
				if (isNotSpace(ceh)) {
					if (start + 1 == ending && getColumn() > wrapColumn)
						writeIndent(indent);
					else {
						data = text.substring(start, ending);
						setColumn(getColumn() + data.length());
						writer.write(data);
					}
					start = ending;
				}
			} else if (isZero(ceh) || isSpace(ceh) || isNewLine(ceh) || isNextLine(ceh)) {
				data = text.substring(start, ending);
				writer.write(data);
				if (isZero(ceh))
					writeLineBreak(null);
				start = ending;
			}
			if (isNotZero(ceh)) {
				breaks = isNewLine(ceh) || isNextLine(ceh);
				spaces = isSpace(ceh);
			}
			ending++;
		}
	}

	private boolean isSpace(char ceh) {
		return ceh == ' ';
	}

	public void writeLiteral (String text, int indent) throws IOException {
		String chomp = determineChomp(text);
		writeIndicator("|" + chomp, true, false, false);
		writeIndent(indent);
		boolean breaks = false;
		int start = 0, ending = 0;
		String data = null;
		while (ending <= text.length()) {
			char ceh = 0;
			if (ending < text.length())
				ceh = text.charAt(ending);
			if (breaks) {
				if (isZero(ceh) || !(isNewLine(ceh) || isNextLine(ceh))) {
					data = text.substring(start, ending);
					setLineBreak(data);
					if (isNotZero(ceh))
						writeIndent(indent);
					start = ending;
				}
			} else if (isZero(ceh) || isNewLine(ceh) || isNextLine(ceh)) {
				data = text.substring(start, ending);
				writer.write(data);
				if (isZero(ceh))
					writeLineBreak(null);
				start = ending;
			}
			if (isNotZero(ceh)) 
				breaks = isNewLine(ceh) || isNextLine(ceh);
			ending++;
		}
	}

	public void writePlain (String text, boolean split, int indent, int wrapColumn) throws IOException {
		if (text == null || "".equals(text)) return;
		String data = null;
		if (!whitespace) {
			data = " ";
			setColumn(getColumn() + data.length());
			writer.write(data);
		}
		whitespace = false;
		setIndentation(false);
		boolean spaces = false, breaks = false;
		int start = 0, ending = 0;
		while (ending <= text.length()) {
			char ceh = 0;
			if (ending < text.length())
				ceh = text.charAt(ending);
			if (spaces) {
				if (isNotSpace(ceh)) {
					if (start + 1 == ending && getColumn() > wrapColumn && split) {
						writeIndent(indent);
						whitespace = false;
						setIndentation(false);
					} else {
						data = text.substring(start, ending);
						setColumn(getColumn() + data.length());
						writer.write(data);
					}
					start = ending;
				}
			} else if (breaks) {
				if (isNotNewLine(ceh) && isNotNextLine(ceh)) {
					if (isNewLine(text.charAt(start)))
						writeLineBreak(null);
					data = text.substring(start, ending);
					setLineBreak(data);
					writeIndent(indent);
					whitespace = false;
					setIndentation(false);
					start = ending;
				}
			} else if (isZero(ceh) || isSpace(ceh) || isNewLine(ceh) || isNextLine(ceh)) {
				data = text.substring(start, ending);
				setColumn(getColumn() + data.length());
				writer.write(data);
				start = ending;
			}
			if (isNotZero(ceh)) {
				spaces = isSpace(ceh);
				breaks = isNewLine(ceh) || isNextLine(ceh);
			}
			ending++;
		}
	}

	private void setLineBreak(String data) throws IOException {
		for (int i = 0, j = data.length(); i < j; i++) {
			char cha = data.charAt(i);
			if (isNewLine(cha))
				writeLineBreak(null);
			else
				writeLineBreak("" + cha);
		}
	}

	private boolean isNotNextLine(char ceh) {
		return ceh != '\u0085';
	}

	private boolean isNotNewLine(char ceh) {
		return ceh != '\n';
	}

	private boolean isNotSpace(char ceh) {
		return ceh != ' ';
	}

	public void writeLineBreak (String data) throws IOException {
		if (data == null)
			data = "\n";
		whitespace = true;
		setIndentation(true);
		setColumn(0);
		writer.write(data);
	}

	public void flushStream () throws IOException {
		writer.flush();
	}

	private String determineChomp (String text) {
		String tail = text.substring(text.length() - 2, text.length() - 1);
		while (tail.length() < 2)
			tail = " " + tail;
		char ceh = tail.charAt(tail.length() - 1);
		char ceh2 = tail.charAt(tail.length() - 2);
		return isNewLine(ceh) || isNextLine(ceh) ? isNewLine(ceh2) || isNextLine(ceh2) ? "+" : "" : "-";
	}

	public void close () throws IOException {
		writer.close();
	}

	public boolean getIndentation() {
		return indentation;
	}

	public void setIndentation(boolean indentation) {
		this.indentation = indentation;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
}

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

import java.util.regex.Pattern;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a> */
class ScalarAnalysis {
	static private final Pattern DOCUMENT_INDICATOR = Pattern.compile("^(---|\\.\\.\\.)");
	static private final String NULL_BL_T_LINEBR = "\0 \t\r\n\u0085";
	static private final String SPECIAL_INDICATOR = "#,[]{}#&*!|>'\"%@`";
	static private final String FLOW_INDICATOR = ",?[]{}";

	public final String scalar;
	public final boolean empty;
	public final boolean multiline;
	public final boolean allowFlowPlain;
	public final boolean allowBlockPlain;
	public final boolean allowSingleQuoted;
	public final boolean allowDoubleQuoted;
	public final boolean allowBlock;

	private ScalarAnalysis (String scalar, boolean empty, boolean multiline, boolean allowFlowPlain, boolean allowBlockPlain,
		boolean allowSingleQuoted, boolean allowDoubleQuoted, boolean allowBlock) {
		this.scalar = scalar;
		this.empty = empty;
		this.multiline = multiline;
		this.allowFlowPlain = allowFlowPlain;
		this.allowBlockPlain = allowBlockPlain;
		this.allowSingleQuoted = allowSingleQuoted;
		this.allowDoubleQuoted = allowDoubleQuoted;
		this.allowBlock = allowBlock;
	}

	static public ScalarAnalysis analyze (String scalar, boolean escapeUnicode) {
		if (scalar == null || "".equals(scalar)) return new ScalarAnalysis(scalar, true, false, false, true, true, true, false);
		boolean blockIndicators = false;
		boolean flowIndicators = false;
		boolean lineBreaks = false;
		boolean specialCharacters = false;

		// Whitespaces.
		// boolean inlineSpaces = false; // non-space space+ non-space
		boolean inlineBreaks = false; // non-space break+ non-space
		boolean leadingSpaces = false; // ^ space+ (non-space | $)
		boolean leadingBreaks = false; // ^ break+ (non-space | $)
		boolean trailingSpaces = false; // (^ | non-space) space+ $
		boolean trailingBreaks = false; // (^ | non-space) break+ $
		boolean inlineBreaksSpaces = false; // non-space break+ space+ non-space
		boolean mixedBreaksSpaces = false; // anything else

		if (DOCUMENT_INDICATOR.matcher(scalar).matches()) {
			blockIndicators = true;
			flowIndicators = true;
		}

		boolean preceededBySpace = true;
		boolean followedBySpace = scalar.length() == 1 || NULL_BL_T_LINEBR.indexOf(scalar.charAt(1)) != -1;

		boolean spaces = false;
		boolean breaks = false;
		boolean mixed = false;
		boolean leading = false;

		int index = 0;

		while (index < scalar.length()) {
			char ceh = scalar.charAt(index);
			if (index == 0) {
				if (SPECIAL_INDICATOR.indexOf(ceh) != -1) {
					flowIndicators = true;
					blockIndicators = true;
				}
				if (ceh == '?' || ceh == ':') {
					flowIndicators = true;
					if (followedBySpace) blockIndicators = true;
				}
				if (ceh == '-' && followedBySpace) {
					flowIndicators = true;
					blockIndicators = true;
				}
			} else {
				if (FLOW_INDICATOR.indexOf(ceh) != -1) flowIndicators = true;
				if (ceh == ':') {
					flowIndicators = true;
					if (followedBySpace) blockIndicators = true;
				}
				if (ceh == '#' && preceededBySpace) {
					flowIndicators = true;
					blockIndicators = true;
				}
			}
			if (ceh == '\n' || '\u0085' == ceh) lineBreaks = true;
			if (escapeUnicode) {
				if (ceh != '\n' && ceh != '\t' && !('\u0020' <= ceh && ceh <= '\u007E')) specialCharacters = true;
			}
			if (' ' == ceh || '\n' == ceh || '\u0085' == ceh) {
				if (spaces && breaks) {
					if (ceh != ' ') mixed = true;
				} else if (spaces) {
					if (ceh != ' ') {
						breaks = true;
						mixed = true;
					}
				} else if (breaks) {
					if (ceh == ' ') spaces = true;
				} else {
					leading = index == 0;
					if (ceh == ' ')
						spaces = true;
					else
						breaks = true;
				}
			} else if (spaces || breaks) {
				if (leading) {
					if (spaces && breaks)
						mixedBreaksSpaces = true;
					else if (spaces)
						leadingSpaces = true;
					else if (breaks) {
						leadingBreaks = true;
					}
				} else if (mixed)
					mixedBreaksSpaces = true;
				else if (spaces && breaks)
					inlineBreaksSpaces = true;
				else if (spaces) {
					// inlineSpaces = true;
				} else if (breaks) {
					inlineBreaks = true;
				}
				spaces = breaks = mixed = leading = false;
			}

			if ((spaces || breaks) && index == scalar.length() - 1) {
				if (spaces && breaks)
					mixedBreaksSpaces = true;
				else if (spaces) {
					trailingSpaces = true;
					if (leading) leadingSpaces = true;
				} else if (breaks) {
					trailingBreaks = true;
					if (leading) leadingBreaks = true;
				}
				spaces = breaks = mixed = leading = false;
			}
			index++;
			preceededBySpace = NULL_BL_T_LINEBR.indexOf(ceh) != -1;
			followedBySpace = index + 1 >= scalar.length() || NULL_BL_T_LINEBR.indexOf(scalar.charAt(index + 1)) != -1;
		}
		boolean allowFlowPlain = true;
		boolean allowBlockPlain = true;
		boolean allowSingleQuoted = true;
		boolean allowDoubleQuoted = true;
		boolean allowBlock = true;

		if (leadingSpaces || leadingBreaks || trailingSpaces) allowFlowPlain = allowBlockPlain = allowBlock = false;

		if (trailingBreaks) allowFlowPlain = allowBlockPlain = false;

		if (inlineBreaksSpaces) allowFlowPlain = allowBlockPlain = allowSingleQuoted = false;

		if (mixedBreaksSpaces || specialCharacters) allowFlowPlain = allowBlockPlain = allowSingleQuoted = allowBlock = false;

		if (inlineBreaks) allowFlowPlain = allowBlockPlain = allowSingleQuoted = false;

		if (trailingBreaks) allowSingleQuoted = false;

		if (lineBreaks) allowFlowPlain = allowBlockPlain = false;

		if (flowIndicators) allowFlowPlain = false;

		if (blockIndicators) allowBlockPlain = false;

		return new ScalarAnalysis(scalar, false, lineBreaks, allowFlowPlain, allowBlockPlain, allowSingleQuoted, allowDoubleQuoted,
			allowBlock);
	}
}

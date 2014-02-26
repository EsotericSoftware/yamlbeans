/*
 * Copyright (c) 2008 Nathan Sweet
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

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class ScalarToken extends Token {
	private String value;
	private boolean plain;
	private char style;

	public ScalarToken (final String value, final boolean plain) {
		this(value, plain, (char)0);
	}

	public ScalarToken (final String value, final boolean plain, final char style) {
		super(TokenType.SCALAR);
		this.value = value;
		this.plain = plain;
		this.style = style;
	}

	public boolean getPlain () {
		return this.plain;
	}

	public String getValue () {
		return this.value;
	}

	public char getStyle () {
		return this.style;
	}

	public String toString () {
		return "<" + type + " value='" + value + "' plain='" + plain + "' style='" + (style == 0 ? "" : style) + "'>";
	}
}

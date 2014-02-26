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

package com.esotericsoftware.yamlbeans.emitter;

import com.esotericsoftware.yamlbeans.Version;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class EmitterConfig {
	Version version = new Version(1, 1);
	boolean canonical;
	boolean useVerbatimTags = true;
	int indentSize = 3;
	int wrapColumn = 100;
	boolean escapeUnicode = true;

	/** Sets the YAML version to output. Default is 1.1. */
	public void setVersion (Version version) {
		if (version == null) throw new IllegalArgumentException("version cannot be null.");
		this.version = version;
	}

	/** If true, the YAML output will be canonical. Default is false. */
	public void setCanonical (boolean canonical) {
		this.canonical = canonical;
	}

	/** Sets the number of spaces to indent. Default is 3. */
	public void setIndentSize (int indentSize) {
		if (indentSize < 2) throw new IllegalArgumentException("indentSize cannot be less than 2.");
		this.indentSize = indentSize;
	}

	/** Sets the column at which values will attempt to wrap. Default is 100. */
	public void setWrapColumn (int wrapColumn) {
		if (wrapColumn <= 4) throw new IllegalArgumentException("wrapColumn must be greater than 4.");
		this.wrapColumn = wrapColumn;
	}

	/** If false, tags will never be surrounded by angle brackets (eg, "!<java.util.LinkedList>"). Default is true. */
	public void setUseVerbatimTags (boolean useVerbatimTags) {
		this.useVerbatimTags = useVerbatimTags;
	}

	/** If false, UTF-8 unicode characters will be output instead of the escaped unicode character code. */
	public void setEscapeUnicode (boolean escapeUnicode) {
		this.escapeUnicode = escapeUnicode;
	}
}

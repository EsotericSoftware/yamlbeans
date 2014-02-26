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

import java.util.Map;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a> */
public class DocumentStartEvent extends Event {
	public final boolean isExplicit;
	public final Version version;
	public final Map<String, String> tags;

	public DocumentStartEvent (boolean explicit, Version version, Map<String, String> tags) {
		super(EventType.DOCUMENT_START);
		this.isExplicit = explicit;
		this.version = version;
		this.tags = tags;
	}

	public String toString () {
		return "<" + type + " explicit='" + isExplicit + "' version='" + version + "' tags='" + tags + "'>";
	}
}

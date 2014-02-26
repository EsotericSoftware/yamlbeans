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
public enum TokenType {
	DOCUMENT_START, //
	DOCUMENT_END, //
	BLOCK_MAPPING_START, //
	BLOCK_SEQUENCE_START, //
	BLOCK_ENTRY, //
	BLOCK_END, //
	FLOW_ENTRY, //
	FLOW_MAPPING_END, //
	FLOW_MAPPING_START, //
	FLOW_SEQUENCE_END, //
	FLOW_SEQUENCE_START, //
	KEY, //
	VALUE, //
	STREAM_END, //
	STREAM_START, //
	ALIAS, //
	ANCHOR, //
	DIRECTIVE, //
	SCALAR, //
	TAG;

	public String toString () {
		return name().toLowerCase().replace('_', ' ');
	}
}

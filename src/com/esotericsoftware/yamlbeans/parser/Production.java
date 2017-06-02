package com.esotericsoftware.yamlbeans.parser;

import com.esotericsoftware.yamlbeans.parser.event.Event;

public abstract class Production {
	public abstract Event produce ();
}

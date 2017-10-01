package com.esotericsoftware.yamlbeans.document;

import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.AliasEvent;

public class YamlAlias extends YamlElement {

	@Override
	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		emitter.emit(new AliasEvent(anchor));
	}
	
	@Override
	public String toString() {
		return "*" + anchor;
	}
}

package com.esotericsoftware.yamlbeans.docs;

import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;

public class YamlScalar extends YamlElement {

	String value;
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		char style = 0; // TODO determine style
		emitter.emit(new ScalarEvent(anchor, tag, new boolean[] {true, true}, value, style));
	}
}

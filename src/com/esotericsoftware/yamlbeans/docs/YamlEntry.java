package com.esotericsoftware.yamlbeans.docs;

import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;

public class YamlEntry {
	
	YamlScalar key;
	YamlElement value;
	
	public YamlEntry(YamlScalar key, YamlElement value) {
		this.key = key;
		this.value = value;
	}

	public YamlScalar getKey() {
		return key;
	}
	
	public YamlElement getValue() {
		return value;
	}
	
	public void setKey(YamlScalar key) {
		this.key = key;
	}
	
	public void setValue(YamlElement value) {
		this.value = value;
	}

	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		key.emitEvent(emitter, config);
		if(value==null)
			emitter.emit(new ScalarEvent(null, null, new boolean[] {true, true}, null, (char)0));
		else
			value.emitEvent(emitter, config);
	}
	
}

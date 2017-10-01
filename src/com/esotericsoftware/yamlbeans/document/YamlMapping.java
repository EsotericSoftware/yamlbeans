package com.esotericsoftware.yamlbeans.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.MappingStartEvent;

public class YamlMapping extends YamlElement implements YamlDocument {

	List<YamlEntry> entries = new LinkedList<YamlEntry>();

	public void addEntry(YamlEntry entry) {
		entries.add(entry);
	}

	@Override
	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		emitter.emit(new MappingStartEvent(anchor, tag, tag==null, false));
		for(YamlEntry entry : entries)
			entry.emitEvent(emitter, config);
		emitter.emit(Event.MAPPING_END);
	}

}

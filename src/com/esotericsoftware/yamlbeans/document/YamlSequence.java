package com.esotericsoftware.yamlbeans.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.SequenceStartEvent;

public class YamlSequence extends YamlElement implements YamlDocument {

	List<YamlElement> elements = new LinkedList<YamlElement>();

	public void addElement(YamlElement element) {
		elements.add(element);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(anchor!=null) {
			sb.append('&');
			sb.append(anchor);
			sb.append(' ');
		}
		if(tag!=null) {
			sb.append(" !");
			sb.append(tag);
		}
		if(!elements.isEmpty()) {
			sb.append('[');
			for(YamlElement element : elements) {
				sb.append(element.toString());
				sb.append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append(']');
		}
		return sb.toString();
	}

	
	@Override
	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		emitter.emit(new SequenceStartEvent(anchor, tag, tag==null, false));
		for (YamlElement element : elements)
			element.emitEvent(emitter, config);
		emitter.emit(Event.SEQUENCE_END);	
	}

}

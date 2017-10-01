package com.esotericsoftware.yamlbeans.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.MappingStartEvent;

public class YamlMapping extends YamlElement implements YamlDocument {

	// use a list to keep the sequence
	List<YamlEntry> entries = new LinkedList<YamlEntry>();

	public int size() {
		return entries.size();
	}
	
	public void addEntry(YamlEntry entry) {
		entries.add(entry);
	}
	
	public boolean deleteEntry(String key) {
		for(int index = 0; index < entries.size(); index++) {
			if(key.equals(entries.get(index).getKey().getValue())) {
				entries.remove(index);
				return true;
			}
		}
		return false;
	}

	public YamlEntry getEntry(String key) throws YamlException {
		for(YamlEntry entry : entries) {
			if(key.equals(entry.getKey().getValue()))
				return entry;
		}
		return null;
	}
	
	public YamlEntry getEntry(int index) throws YamlException {
		return entries.get(index);
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
		if(!entries.isEmpty()) {
			sb.append('{');
			for(YamlEntry entry : entries) {
				sb.append(entry.toString());
				sb.append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append('}');
		}
		return sb.toString();
	}
	
	@Override
	public void emitEvent(Emitter emitter, WriteConfig config) throws EmitterException, IOException {
		emitter.emit(new MappingStartEvent(anchor, tag, tag==null, false));
		for(YamlEntry entry : entries)
			entry.emitEvent(emitter, config);
		emitter.emit(Event.MAPPING_END);
	}
	
	public void setEntry(String key, boolean value) throws YamlException {
		setEntry(key, new YamlScalar(value));
	}

	public void setEntry(String key, Number value) throws YamlException {
		setEntry(key, new YamlScalar(value));
	}

	public void setEntry(String key, String value) throws YamlException {
		setEntry(key, new YamlScalar(value));
	}

	public void setEntry(String key, YamlElement value) throws YamlException {
		YamlEntry entry = getEntry(key);
		if(entry!=null)
			entry.setValue(value);
		else {
			entry = new YamlEntry(new YamlScalar(key), value);
			addEntry(entry);
		}
		
	}

	public YamlElement getElement(int item) throws YamlException {
		throw new YamlException("Can only get element on sequence!");
	}

	public void deleteElement(int element) throws YamlException {
		throw new YamlException("Can only delete element on sequence!");
	}
	
	public void setElement(int item, boolean element) throws YamlException {
		throw new YamlException("Can only set element on sequence!");
	}

	public void setElement(int item, Number element) throws YamlException {
		throw new YamlException("Can only set element on sequence!");
	}

	public void setElement(int item, String element) throws YamlException {
		throw new YamlException("Can only set element on sequence!");
	}

	public void setElement(int item, YamlElement element) throws YamlException {
		throw new YamlException("Can only set element on sequence!");
	}

	public void addElement(boolean element) throws YamlException {
		throw new YamlException("Can only add element on sequence!");
	}

	public void addElement(Number element) throws YamlException {
		throw new YamlException("Can only add element on sequence!");
	}

	public void addElement(String element) throws YamlException {
		throw new YamlException("Can only add element on sequence!");
	}

	public void addElement(YamlElement element) throws YamlException {
		throw new YamlException("Can only add element on sequence!");
	}
}

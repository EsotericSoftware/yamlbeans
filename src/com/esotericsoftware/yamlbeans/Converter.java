package com.esotericsoftware.yamlbeans;

public abstract class Converter {
	private Converter next;
	Class type;
	public void setNext(Converter converter) {
		this.next = converter;
	}
	public abstract Object getType(Class type, String value) throws YamlException;
}

package com.esotericsoftware.yamlbeans;

public abstract class Converter {
	protected Converter next;
	Class type;
	public Converter setNext(Converter converter) {
		this.next = converter;
		return next;
	}
	public abstract Object getType(Class type, String value) throws YamlException;
}

package com.esotericsoftware.yamlbeans;

public class StringConverter extends Converter {
	public StringConverter(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == String.class)
			return value;
		else
			return next.getType(type, value);
	}
}

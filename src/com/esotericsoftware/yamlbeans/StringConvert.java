package com.esotericsoftware.yamlbeans;

public class StringConvert extends Converter {
	public StringConvert(Class type) {
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

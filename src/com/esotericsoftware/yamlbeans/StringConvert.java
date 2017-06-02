package com.esotericsoftware.yamlbeans;

public class StringConvert extends Converter {
	public StringConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == String.class)
			convertedValue = value;
		return convertedValue;
	}
}

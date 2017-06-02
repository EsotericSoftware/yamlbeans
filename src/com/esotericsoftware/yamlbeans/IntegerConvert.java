package com.esotericsoftware.yamlbeans;

public class IntegerConvert extends Converter {
	public IntegerConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Integer.TYPE)
			convertedValue = value.length() == 0 ? 0 : Integer.decode(value);
		else if (type == Integer.class)
			convertedValue = value.length() == 0 ? null : Integer.decode(value);
		return convertedValue;
	}
}

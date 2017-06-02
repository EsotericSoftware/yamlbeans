package com.esotericsoftware.yamlbeans;

public class ShortConvert extends Converter {
	public ShortConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Short.TYPE)
			convertedValue = value.length() == 0 ? 0 : Short.decode(value);
		else if (type == Short.class)
			convertedValue = value.length() == 0 ? null : Short.decode(value);
		return convertedValue;
	}
}
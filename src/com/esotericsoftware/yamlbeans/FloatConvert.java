package com.esotericsoftware.yamlbeans;

public class FloatConvert extends Converter {
	public FloatConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Float.TYPE)
			convertedValue = value.length() == 0 ? 0 : Float.valueOf(value);
		else if (type == Float.class)
			convertedValue = value.length() == 0 ? null : Float.valueOf(value);
		return convertedValue;
	}
}
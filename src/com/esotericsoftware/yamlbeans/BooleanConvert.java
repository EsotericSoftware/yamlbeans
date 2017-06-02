package com.esotericsoftware.yamlbeans;

public class BooleanConvert extends Converter {
	public BooleanConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Boolean.TYPE)
			convertedValue = value.length() == 0 ? 0 : Boolean.valueOf(value);
		else if (type == Boolean.class)
			convertedValue = value.length() == 0 ? null : Boolean.valueOf(value);
		return convertedValue;
	}
}

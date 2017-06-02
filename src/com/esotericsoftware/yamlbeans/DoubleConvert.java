package com.esotericsoftware.yamlbeans;

public class DoubleConvert extends Converter {
	public DoubleConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Double.TYPE)
			convertedValue = value.length() == 0 ? 0 : Double.valueOf(value);
		else if (type == Double.class)
			convertedValue = value.length() == 0 ? null : Double.valueOf(value);
		return convertedValue;
	}
}
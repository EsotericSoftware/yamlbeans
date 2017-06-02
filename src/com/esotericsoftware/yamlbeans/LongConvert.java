package com.esotericsoftware.yamlbeans;

public class LongConvert extends Converter {
	public LongConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Long.TYPE)
			convertedValue = value.length() == 0 ? 0 : Long.decode(value);
		else if (type == Long.class)
			convertedValue = value.length() == 0 ? null : Long.decode(value);
		return convertedValue;
	}
}
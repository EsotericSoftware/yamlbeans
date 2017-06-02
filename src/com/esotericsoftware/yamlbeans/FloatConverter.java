package com.esotericsoftware.yamlbeans;

public class FloatConverter extends Converter {
	public FloatConverter(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Float.TYPE)
			return value.length() == 0 ? 0 : Float.valueOf(value);
		else if (type == Float.class)
			return value.length() == 0 ? null : Float.valueOf(value);
		else
			return next.getType(type, value);
	}
}
package com.esotericsoftware.yamlbeans;

public class DoubleConverter extends Converter {
	public DoubleConverter(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Double.TYPE)
			return value.length() == 0 ? 0 : Double.valueOf(value);
		else if (type == Double.class)
			return value.length() == 0 ? null : Double.valueOf(value);
		else
			return next.getType(type, value);
	}
}
package com.esotericsoftware.yamlbeans;

public class LongConverter extends Converter {
	public LongConverter(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Long.TYPE)
			return value.length() == 0 ? 0 : Long.decode(value);
		else if (type == Long.class)
			return value.length() == 0 ? null : Long.decode(value);
		else
			return next.getType(type, value);
	}
}
package com.esotericsoftware.yamlbeans;

public class ShortConvert extends Converter {
	public ShortConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Short.TYPE)
			return value.length() == 0 ? 0 : Short.decode(value);
		else if (type == Short.class)
			return value.length() == 0 ? null : Short.decode(value);
		else
			return next.getType(type, value);
	}
}
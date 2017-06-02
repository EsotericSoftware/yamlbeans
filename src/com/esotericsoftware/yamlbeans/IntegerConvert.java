package com.esotericsoftware.yamlbeans;

public class IntegerConvert extends Converter {
	public IntegerConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Integer.TYPE)
			return value.length() == 0 ? 0 : Integer.decode(value);
		else if (type == Integer.class)
			return value.length() == 0 ? null : Integer.decode(value);
		else
			return next.getType(type, value);
	}
}

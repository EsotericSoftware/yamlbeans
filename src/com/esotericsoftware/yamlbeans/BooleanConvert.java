package com.esotericsoftware.yamlbeans;

public class BooleanConvert extends Converter {
	public BooleanConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Boolean.TYPE)
			return  value.length() == 0 ? 0 : Boolean.valueOf(value);
		else if (type == Boolean.class)
			return  value.length() == 0 ? null : Boolean.valueOf(value);
		else
			return next.getType(type, value);
	}
}

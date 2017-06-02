package com.esotericsoftware.yamlbeans;

public class ByteConvert extends Converter {
	public ByteConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Byte.TYPE)
			return value.length() == 0 ? 0 : Byte.decode(value);
		else if (type == Byte.class)
			return value.length() == 0 ? null : Byte.decode(value);
		else 
			return next.getType(type, value);
	}
}

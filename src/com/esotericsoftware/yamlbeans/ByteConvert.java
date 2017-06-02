package com.esotericsoftware.yamlbeans;

public class ByteConvert extends Converter {
	public ByteConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		Object convertedValue;
		if (type == Byte.TYPE)
			convertedValue = value.length() == 0 ? 0 : Byte.decode(value);
		else if (type == Byte.class)
			convertedValue = value.length() == 0 ? null : Byte.decode(value);
		else 
			throw new YamlException("Unknown field type.");
		return convertedValue;
	}
}

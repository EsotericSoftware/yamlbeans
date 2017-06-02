package com.esotericsoftware.yamlbeans;

public class CharacterConverter extends Converter {
	public CharacterConverter(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) throws YamlException {
		if (type == Character.TYPE)
			return value.length() == 0 ? 0 : value.charAt(0);
		else if (type == Character.class)
			return  value.length() == 0 ? null : value.charAt(0);
		else
			return next.getType(type, value);
	}
}
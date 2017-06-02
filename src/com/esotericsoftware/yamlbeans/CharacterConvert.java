package com.esotericsoftware.yamlbeans;

public class CharacterConvert extends Converter {
	public CharacterConvert(Class type) {
		this.type = type;
	}

	@Override
	public Object getType(Class type, String value) {
		Object convertedValue = null;
		if (type == Character.TYPE)
			convertedValue = value.length() == 0 ? 0 : value.charAt(0);
		else if (type == Character.class)
			convertedValue = value.length() == 0 ? null : value.charAt(0);
		return convertedValue;
	}
}
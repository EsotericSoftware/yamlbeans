package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class CharacterConverter extends Converter {

    public CharacterConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Character.TYPE) {
            return value.length() == 0 ? 0 : value.charAt(0);
        } else if (type == Character.class) {
            return value.length() == 0 ? null : value.charAt(0);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}
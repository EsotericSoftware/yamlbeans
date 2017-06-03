package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class ShortConverter extends Converter {

    public ShortConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Short.TYPE) {
            return value.length() == 0 ? 0 : Short.decode(value);
        } else if (type == Short.class) {
            return value.length() == 0 ? null : Short.decode(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}
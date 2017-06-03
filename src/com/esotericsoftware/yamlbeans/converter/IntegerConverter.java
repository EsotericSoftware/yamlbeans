package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class IntegerConverter extends Converter {

    public IntegerConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Integer.TYPE) {
            return value.length() == 0 ? 0 : Integer.decode(value);
        } else if (type == Integer.class) {
            return value.length() == 0 ? null : Integer.decode(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}

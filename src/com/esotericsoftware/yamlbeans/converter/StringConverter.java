package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class StringConverter extends Converter {

    public StringConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == String.class) {
            return value;
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}

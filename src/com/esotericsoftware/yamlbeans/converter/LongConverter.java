package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class LongConverter extends Converter {

    public LongConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Long.TYPE) {
            return value.length() == 0 ? 0 : Long.decode(value);
        } else if (type == Long.class) {
            return value.length() == 0 ? null : Long.decode(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}
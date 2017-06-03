package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class BooleanConverter extends Converter {

    public BooleanConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Boolean.TYPE) {
            return value.length() == 0 ? 0 : Boolean.valueOf(value);
        } else if (type == Boolean.class) {
            return value.length() == 0 ? null : Boolean.valueOf(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}

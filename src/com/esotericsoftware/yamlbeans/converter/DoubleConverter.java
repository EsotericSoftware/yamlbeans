package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class DoubleConverter extends Converter {

    public DoubleConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Double.TYPE) {
            return value.length() == 0 ? 0 : Double.valueOf(value);
        } else if (type == Double.class) {
            return value.length() == 0 ? null : Double.valueOf(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}
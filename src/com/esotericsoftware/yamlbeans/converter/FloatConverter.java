package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class FloatConverter extends Converter {

    public FloatConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Float.TYPE) {
            return value.length() == 0 ? 0 : Float.valueOf(value);
        } else if (type == Float.class) {
            return value.length() == 0 ? null : Float.valueOf(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}
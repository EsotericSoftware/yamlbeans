package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public class ByteConverter extends Converter {

    public ByteConverter(Class type) {
        super(type);
    }

    protected Object convert(String value) throws YamlException {
        if (type == Byte.TYPE) {
            return value.length() == 0 ? 0 : Byte.decode(value);
        } else if (type == Byte.class) {
            return value.length() == 0 ? null : Byte.decode(value);
        } else {
            throw new YamlException("Unknown field type");
        }
    }
}

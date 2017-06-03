package com.esotericsoftware.yamlbeans.converter;

import com.esotericsoftware.yamlbeans.YamlException;

public abstract class Converter {

    protected Class type;
    protected Converter next;

    public Converter(Class type) {
        this.type = type;
    }

    protected abstract Object convert(String value) throws YamlException;

    public Converter setNext(Converter converter) {
        this.next = converter;
        return next;
    }

    public Object getType(String value) throws YamlException {
        Object convertedValue;

        try {
            convertedValue = convert(value);
        } catch (YamlException e) {
            if (next == null) {
                throw e;
            } else {
                convertedValue = next.getType(value);
            }
        }

        return convertedValue;
    }
}

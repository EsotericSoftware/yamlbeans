package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Field;

public interface PropertyNameConverter {

    PropertyNameConverter DEFAULT = new DefaultPropertyNameConverter();

    /**
     * Converts the Java field name into the YAML representation
     */
    String convertFieldToPropertyName (Field field);

    class DefaultPropertyNameConverter implements PropertyNameConverter {
        public String convertFieldToPropertyName (Field field) {
            return field.getName();
        }
    }
}

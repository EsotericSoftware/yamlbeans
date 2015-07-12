package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Field;

/**
 * Converts properties into upper case, for unit testing
 */
public class TestPropertyNameConverter implements PropertyNameConverter {
    public String convertFieldToPropertyName (Field field) {
        return field.getName().toUpperCase();
    }
}

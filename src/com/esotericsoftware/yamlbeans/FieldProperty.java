package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Field;

public class FieldProperty extends Property {

    private final Field field;

    public FieldProperty(Field field) {
        super(field.getDeclaringClass(), field.getName(), field.getType(), field.getGenericType());
        this.field = field;
    }

    public void set(Object object, Object value) throws Exception {
        if (object instanceof DeferredConstruction) {
            ((DeferredConstruction) object).storeProperty(this, value);
        } else {
            field.set(object, value);
        }
    }

    public Object get(Object object) throws Exception {
        return field.get(object);
    }
}

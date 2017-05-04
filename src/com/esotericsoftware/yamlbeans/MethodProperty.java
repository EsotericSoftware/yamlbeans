package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Method;

public class MethodProperty extends Property {

    private final Method setMethod;
    private final Method getMethod;

    public MethodProperty(String name, Method setMethod, Method getMethod) {
        super(getMethod.getDeclaringClass(), name, getMethod.getReturnType(), getMethod.getGenericReturnType());
        this.setMethod = setMethod;
        this.getMethod = getMethod;
    }

    public void set(Object object, Object value) throws Exception {
        if (object instanceof DeferredConstruction) {
            ((DeferredConstruction) object).storeProperty(this, value);
        } else {
            setMethod.invoke(object, value);
        }
    }

    public Object get(Object object) throws Exception {
        return getMethod.invoke(object);
    }
}

package com.esotericsoftware.yamlbeans;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public abstract class Property implements Comparable<Property> {

    private final Class declaringClass;
    private final String name;
    private final Class type;
    private final Class elementType;

    protected Property(Class declaringClass, String name, Class type, Type genericType) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.type = type;
        this.elementType = getElementTypeFromGenerics(genericType);
    }

    abstract public void set(Object object, Object value) throws Exception;

    abstract public Object get(Object object) throws Exception;

    public Class getDeclaringClass() {
        return declaringClass;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public Class getElementType() {
        return elementType;
    }

    private Class getElementTypeFromGenerics(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (isCollection(rawType) || isMap(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    return (Class) actualTypeArguments[actualTypeArguments.length - 1];
                }
            }
        }
        return null;
    }

    private boolean isMap(Type type) {
        return Map.class.isAssignableFrom((Class) type);
    }

    private boolean isCollection(Type type) {
        return Collection.class.isAssignableFrom((Class) type);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Property other = (Property) obj;
        if (declaringClass == null) {
            if (other.declaringClass != null) return false;
        } else if (!declaringClass.equals(other.declaringClass)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (type == null) {
            if (other.type != null) return false;
        } else if (!type.equals(other.type)) return false;
        if (elementType == null) {
            if (other.elementType != null) return false;
        } else if (!elementType.equals(other.elementType)) return false;
        return true;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Property o) {
        int comparison = name.compareTo(o.name);
        if (comparison != 0) {
            // Sort id and name above all other fields.
            if (name.equals("id")) return -1;
            if (o.name.equals("id")) return 1;
            if (name.equals("name")) return -1;
            if (o.name.equals("name")) return 1;
        }
        return comparison;
    }
}

/*
 * Copyright (c) 2008 Nathan Sweet
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.esotericsoftware.yamlbeans;

import com.esotericsoftware.yamlbeans.YamlConfig.ConstructorParameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility for dealing with beans and public fields.
 *
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
class Beans {

    private Beans() {
    }

    static public boolean isScalar(Class c) {
        return c.isPrimitive() || c == String.class || c == Integer.class || c == Boolean.class || c == Float.class
                || c == Long.class || c == Double.class || c == Short.class || c == Byte.class || c == Character.class;
    }

    static public DeferredConstruction getDeferredConstruction(Class type, YamlConfig config) {
        DeferredConstruction deferredConstruction = null;

        ConstructorParameters parameters = config.readConfig.constructorParameters.get(type);
        if (parameters != null) {
            deferredConstruction = new DeferredConstruction(parameters.constructor, parameters.parameterNames);
        }
        try {
            Class constructorProperties = Class.forName("java.beans.ConstructorProperties");

            for (Constructor typeConstructor : type.getConstructors()) {
                Annotation annotation = typeConstructor.getAnnotation(constructorProperties);
                if (annotation == null) {
                    continue;
                }

                String[] parameterNames = (String[]) constructorProperties.getMethod("value").invoke(annotation, (Object[]) null);
                deferredConstruction = new DeferredConstruction(typeConstructor, parameterNames);
                break;
            }
        } catch (Exception ignored) {
        }

        return deferredConstruction;
    }

    static public Object createObject(Class type, boolean privateConstructors) throws InvocationTargetException {
        // Use no-arg constructor.
        Constructor constructor = null;
        for (Constructor typeConstructor : type.getConstructors()) {
            if (typeConstructor.getParameterTypes().length == 0) {
                constructor = typeConstructor;
                break;
            }
        }

        if (constructor == null && privateConstructors) {
            // Try a private constructor.
            try {
                constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
            } catch (SecurityException ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Otherwise try to use a common implementation.
        if (constructor == null) {
            try {
                if (List.class.isAssignableFrom(type)) {
                    constructor = ArrayList.class.getConstructor(new Class[0]);
                } else if (Set.class.isAssignableFrom(type)) {
                    constructor = HashSet.class.getConstructor(new Class[0]);
                } else if (Map.class.isAssignableFrom(type)) {
                    constructor = HashMap.class.getConstructor(new Class[0]);
                }
            } catch (Exception ex) {
                throw new InvocationTargetException(ex, "Error getting constructor for class: " + type.getName());
            }
        }

        if (constructor == null)
            throw new InvocationTargetException(null, "Unable to find a no-arg constructor for class: " + type.getName());

        try {
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new InvocationTargetException(ex, "Error constructing instance of class: " + type.getName());
        }
    }

    static public Set<Property> getProperties(Class type, boolean beanProperties, boolean privateFields, YamlConfig config) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        Class[] noArgs = new Class[0], oneArg = new Class[1];
        Set<Property> properties = config.writeConfig.keepBeanPropertyOrder ? new LinkedHashSet() : new TreeSet();
        for (Field field : getAllFields(type)) {
            String name = field.getName();

            if (beanProperties) {
                DeferredConstruction deferredConstruction = getDeferredConstruction(type, config);
                boolean constructorProperty = deferredConstruction != null && deferredConstruction.hasParameter(name);

                String nameUpper = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                Method getMethod = null, setMethod = null;
                try {
                    oneArg[0] = field.getType();
                    setMethod = type.getMethod("set" + nameUpper, oneArg);
                } catch (Exception ignored) {
                }
                try {
                    getMethod = type.getMethod("get" + nameUpper, noArgs);
                } catch (Exception ignored) {
                }
                if (getMethod == null && (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))) {
                    try {
                        getMethod = type.getMethod("is" + nameUpper, noArgs);
                    } catch (Exception ignored) {
                    }
                }
                if (getMethod != null && (setMethod != null || constructorProperty)) {
                    properties.add(new MethodProperty(name, setMethod, getMethod));
                    continue;
                }
            }

            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            if (!Modifier.isPublic(modifiers) && !privateFields) {
                continue;
            }
            try {
                field.setAccessible(true);
            } catch (Exception ignored) {
            }
            properties.add(new FieldProperty(field));
        }
        return properties;
    }

    static private String toJavaIdentifier(String name) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isJavaIdentifierPart(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }
        
    static public Property getProperty(Class type, String name, boolean beanProperties, boolean privateFields, YamlConfig config) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name cannot be null or empty.");
        }
        name = toJavaIdentifier(name);

        if (beanProperties) {
            DeferredConstruction deferredConstruction = getDeferredConstruction(type, config);
            boolean constructorProperty = deferredConstruction != null && deferredConstruction.hasParameter(name);

            String nameUpper = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method getMethod = null;
            try {
                getMethod = type.getMethod("get" + nameUpper);
            } catch (Exception ignored) {
            }
            if (getMethod == null) {
                try {
                    getMethod = type.getMethod("is" + nameUpper);
                } catch (Exception ignored) {
                }
            }
            if (getMethod != null) {
                Method setMethod = null;
                try {
                    setMethod = type.getMethod("set" + nameUpper, getMethod.getReturnType());
                } catch (Exception ignored) {
                }
                if (getMethod != null && (setMethod != null || constructorProperty))
                    return new MethodProperty(name, setMethod, getMethod);
            }
        }

        for (Field field : getAllFields(type)) {
            if (!field.getName().equals(name)) continue;
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
            if (!Modifier.isPublic(modifiers) && !privateFields) continue;
            try {
                field.setAccessible(true);
            } catch (Exception ignored) {
            }
            return new FieldProperty(field);
        }
        return null;
    }

    static private ArrayList<Field> getAllFields(Class type) {
        ArrayList<Class> classes = new ArrayList();
        Class nextClass = type;
        while (nextClass != null && nextClass != Object.class) {
            classes.add(nextClass);
            nextClass = nextClass.getSuperclass();
        }
        ArrayList<Field> allFields = new ArrayList();
        for (int i = classes.size() - 1; i >= 0; i--) {
            Collections.addAll(allFields, classes.get(i).getDeclaredFields());
        }
        return allFields;
    }

}

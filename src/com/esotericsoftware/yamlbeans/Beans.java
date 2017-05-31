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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.esotericsoftware.yamlbeans.YamlConfig.ConstructorParameters;

/**
 * Utility for dealing with beans and public fields.
 *
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
class Beans {

	static private final String GETTER = "get";
	static private final String GETTER_BOOLEAN = "is";
	static private final String SETTER = "set";

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
		Constructor constructor = getNoArgConstructor(type);

		if (constructor == null && privateConstructors) {
			constructor = getPrivateConstructor(type);
		}
		if (constructor == null) {
			constructor = tryCommonImplementationConstructor(type);
		}

		if (constructor == null) {
			throw new InvocationTargetException(null, "Unable to find a no-arg constructor for class: " + type.getName());
		} else {
			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw new InvocationTargetException(e, "Error constructing instance of class: " + type.getName());
			}
		}
	}

	static public Set<Property> getProperties(Class type, boolean beanProperties, boolean privateFields, YamlConfig config) {
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null.");
		}
		Class[] noArgs = new Class[0];
		Class[] oneArg = new Class[1];

		Set<Property> properties;
		if (config.writeConfig.keepBeanPropertyOrder) {
			properties = new LinkedHashSet<Property>();
		} else {
			properties = new TreeSet<Property>();
		}

		for (Field field : getAllFields(type)) {
			String name = field.getName();

			if (beanProperties) {
				DeferredConstruction deferredConstruction = getDeferredConstruction(type, config);
				boolean constructorProperty = deferredConstruction != null && deferredConstruction.hasParameter(name);

				String upperName = getUpperName(name);
				Method getMethod = null;
				Method setMethod = null;

				try {
					oneArg[0] = field.getType();
					setMethod = type.getMethod(SETTER + upperName, oneArg);
				} catch (Exception ignored) {
				}
				try {
					getMethod = type.getMethod(GETTER + upperName, noArgs);
				} catch (Exception ignored) {
				}
				if (getMethod == null && isBooleanType(field)) {
					try {
						getMethod = type.getMethod(GETTER_BOOLEAN + upperName, noArgs);
					} catch (Exception ignored) {
					}
				}

				if (getMethod != null && (setMethod != null || constructorProperty)) {
					properties.add(new MethodProperty(name, setMethod, getMethod));
					continue;
				}
			}

			if (isProperField(field, privateFields)) {
				field.setAccessible(true);
				properties.add(new FieldProperty(field));
			}
		}

		return properties;
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

			String upperName = getUpperName(name);
			Method getMethod = null;
			Method setMethod = null;

			try {
				getMethod = type.getMethod(GETTER + upperName);
			} catch (Exception ignored) {
			}
			if (getMethod == null) {
				try {
					getMethod = type.getMethod(GETTER_BOOLEAN + upperName);
				} catch (Exception ignored) {
				}
			}

			if (getMethod != null) {
				try {
					setMethod = type.getMethod(SETTER + upperName, getMethod.getReturnType());
				} catch (Exception ignored) {
				}
				if (setMethod != null || constructorProperty) {
					return new MethodProperty(name, setMethod, getMethod);
				}
			}
		}

		FieldProperty fieldProperty = null;
		for (Field field : getAllFields(type)) {
			if (!field.getName().equals(name)) {
				continue;
			}

			if (isProperField(field, privateFields)) {
				field.setAccessible(true);
				fieldProperty = new FieldProperty(field);
				break;
			}
		}

		return fieldProperty;
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

	static private Constructor getNoArgConstructor(Class type) {
		Constructor noArgConstructor = null;

		for (Constructor typeConstructor : type.getConstructors()) {
			if (isNoArgConstructor(typeConstructor)) {
				noArgConstructor = typeConstructor;
				break;
			}
		}

		return noArgConstructor;
	}

	static private boolean isNoArgConstructor(Constructor constructor) {
		return constructor.getParameterTypes().length == 0;
	}

	static private Constructor getPrivateConstructor(Class type) {
		Constructor privateConstructor = null;

		try {
			privateConstructor = type.getDeclaredConstructor();
			privateConstructor.setAccessible(true);
		} catch (SecurityException ignored) {
		} catch (NoSuchMethodException ignored) {
		}

		return privateConstructor;
	}

	static private Constructor tryCommonImplementationConstructor(Class type) throws InvocationTargetException {
		Constructor commonImplementationConstructor = null;
		Class[] noArgs = new Class[0];

		try {
			if (List.class.isAssignableFrom(type)) {
				commonImplementationConstructor = ArrayList.class.getConstructor(noArgs);
			} else if (Set.class.isAssignableFrom(type)) {
				commonImplementationConstructor = HashSet.class.getConstructor(noArgs);
			} else if (Map.class.isAssignableFrom(type)) {
				commonImplementationConstructor = HashMap.class.getConstructor(noArgs);
			}
		} catch (Exception e) {
			throw new InvocationTargetException(e, "Error getting constructor for class: " + type.getName());
		}

		return commonImplementationConstructor;
	}

	static private ArrayList<Field> getAllFields(Class type) {
		ArrayList<Class> classes = new ArrayList<Class>();
		ArrayList<Field> allFields = new ArrayList<Field>();

		Class nextClass = type;
		while (nextClass != null && nextClass != Object.class) {
			classes.add(nextClass);
			nextClass = nextClass.getSuperclass();
		}

		for (int i = classes.size() - 1; i >= 0; i--) {
			Collections.addAll(allFields, classes.get(i).getDeclaredFields());
		}

		return allFields;
	}

	static private String getUpperName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	static private boolean isBooleanType(Field field) {
		return field.getType().equals(Boolean.class) || field.getType().equals(boolean.class);
	}

	static private boolean isProperField(Field field, boolean privateFields) {
		boolean isProper;

		int modifiers = field.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
			isProper = false;
		} else if (!Modifier.isPublic(modifiers) && !privateFields) {
			isProper = false;
		} else {
			isProper = true;
		}

		return isProper;
	}

	static public class MethodProperty extends Property {

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

	static public class FieldProperty extends Property {

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

	static public abstract class Property implements Comparable<Property> {

		private final Class declaringClass;
		private final String name;
		private final Class type;
		private final Class elementType;

		Property(Class declaringClass, String name, Class type, Type genericType) {
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
						final Type cType = actualTypeArguments[actualTypeArguments.length - 1];
						if (cType instanceof Class) {
							return (Class) cType;
						} else if (cType instanceof WildcardType) {
							WildcardType t = (WildcardType) cType;
							final Type bound = t.getUpperBounds()[0];
							return bound instanceof Class ? (Class) bound : null;
						} else if (cType instanceof ParameterizedType) {
							ParameterizedType t = (ParameterizedType) cType;
							final Type rt = t.getRawType();
							return rt instanceof Class ? (Class) rt : null;
						}
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

		public String toString() {
			return name;
		}
	}
}

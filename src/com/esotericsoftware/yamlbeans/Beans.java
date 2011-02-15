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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.esotericsoftware.yamlbeans.YamlConfig.ConstructorParameters;

/**
 * Utility for dealing with beans and public fields.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
class Beans {
	private Beans () {
	}

	static public boolean isScalar (Class c) {
		return c.isPrimitive() || c == String.class || c == Integer.class || c == Boolean.class || c == Float.class
			|| c == Long.class || c == Double.class || c == Short.class || c == Byte.class || c == Character.class;
	}

	static public DeferredConstruction getDeferredConstruction (Class type, YamlConfig config) {
		ConstructorParameters parameters = config.readConfig.constructorParameters.get(type);
		if (parameters != null) return new DeferredConstruction(parameters.constructor, parameters.parameterNames);
		try {
			Class constructorProperties = Class.forName("java.beans.ConstructorProperties");
			for (Constructor typeConstructor : type.getConstructors()) {
				Annotation annotation = typeConstructor.getAnnotation(constructorProperties);
				if (annotation == null) continue;
				String[] parameterNames = (String[])constructorProperties.getMethod("value").invoke(annotation, (Object[])null);
				return new DeferredConstruction(typeConstructor, parameterNames);
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	static private boolean canInitializeProperty (Class type, PropertyDescriptor property, YamlConfig config) {
		if (property.getWriteMethod() != null) return true;

		// Check if the property can be initialized through the constructor.
		DeferredConstruction deferredConstruction = getDeferredConstruction(type, config);
		if (deferredConstruction != null && deferredConstruction.hasParameter(property.getName())) return true;

		return false;
	}

	static public Object createObject (Class type, boolean privateConstructors) throws InvocationTargetException {
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

	static public Set<Property> getProperties (Class type, boolean beanProperties, boolean privateFields, YamlConfig config)
		throws IntrospectionException {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		Set<Property> properties = new TreeSet();
		if (beanProperties) {
			for (PropertyDescriptor property : Introspector.getBeanInfo(type).getPropertyDescriptors())
				if (property.getReadMethod() != null && canInitializeProperty(type, property, config))
					properties.add(new MethodProperty(type, property));
		}
		for (Field field : getAllFields(type)) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
			if (!Modifier.isPublic(modifiers)) {
				if (!privateFields) continue;
				field.setAccessible(true);
			}
			properties.add(new FieldProperty(field));
		}
		return properties;
	}

	static public Property getProperty (Class type, String name, boolean beanProperties, boolean privateFields, YamlConfig config)
		throws IntrospectionException {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (name == null || name.length() == 0) throw new IllegalArgumentException("name cannot be null or empty.");
		if (beanProperties) {
			for (PropertyDescriptor property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
				if (property.getName().equals(name)) {
					if (property.getReadMethod() != null && canInitializeProperty(type, property, config))
						return new MethodProperty(type, property);
					break;
				}
			}
		}
		for (Field field : getAllFields(type)) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
			if (!Modifier.isPublic(modifiers)) {
				if (!privateFields) continue;
				field.setAccessible(true);
			}
			if (field.getName().equals(name)) return new FieldProperty(field);
		}
		return null;
	}

	static private ArrayList<Field> getAllFields (Class type) {
		ArrayList<Field> allFields = new ArrayList();
		Class nextClass = type;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}
		return allFields;
	}

	static public class MethodProperty extends Property {
		private final PropertyDescriptor property;

		public MethodProperty (Class declaringClass, PropertyDescriptor property) {
			super(declaringClass, property.getName(), property.getPropertyType());
			this.property = property;
		}

		public void set (Object object, Object value) throws Exception {
			if (object instanceof DeferredConstruction) {
				((DeferredConstruction)object).storeProperty(this, value);
				return;
			}
			property.getWriteMethod().invoke(object, value);
		}

		public Object get (Object object) throws Exception {
			return property.getReadMethod().invoke(object);
		}
	}

	static public class FieldProperty extends Property {
		private final Field field;

		public FieldProperty (Field field) {
			super(field.getDeclaringClass(), field.getName(), field.getType());
			this.field = field;
		}

		public void set (Object object, Object value) throws Exception {
			if (object instanceof DeferredConstruction) {
				((DeferredConstruction)object).storeProperty(this, value);
				return;
			}
			field.set(object, value);
		}

		public Object get (Object object) throws Exception {
			return field.get(object);
		}
	}

	static public abstract class Property implements Comparable<Property> {
		private final Class declaringClass;
		private final String name;
		private final Class type;

		public Property (Class declaringClass, String name, Class type) {
			this.declaringClass = declaringClass;
			this.name = name;
			this.type = type;
		}

		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Property other = (Property)obj;
			if (declaringClass == null) {
				if (other.declaringClass != null) return false;
			} else if (!declaringClass.equals(other.declaringClass)) return false;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			if (type == null) {
				if (other.type != null) return false;
			} else if (!type.equals(other.type)) return false;
			return true;
		}

		public Class getDeclaringClass () {
			return declaringClass;
		}

		public Class getType () {
			return type;
		}

		public String getName () {
			return name;
		}

		public String toString () {
			return name;
		}

		public int compareTo (Property o) {
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

		abstract public void set (Object object, Object value) throws Exception;

		abstract public Object get (Object object) throws Exception;
	}
}

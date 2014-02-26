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

import com.esotericsoftware.yamlbeans.Beans.Property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/** Stores a constructor, parameters names, and property values so construction can be deferred until all property values are
 * known.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
class DeferredConstruction {
	private final Constructor constructor;
	private final String[] parameterNames;
	private final ParameterValue[] parameterValues;
	private final List<PropertyValue> propertyValues = new ArrayList(16);

	public DeferredConstruction (Constructor constructor, String[] parameterNames) {
		this.constructor = constructor;
		this.parameterNames = parameterNames;
		parameterValues = new ParameterValue[parameterNames.length];
	}

	public Object construct () throws InvocationTargetException {
		try {
			Object[] parameters = new Object[parameterValues.length];
			int i = 0;
			for (ParameterValue parameter : parameterValues) {
				if (parameter == null)
					throw new InvocationTargetException(new YamlException("Missing constructor property: " + parameterNames[i]));
				parameters[i++] = parameter.value;
			}
			Object object = constructor.newInstance(parameters);
			for (PropertyValue propertyValue : propertyValues)
				propertyValue.property.set(object, propertyValue.value);
			return object;
		} catch (Exception ex) {
			throw new InvocationTargetException(ex, "Error constructing instance of class: "
				+ constructor.getDeclaringClass().getName());
		}
	}

	public void storeProperty (Property property, Object value) {
		int index = 0;
		for (String name : parameterNames) {
			if (property.getName().equals(name)) {
				ParameterValue parameterValue = new ParameterValue();
				parameterValue.value = value;
				parameterValues[index] = parameterValue;
				return;
			}
			index++;
		}

		PropertyValue propertyValue = new PropertyValue();
		propertyValue.property = property;
		propertyValue.value = value;
		propertyValues.add(propertyValue);
	}

	public boolean hasParameter (String name) {
		for (String s : parameterNames)
			if (s.equals(name)) return true;
		return false;
	}

	static class PropertyValue {
		Property property;
		Object value;
	}

	static class ParameterValue {
		Object value;
	}
}

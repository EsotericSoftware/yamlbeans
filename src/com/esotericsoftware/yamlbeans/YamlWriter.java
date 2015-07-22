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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.esotericsoftware.yamlbeans.Beans.Property;
import com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName;
import com.esotericsoftware.yamlbeans.YamlConfig.WriteConfig;
import com.esotericsoftware.yamlbeans.emitter.Emitter;
import com.esotericsoftware.yamlbeans.emitter.EmitterException;
import com.esotericsoftware.yamlbeans.parser.AliasEvent;
import com.esotericsoftware.yamlbeans.parser.DocumentEndEvent;
import com.esotericsoftware.yamlbeans.parser.DocumentStartEvent;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.MappingStartEvent;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;
import com.esotericsoftware.yamlbeans.parser.SequenceStartEvent;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

/** Serializes Java objects as YAML.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class YamlWriter {
	private final YamlConfig config;
	private final Emitter emitter;
	private boolean started;

	private Map<Class, Object> defaultValuePrototypes = new IdentityHashMap();

	private final List queuedObjects = new ArrayList();
	private final Map<Object, Integer> referenceCount = new IdentityHashMap();
	private final Map<Object, String> anchoredObjects = new HashMap();
	private int nextAnchor = 1;
	private boolean isRoot;

	public YamlWriter (Writer writer) {
		this(writer, new YamlConfig());
	}

	public YamlWriter (Writer writer, YamlConfig config) {
		this.config = config;
		emitter = new Emitter(writer, config.writeConfig.emitterConfig);
	}

	public void setAlias (Object object, String alias) {
		anchoredObjects.put(object, alias);
	}

	public void write (Object object) throws YamlException {
		if (config.writeConfig.autoAnchor) {
			countObjectReferences(object);
			queuedObjects.add(object);
			return;
		}
		writeInternal(object);
	}

	public YamlConfig getConfig () {
		return config;
	}

	private void writeInternal (Object object) throws YamlException {
		try {
			if (!started) {
				emitter.emit(Event.STREAM_START);
				started = true;
			}
			emitter.emit(new DocumentStartEvent(config.writeConfig.explicitFirstDocument, null, null));
			isRoot = true;
			writeValue(object, config.writeConfig.writeRootTags ? null : object.getClass(), null, null);
			emitter.emit(new DocumentEndEvent(config.writeConfig.explicitEndDocument));
		} catch (EmitterException ex) {
			throw new YamlException("Error writing YAML.", ex);
		} catch (IOException ex) {
			throw new YamlException("Error writing YAML.", ex);
		}
	}

	/** Returns the YAML emitter, which allows the YAML output to be configured. */
	public Emitter getEmitter () {
		return emitter;
	}

	/** Writes any buffered objects, then resets the list of anchored objects.
	 * @see WriteConfig#setAutoAnchor(boolean) */
	public void clearAnchors () throws YamlException {
		for (Object object : queuedObjects)
			writeInternal(object);
		queuedObjects.clear();
		referenceCount.clear();
		nextAnchor = 1;
	}

	/** Finishes writing any buffered output and releases all resources.
	 * @throws YamlException If the buffered output could not be written or the writer could not be closed. */
	public void close () throws YamlException {
		clearAnchors();
		defaultValuePrototypes.clear();
		try {
			emitter.emit(Event.STREAM_END);
			emitter.close();
		} catch (EmitterException ex) {
			throw new YamlException(ex);
		} catch (IOException ex) {
			throw new YamlException(ex);
		}
	}

	private void writeValue (Object object, Class fieldClass, Class elementType, Class defaultType) throws EmitterException,
		IOException, YamlException {
		boolean isRoot = this.isRoot;
		this.isRoot = false;

		if (object == null) {
			emitter.emit(new ScalarEvent(null, null, new boolean[] {true, true}, null, (char)0));
			return;
		}

		Class valueClass = object.getClass();
		boolean unknownType = fieldClass == null;
		if (unknownType) fieldClass = valueClass;

		if (object instanceof Enum) {
			emitter.emit(new ScalarEvent(null, null, new boolean[] {true, true}, ((Enum)object).name(), (char)0));
			return;
		}

		String anchor = null;
		if (!Beans.isScalar(valueClass)) {
			anchor = anchoredObjects.get(object);
			if (config.writeConfig.autoAnchor) {
				Integer count = referenceCount.get(object);
				if (count == null) {
					emitter.emit(new AliasEvent(anchoredObjects.get(object)));
					return;
				}
				if (count > 1) {
					referenceCount.remove(object);
					if (anchor == null) {
						anchor = String.valueOf(nextAnchor++);
						anchoredObjects.put(object, anchor);
					}
				}
			}
		}

		String tag = null;
		boolean showTag = false;
		if ((unknownType || valueClass != fieldClass || config.writeConfig.writeClassName == WriteClassName.ALWAYS) && config.writeConfig.writeClassName != WriteClassName.NEVER) {
			showTag = true;
			if ((unknownType || fieldClass == List.class) && valueClass == ArrayList.class) showTag = false;
			if ((unknownType || fieldClass == Map.class) && valueClass == HashMap.class) showTag = false;
			if (fieldClass == Set.class && valueClass == HashSet.class) showTag = false;
			if (valueClass == defaultType) showTag = false;
			if (showTag) {
				tag = config.classNameToTag.get(valueClass.getName());
				if (tag == null) tag = valueClass.getName();
			}
		}

		for (Entry<Class, ScalarSerializer> entry : config.scalarSerializers.entrySet()) {
			if (entry.getKey().isAssignableFrom(valueClass)) {
				ScalarSerializer serializer = entry.getValue();
				emitter.emit(new ScalarEvent(null, tag, new boolean[] {tag == null, tag == null}, serializer.write(object), (char)0));
				return;
			}
		}

		if (Beans.isScalar(valueClass)) {
			char style = 0;
			String string = String.valueOf(object);
			if (valueClass == String.class) {
				try {
					Float.parseFloat(string);
					style = '\'';
				} catch (NumberFormatException ignored) {
				}
			}
			emitter.emit(new ScalarEvent(null, tag, new boolean[] {true, true}, string, style));
			return;
		}

		if (object instanceof Collection) {
			emitter.emit(new SequenceStartEvent(anchor, tag, !showTag, false));
			for (Object item : (Collection)object) {
				if (isRoot && !config.writeConfig.writeRootElementTags) elementType = item.getClass();
				writeValue(item, elementType, null, null);
			}
			emitter.emit(Event.SEQUENCE_END);
			return;
		}

		if (object instanceof Map) {
			emitter.emit(new MappingStartEvent(anchor, tag, !showTag, false));
			for (Object item : ((Map)object).entrySet()) {
				Entry entry = (Entry)item;
				writeValue(entry.getKey(), null, null, null);
				if (isRoot && !config.writeConfig.writeRootElementTags) elementType = entry.getValue().getClass();
				writeValue(entry.getValue(), elementType, null, null);
			}
			emitter.emit(Event.MAPPING_END);
			return;
		}

		if (fieldClass.isArray()) {
			elementType = fieldClass.getComponentType();
			emitter.emit(new SequenceStartEvent(anchor, null, true, false));
			for (int i = 0, n = Array.getLength(object); i < n; i++)
				writeValue(Array.get(object, i), elementType, null, null);
			emitter.emit(Event.SEQUENCE_END);
			return;
		}

		// Value must be a bean.

		Object prototype = null;
		if (!config.writeConfig.writeDefaultValues && valueClass != Class.class) {
			prototype = defaultValuePrototypes.get(valueClass);
			if (prototype == null && Beans.getDeferredConstruction(valueClass, config) == null) {
				try {
					prototype = Beans.createObject(valueClass, config.privateConstructors);
				} catch (InvocationTargetException ex) {
					throw new YamlException("Error creating object prototype to determine default values.", ex);
				}
				defaultValuePrototypes.put(valueClass, prototype);
			}
		}

		Set<Property> properties = Beans.getProperties(valueClass, config.beanProperties, config.privateFields, config);
		emitter.emit(new MappingStartEvent(anchor, tag, !showTag, false));
		for (Property property : properties) {
			try {
				Object propertyValue = property.get(object);
				if (prototype != null) {
					// Don't output properties that have the default value for the prototype.
					Object prototypeValue = property.get(prototype);
					if (propertyValue == null && prototypeValue == null) continue;
					if (propertyValue != null && prototypeValue != null && prototypeValue.equals(propertyValue)) continue;
				}
				emitter.emit(new ScalarEvent(null, null, new boolean[] {true, true}, property.getName(), (char)0));
				Class propertyElementType = config.propertyToElementType.get(property);
				Class propertyDefaultType = config.propertyToDefaultType.get(property);
				writeValue(propertyValue, property.getType(), propertyElementType, propertyDefaultType);
			} catch (Exception ex) {
				throw new YamlException("Error getting property '" + property + "' on class: " + valueClass.getName(), ex);
			}
		}
		emitter.emit(Event.MAPPING_END);
	}

	private void countObjectReferences (Object object) throws YamlException {
		if (object == null || Beans.isScalar(object.getClass())) return;

		// Count every reference to the object, but follow its own references the first time it is encountered.
		Integer count = referenceCount.get(object);
		if (count != null) {
			referenceCount.put(object, count + 1);
			return;
		}
		referenceCount.put(object, 1);

		if (object instanceof Collection) {
			for (Object item : (Collection)object)
				countObjectReferences(item);
			return;
		}

		if (object instanceof Map) {
			for (Object value : ((Map)object).values())
				countObjectReferences(value);
			return;
		}

		if (object.getClass().isArray()) {
			for (int i = 0, n = Array.getLength(object); i < n; i++)
				countObjectReferences(Array.get(object, i));
			return;
		}

		// Value must be an object.

		Set<Property> properties = Beans.getProperties(object.getClass(), config.beanProperties, config.privateFields, config);
		for (Property property : properties) {
			if (Beans.isScalar(property.getType())) continue;
			Object propertyValue;
			try {
				propertyValue = property.get(object);
			} catch (Exception ex) {
				throw new YamlException("Error getting property '" + property + "' on class: " + object.getClass().getName(), ex);
			}
			countObjectReferences(propertyValue);
		}
	}
}

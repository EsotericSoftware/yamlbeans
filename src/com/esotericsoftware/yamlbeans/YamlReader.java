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

import static com.esotericsoftware.yamlbeans.parser.EventType.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.yamlbeans.Beans.Property;
import com.esotericsoftware.yamlbeans.parser.AliasEvent;
import com.esotericsoftware.yamlbeans.parser.CollectionStartEvent;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;
import com.esotericsoftware.yamlbeans.parser.Parser.ParserException;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer.TokenizerException;

/**
 * Deserializes Java objects from YAML.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
public class YamlReader {
	private final YamlConfig config;
	Parser parser;
	private final Map<String, Object> anchors = new HashMap();

	public YamlReader (Reader reader) {
		this(reader, new YamlConfig());
	}

	public YamlReader (Reader reader, YamlConfig config) {
		this.config = config;
		parser = new Parser(reader, config.readConfig.defaultVersion);
	}

	public YamlReader (String yaml) {
		this(new StringReader(yaml));
	}

	public YamlReader (String yaml, YamlConfig config) {
		this(new StringReader(yaml), config);
	}

	public YamlConfig getConfig () {
		return config;
	}

	public void close () throws IOException {
		parser.close();
		anchors.clear();
	}

	/**
	 * Reads the next YAML document and deserializes it into an object. The type of object is defined by the YAML tag. If there is
	 * no YAML tag, the object will be an {@link ArrayList}, {@link HashMap}, or String.
	 */
	public Object read () throws YamlException {
		return read(null);
	}

	/**
	 * Reads an object of the specified type from YAML.
	 * @param type The type of object to read. If null, behaves the same as {{@link #read()}.
	 */
	public <T> T read (Class<T> type) throws YamlException {
		return read(type, null);
	}

	/**
	 * Reads an array, Map, List, or Collection object of the specified type from YAML, using the specified element type.
	 * @param type The type of object to read. If null, behaves the same as {{@link #read()}.
	 */
	public <T> T read (Class<T> type, Class elementType) throws YamlException {
		try {
			while (true) {
				Event event = parser.getNextEvent();
				if (event == null) return null;
				if (event.type == STREAM_END) return null;
				if (event.type == DOCUMENT_START) break;
			}
			return (T)readValue(type, elementType, null);
		} catch (ParserException ex) {
			throw new YamlException("Error parsing YAML.", ex);
		} catch (TokenizerException ex) {
			throw new YamlException("Error tokenizing YAML.", ex);
		}
	}

	private Object readValue (Class type, Class elementType, Class defaultType) throws YamlException, ParserException,
		TokenizerException {
		String tag = null, anchor = null;
		Event event = parser.peekNextEvent();

		switch (event.type) {
		case ALIAS:
			parser.getNextEvent();
			anchor = ((AliasEvent)event).anchor;
			Object value = anchors.get(anchor);
			if (value == null) throw new YamlReaderException("Unknown anchor: " + anchor);
			return value;
		case MAPPING_START:
		case SEQUENCE_START:
			tag = ((CollectionStartEvent)event).tag;
			anchor = ((CollectionStartEvent)event).anchor;
			break;
		case SCALAR:
			tag = ((ScalarEvent)event).tag;
			anchor = ((ScalarEvent)event).anchor;
			break;
		}

		if (tag != null) {
			type = config.tagToClass.get(tag);
			if (type == null) {
				try {
					if (config.readConfig.classLoader != null)
						type = Class.forName(tag, true, config.readConfig.classLoader);
					else
						type = Class.forName(tag);
				} catch (ClassNotFoundException ex) {
					throw new YamlReaderException("Unable to find class specified by tag: " + tag);
				}
			}
		} else if (defaultType != null) {
			type = defaultType;
		}

		return readValueInternal(type, elementType, anchor);
	}

	private Object readValueInternal (Class type, Class elementType, String anchor) throws YamlException, ParserException,
		TokenizerException {
		if (type == null || type == Object.class) {
			Event event = parser.peekNextEvent();
			switch (event.type) {
			case MAPPING_START:
				type = HashMap.class;
				break;
			case SCALAR:
				type = String.class;
				break;
			case SEQUENCE_START:
				type = ArrayList.class;
				break;
			default:
				throw new YamlReaderException("Expected scalar, sequence, or mapping but found: " + event.type);
			}
		}

		if (type == String.class) {
			Event event = parser.getNextEvent();
			if (event.type != SCALAR) throw new YamlReaderException("Expected scalar for String type but found: " + event.type);
			String value = ((ScalarEvent)event).value;
			if (anchor != null) anchors.put(anchor, value);
			return value;
		}

		if (Beans.isScalar(type)) {
			Event event = parser.getNextEvent();
			if (event.type != SCALAR)
				throw new YamlReaderException("Expected scalar for primitive type '" + type.getClass() + "' but found: " + event.type);
			String value = ((ScalarEvent)event).value;
			try {
				Object convertedValue;
				if (type == String.class) {
					convertedValue = value;
				} else if (type == Integer.TYPE || type == Integer.class) {
					convertedValue = Integer.valueOf(value);
				} else if (type == Boolean.TYPE || type == Boolean.class) {
					convertedValue = Boolean.valueOf(value);
				} else if (type == Float.TYPE || type == Float.class) {
					convertedValue = Float.valueOf(value);
				} else if (type == Double.TYPE || type == Double.class) {
					convertedValue = Double.valueOf(value);
				} else if (type == Long.TYPE || type == Long.class) {
					convertedValue = Long.valueOf(value);
				} else if (type == Short.TYPE || type == Short.class) {
					convertedValue = Short.valueOf(value);
				} else if (type == Character.TYPE || type == Character.class) {
					convertedValue = value.charAt(0);
				} else if (type == Byte.TYPE || type == Byte.class) {
					convertedValue = Byte.valueOf(value);
				} else
					throw new YamlException("Unknown field type.");
				if (anchor != null) anchors.put(anchor, convertedValue);
				return convertedValue;
			} catch (Exception ex) {
				throw new YamlReaderException("Unable to convert value to required type \"" + type + "\": " + value, ex);
			}
		}

		if (Enum.class.isAssignableFrom(type)) {
			Event event = parser.getNextEvent();
			if (event.type != SCALAR) throw new YamlReaderException("Expected scalar for enum type but found: " + event.type);
			String enumValueName = ((ScalarEvent)event).value;
			if (enumValueName.length() == 0) return null;
			try {
				return Enum.valueOf(type, enumValueName);
			} catch (Exception ex) {
				throw new YamlReaderException("Unable to find enum value '" + enumValueName + "' for enum class: " + type.getName());
			}
		}

		for (Entry<Class, ScalarSerializer> entry : config.scalarSerializers.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				ScalarSerializer serializer = entry.getValue();
				Event event = parser.getNextEvent();
				if (event.type != SCALAR)
					throw new YamlReaderException("Expected scalar for type '" + type + "' to be deserialized by scalar serializer '"
						+ serializer.getClass().getName() + "' but found: " + event.type);
				Object value = serializer.read(((ScalarEvent)event).value);
				if (anchor != null) anchors.put(anchor, value);
				return value;
			}
		}

		Event event = parser.peekNextEvent();
		switch (event.type) {
		case MAPPING_START: {
			// Must be a map or an object.
			event = parser.getNextEvent();
			Object object;
			try {
				object = createObject(type);
			} catch (InvocationTargetException ex) {
				throw new YamlReaderException("Error creating object.", ex);
			}
			if (anchor != null) anchors.put(anchor, object);
			while (true) {
				if (parser.peekNextEvent().type == MAPPING_END) {
					parser.getNextEvent();
					break;
				}
				Object key = readValue(null, null, null);
				if (object instanceof Map) {
					// Add to map.
					((Map)object).put(key, readValue(elementType, null, null));
				} else {
					// Set field on object.
					try {
						Property property = Beans.getProperty(type, (String)key, config.beanProperties, config.privateFields, config);
						if (property == null)
							throw new YamlReaderException("Unable to find property '" + key + "' on class: " + type.getName());
						Class propertyElementType = config.propertyToElementType.get(property);
						Class propertyDefaultType = config.propertyToDefaultType.get(property);
						property.set(object, readValue(property.getType(), propertyElementType, propertyDefaultType));
					} catch (Exception ex) {
						if (ex instanceof YamlReaderException) throw (YamlReaderException)ex;
						throw new YamlReaderException("Error setting property '" + key + "' on class: " + type.getName(), ex);
					}
				}
			}
			if (object instanceof DeferredConstruction) {
				try {
					return ((DeferredConstruction)object).construct();
				} catch (InvocationTargetException ex) {
					throw new YamlReaderException("Error creating object.", ex);
				}
			}
			return object;
		}
		case SEQUENCE_START: {
			// Must be a collection or an array.
			event = parser.getNextEvent();
			Collection collection;
			if (Collection.class.isAssignableFrom(type)) {
				try {
					collection = (Collection)Beans.createObject(type);
				} catch (InvocationTargetException ex) {
					throw new YamlReaderException("Error creating object.", ex);
				}
			} else if (type.isArray()) {
				collection = new ArrayList();
				elementType = type.getComponentType();
			} else
				throw new YamlReaderException("A sequence is not a valid value for the type: " + type.getName());
			if (!type.isArray() && anchor != null) anchors.put(anchor, collection);
			while (true) {
				event = parser.peekNextEvent();
				if (event.type == SEQUENCE_END) {
					parser.getNextEvent();
					break;
				}
				collection.add(readValue(elementType, null, null));
			}
			if (!type.isArray()) return collection;
			Object array = Array.newInstance(elementType, collection.size());
			int i = 0;
			for (Object object : collection)
				Array.set(array, i++, object);
			if (anchor != null) anchors.put(anchor, array);
			return array;
		}
		case SCALAR:
			// Interpret an empty scalar as null.
			if (((ScalarEvent)event).value.length() == 0) {
				event = parser.getNextEvent();
				return null;
			}
			// Fall through.
		default:
			throw new YamlReaderException("Expected data for a " + type.getName() + " field but found: " + event.type);
		}
	}

	/**
	 * Returns a new object of the requested type.
	 */
	protected Object createObject (Class type) throws InvocationTargetException {
		// Use deferred construction if a non-zero-arg constructor is available.
		DeferredConstruction deferredConstruction = Beans.getDeferredConstruction(type, config);
		if (deferredConstruction != null) return deferredConstruction;
		return Beans.createObject(type);
	}

	public class YamlReaderException extends YamlException {
		public YamlReaderException (String message, Throwable cause) {
			super("Line " + parser.getLineNumber() + ", column " + parser.getColumn() + ": " + message, cause);
		}

		public YamlReaderException (String message) {
			this(message, null);
		}
	}

	public static void main (String[] args) throws Exception {
		YamlReader reader = new YamlReader(new FileReader("test/test.yml"));
		System.out.println(reader.read());
	}
}

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
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.yamlbeans.Beans.Property;
import com.esotericsoftware.yamlbeans.parser.AliasEvent;
import com.esotericsoftware.yamlbeans.parser.CollectionStartEvent;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.EventType;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.Parser.ParserException;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer.TokenizerException;

/** Deserializes Java objects from YAML.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class YamlReader implements AutoCloseable {
	private final YamlConfig config;
	Parser parser;
	private final Map<String, Object> anchorMap = new HashMap();

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

	/** Return the object with the given alias, or null. This is only valid after objects have been read and before
	 * {@link #close()} */
	public Object get (String alias) {
		return anchorMap.get(alias);
	}

	private void addAnchor (String key, Object value) {
		if (config.readConfig.anchors) anchorMap.put(key, value);
	}

	public void close () throws IOException {
		parser.close();
		anchorMap.clear();
	}

	/** Reads the next YAML document and deserializes it into an object. The type of object is defined by the YAML tag. If there is
	 * no YAML tag, the object will be an {@link ArrayList}, {@link HashMap}, or String.
	 * <p>
	 * This method creates and configures an instance of a class specified by the YAML data, so should be used only with YAML data
	 * from a trusted source. */
	public Object read () throws YamlException {
		return read(null);
	}

	/** Reads an object of the specified type from YAML.
	 * @param type The type of object to read. If null, behaves the same as {{@link #read()}.
	 * @throws YamlReaderException if the YAML data specifies a type that is incompatible with the specified type. */
	public <T> T read (Class<T> type) throws YamlException {
		return read(type, null);
	}

	/** Reads an array, Map, List, or Collection object of the specified type from YAML, using the specified element type.
	 * @param type The type of object to read. If null, behaves the same as {{@link #read()}.
	 * @throws YamlReaderException if the YAML data specifies a type that is incompatible with the specified type. */
	public <T> T read (Class<T> type, Class elementType) throws YamlException {
		anchorMap.clear();
		try {
			while (true) {
				Event event = parser.getNextEvent();
				if (event == null) return null;
				if (event.type == STREAM_END) return null;
				if (event.type == DOCUMENT_START) break;
			}
			Object object = readValue(type, elementType, null);
			parser.getNextEvent(); // consume it(DOCUMENT_END)
			return (T)object;
		} catch (ParserException ex) {
			throw new YamlException("Error parsing YAML.", ex);
		} catch (TokenizerException ex) {
			throw new YamlException("Error tokenizing YAML.", ex);
		}
	}

	/** Returns an iterator that reads all documents from YAML into objects.
	 * @param type The type of object to read. If null, behaves the same as {{@link #read()}. */
	public <T> Iterator<T> readAll (final Class<T> type) {
		return new Iterator<T>() {
			public boolean hasNext () {
				Event event = parser.peekNextEvent();
				return event != null && event.type != STREAM_END;
			}

			public T next () {
				try {
					return read(type);
				} catch (YamlException ex) {
					throw new RuntimeException("Error reading YAML document for iterator.", ex);
				}
			}

			public void remove () {
				throw new UnsupportedOperationException();
			}
		};
	}

	/** Reads an object from the YAML. Can be overidden to take some action for any of the objects returned.
	 * @param type May be null.
	 * @throws YamlReaderException if the YAML data specifies a type that is incompatible with the specified type. */
	protected Object readValue (Class type, Class elementType, Class defaultType)
		throws YamlException, ParserException, TokenizerException {
		String tag = null, anchor = null;
		Event event = parser.peekNextEvent();

		switch (event.type) {
		case ALIAS:
			parser.getNextEvent();
			anchor = ((AliasEvent)event).anchor;
			Object value = anchorMap.get(anchor);
			if (value == null && config.readConfig.anchors) throw new YamlReaderException("Unknown anchor: " + anchor);
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
		default:
		}

		return readValueInternal(this.chooseType(tag, defaultType, type), elementType, anchor);
	}

	private Class<?> chooseType (String tag, Class<?> defaultType, Class<?> providedType) throws YamlReaderException {
		if (tag != null && config.readConfig.classTags) {
			Class<?> userConfiguredByTag = config.tagToClass.get(tag);
			if (userConfiguredByTag != null) return userConfiguredByTag;

			ClassLoader classLoader = (config.readConfig.classLoader == null ? this.getClass().getClassLoader()
				: config.readConfig.classLoader);

			tag = tag.replace("!", "");
			try {
				Class<?> loadedFromTag = findTagClass(tag, classLoader);
				if (loadedFromTag != null) {
					if (providedType != null && !providedType.isAssignableFrom(loadedFromTag)) {
						throw new YamlReaderException("Class specified by tag is incompatible with expected type: "
							+ loadedFromTag.getName() + " (expected " + providedType.getName() + ")");
					}
					return loadedFromTag;
				}
			} catch (ClassNotFoundException e) {
				throw new YamlReaderException("Unable to find class specified by tag: " + tag);
			}
		}

		if (defaultType != null) return defaultType;

		return providedType; // May be null.
	}

	/** Used during reading when a tag is present, and {@link YamlConfig#setClassTag(String, Class)} was not used for that tag.
	 * Attempts to load the class corresponding to that tag.
	 * <p>
	 * If this returns a non-null Class, that will be used as the deserialization type regardless of whether a type was explicitly
	 * asked for or if a default type exists.
	 * <p>
	 * If this returns null, no guidance will be provided by the tag and we will fall back to the default type or a requested
	 * target type, if any exist.
	 * <p>
	 * If this throws a ClassNotFoundException, parsing will fail.
	 * <p>
	 * The default implementation is simply {@code
	 * Class.forName(tag, true, classLoader);
	 * } and never returns null.
	 * <p>
	 * You can override this to handle cases where you do not want to respect the type tags found in a document, eg if they were
	 * output by another program using classes that do not exist on your classpath. */
	protected Class<?> findTagClass (String tag, ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(tag, true, classLoader);
	}

	/**
	 * Reads a scalar value from the parser and converts it to the specified type.
	 * @param type The type to convert the scalar to.
	 * @param anchor The anchor of the scalar, or null.
	 * @return The converted scalar value.
	 * @throws YamlException
	 * @throws ParserException
	 */
	private Object readScalarValue(Class<?> type, String anchor) throws YamlException, ParserException {
		Event event = parser.getNextEvent();
		if (event.type != SCALAR) {
			throw new YamlReaderException("Expected scalar for primitive type '" + type.getClass() + "' but found: " + event.type);
		}

		String value = ((ScalarEvent) event).value;
		try {
			Object convertedValue;
			if (value == null) {
				convertedValue = null;
			} else if (type == String.class) {
				convertedValue = value;
			} else if (type == Integer.TYPE || type == Integer.class) {
				convertedValue = Integer.decode(value);
			} else if (type == Boolean.TYPE || type == Boolean.class) {
				convertedValue = Boolean.valueOf(value);
			} else if (type == Float.TYPE || type == Float.class) {
				convertedValue = Float.valueOf(value);
			} else if (type == Double.TYPE || type == Double.class) {
				convertedValue = Double.valueOf(value);
			} else if (type == Long.TYPE || type == Long.class) {
				convertedValue = Long.decode(value);
			} else if (type == Short.TYPE || type == Short.class) {
				convertedValue = Short.decode(value);
			} else if (type == Character.TYPE || type == Character.class) {
				convertedValue = value.charAt(0);
			} else if (type == Byte.TYPE || type == Byte.class) {
				convertedValue = Byte.decode(value);
			} else {
				throw new YamlException("Unknown field type.");
			}
			if (anchor != null) {
				addAnchor(anchor, convertedValue);
			}
			return convertedValue;
		} catch (Exception ex) {
			throw new YamlReaderException("Unable to convert value to required type \"" + type + "\": " + value, ex);
		}
	}

	private Object readValueInternal (Class type, Class elementType, String anchor)
		throws YamlException, ParserException, TokenizerException {
		if (type == null || type == Object.class) {
			Event event = parser.peekNextEvent();
			switch (event.type) {
			case MAPPING_START:
				type = LinkedHashMap.class;
				break;
			case SCALAR:
				if (config.readConfig.guessNumberTypes) {
					String value = ((ScalarEvent)event).value;
					if (value != null) {
						Number number = valueConvertedNumber(value);
						if (number != null) {
							if (anchor != null) addAnchor(anchor, number);
							parser.getNextEvent();
							return number;
						}
					}
				}
				type = String.class;
				break;
			case SEQUENCE_START:
				type = ArrayList.class;
				break;
			default:
				throw new YamlReaderException("Expected scalar, sequence, or mapping but found: " + event.type);
			}
		}

		if (Beans.isScalar(type)) {
			return readScalarValue(type, anchor);
		}

		for (Entry<Class, ScalarSerializer> entry : config.scalarSerializers.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				ScalarSerializer serializer = entry.getValue();
				Event event = parser.getNextEvent();
				if (event.type != SCALAR) throw new YamlReaderException("Expected scalar for type '" + type
					+ "' to be deserialized by scalar serializer '" + serializer.getClass().getName() + "' but found: " + event.type);
				Object value = serializer.read(((ScalarEvent)event).value);
				if (anchor != null) addAnchor(anchor, value);
				return value;
			}
		}

		if (Enum.class.isAssignableFrom(type)) {
			Event event = parser.getNextEvent();
			if (event.type != SCALAR) throw new YamlReaderException("Expected scalar for enum type but found: " + event.type);
			String enumValueName = ((ScalarEvent)event).value;
			if (enumValueName == null) return null;
			try {
				return Enum.valueOf(type, enumValueName);
			} catch (Exception ex) {
				throw new YamlReaderException("Unable to find enum value '" + enumValueName + "' for enum class: " + type.getName());
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
			if (anchor != null) addAnchor(anchor, object);
			ArrayList keys = new ArrayList();
			while (true) {
				if (parser.peekNextEvent().type == MAPPING_END) {
					parser.getNextEvent();
					break;
				}
				Object key = readValue(null, null, null);
				// Explicit key/value pairs (using "? key\n: value\n") will come back as a map.
				boolean isExplicitKey = key instanceof Map;
				Object value = null;
				if (isExplicitKey) {
					Entry nameValuePair = (Entry)((Map)key).entrySet().iterator().next();
					key = nameValuePair.getKey();
					value = nameValuePair.getValue();
				}
				if (object instanceof Map) {
					// Add to map.
					if (config.tagSuffix != null) {
						Event nextEvent = parser.peekNextEvent();
						switch (nextEvent.type) {
						case MAPPING_START:
						case SEQUENCE_START:
							((Map)object).put(key + config.tagSuffix, ((CollectionStartEvent)nextEvent).tag);
							break;
						case SCALAR:
							((Map)object).put(key + config.tagSuffix, ((ScalarEvent)nextEvent).tag);
							break;
						}
					}
					if (!isExplicitKey) value = readValue(elementType, null, null);
					if (!config.allowDuplicates && ((Map)object).containsKey(key)) {
						throw new YamlReaderException("Duplicate key found '" + key + "'");
					}
					if (config.readConfig.autoMerge && "<<".equals(key) && value != null)
						mergeMap((Map)object, value);
					else
						((Map)object).put(key, value);
				} else {
					// Set field on object.
					try {
						if (!config.allowDuplicates && keys.contains(key)) {
							throw new YamlReaderException("Duplicate key found '" + key + "'");
						}
						keys.add(key);

						Property property = Beans.getProperty(type, (String)key, config.beanProperties, config.privateFields, config);
						if (property == null) {
							if (config.readConfig.ignoreUnknownProperties) {
								// if next event is sequence, mapping... start, go though all of it until
								// corresponding sequence, mapping... end
								Event nextEvent = parser.peekNextEvent();
								EventType nextType = nextEvent.type;
								if (nextType == SEQUENCE_START || nextType == MAPPING_START) {
									skipRange();
								} else {
									// go though the next event, because this is a value of missing property
									parser.getNextEvent();
								}

								continue;
							}
							throw new YamlReaderException("Unable to find property '" + key + "' on class: " + type.getName());
						}
						Class propertyElementType = config.propertyToElementType.get(property);
						if (propertyElementType == null) propertyElementType = property.getElementType();
						Class propertyDefaultType = config.propertyToDefaultType.get(property);
						if (!isExplicitKey) value = readValue(property.getType(), propertyElementType, propertyDefaultType);
						property.set(object, value);
					} catch (Exception ex) {
						if (ex instanceof YamlReaderException) throw (YamlReaderException)ex;
						throw new YamlReaderException("Error setting property '" + key + "' on class: " + type.getName(), ex);
					}
				}
			}
			if (object instanceof DeferredConstruction) {
				try {
					object = ((DeferredConstruction)object).construct();
					if (anchor != null) addAnchor(anchor, object); // Update anchor with real object.
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
					collection = (Collection)Beans.createObject(type, config.privateConstructors);
				} catch (InvocationTargetException ex) {
					throw new YamlReaderException("Error creating object.", ex);
				}
			} else if (type.isArray()) {
				collection = new ArrayList();
				elementType = type.getComponentType();
			} else
				throw new YamlReaderException("A sequence is not a valid value for the type: " + type.getName());
			if (!type.isArray() && anchor != null) addAnchor(anchor, collection);
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
			if (anchor != null) addAnchor(anchor, array);
			return array;
		}
		default:
			throw new YamlReaderException("Expected data for a " + type.getName() + " field but found: " + event.type);
		}
	}

	/** see http://yaml.org/type/merge.html */
	@SuppressWarnings("unchecked")
	private void mergeMap (Map<String, Object> dest, Object source) throws YamlReaderException {
		if (source instanceof Collection) {
			for (Object item : ((Collection<Object>)source))
				mergeMap(dest, item);
		} else if (source instanceof Map) {
			Map<String, Object> map = (Map<String, Object>)source;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if (!dest.containsKey(entry.getKey())) dest.put(entry.getKey(), entry.getValue());

			}
		} else
			throw new YamlReaderException("Expected a mapping or a sequence of mappings for a '<<' merge field but found: "
				+ source.getClass().getSimpleName());

	}

	/** Returns a new object of the requested type. */
	protected Object createObject (Class type) throws InvocationTargetException {
		// Use deferred construction if a non-zero-arg constructor is available.
		DeferredConstruction deferredConstruction = Beans.getDeferredConstruction(type, config);
		if (deferredConstruction != null) return deferredConstruction;
		return Beans.createObject(type, config.privateConstructors);
	}

	public class YamlReaderException extends YamlException {
		public YamlReaderException (String message, Throwable cause) {
			super("Line " + parser.getLineNumber() + ", column " + parser.getColumn() + ": " + message, cause);
		}

		public YamlReaderException (String message) {
			this(message, null);
		}
	}

	private Number valueConvertedNumber (String value) {
		Number number = null;
		try {
			number = Long.decode(value);
		} catch (NumberFormatException e) {
		}
		if (number == null) {
			try {
				number = Double.parseDouble(value);
			} catch (NumberFormatException e) {
			}
		}
		return number;
	}

	private void skipRange () {
		Event nextEvent;
		int depth = 0;
		do {
			nextEvent = parser.getNextEvent();
			switch (nextEvent.type) {
			case SEQUENCE_START:
				depth++;
				break;
			case MAPPING_START:
				depth++;
				break;
			case SEQUENCE_END:
				depth--;
				break;
			case MAPPING_END:
				depth--;
				break;
			default:
				// ignore
				break;
			}
		} while (depth > 0);
	}

	public static void main (String[] args) throws Exception {
		YamlReader reader = new YamlReader(new FileReader("test/test.yml"));
		Object object = reader.read();
		System.out.println(object);
		StringWriter string = new StringWriter();
		YamlWriter writer = new YamlWriter(string);
		writer.write(object);
		writer.close();
		System.out.println(string);
	}
}

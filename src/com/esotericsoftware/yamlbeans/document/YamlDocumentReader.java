package com.esotericsoftware.yamlbeans.document;

import static com.esotericsoftware.yamlbeans.parser.EventType.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import com.esotericsoftware.yamlbeans.Version;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.parser.*;
import com.esotericsoftware.yamlbeans.parser.Parser.ParserException;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer.TokenizerException;

public class YamlDocumentReader {

	Parser parser;

	public YamlDocumentReader(String yaml) {
		this(new StringReader(yaml));
	}

	public YamlDocumentReader(String yaml, Version version) {
		this(new StringReader(yaml), version);
	}

	public YamlDocumentReader(Reader reader) {
		this(reader, null);
	}

	public YamlDocumentReader(Reader reader, Version version) {
		if(version==null)
			version = Version.DEFAULT_VERSION;
		parser = new Parser(reader, version);
	}

	public YamlDocument read() throws YamlException {
		return read(YamlDocument.class);
	}

	@SuppressWarnings("unchecked")
	public <T> T read(Class<T> type) throws YamlException {
		try {
			while (true) {
				Event event = parser.peekNextEvent();
				if (event == null)
					return null;
				switch (event.type) {
				case STREAM_START:
					parser.getNextEvent(); // consume it
					break;
				case STREAM_END:
					parser.getNextEvent(); // consume it
					return null;
				case DOCUMENT_START:
					parser.getNextEvent(); // consume it
					return (T)readDocument();
				default:
					throw new IllegalStateException();
				}
			}
		} catch (ParserException ex) {
			throw new YamlException("Error parsing YAML.", ex);
		} catch (TokenizerException ex) {
			throw new YamlException("Error tokenizing YAML.", ex);
		}
	}

	public <T> Iterator<T> readAll(final Class<T> type) {
		Iterator<T> iterator = new Iterator<T>() {

			public boolean hasNext() {
				Event event = parser.peekNextEvent();
				return event != null && event.type != STREAM_END;
			}

			public T next() {
				try {
					return read(type);
				} catch (YamlException e) {
					throw new RuntimeException("Iterative reading documents exception", e);
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		return iterator;
	}

	private YamlElement readDocument() {
		Event event = parser.peekNextEvent();
		YamlElementReader reader = getReaderForEventType(event.type);
		YamlElement yamlElement = reader.read(parser);
		parser.getNextEvent(); // consume it(DOCUMENT_END)
		return yamlElement;
	}

	private YamlMapping readMapping() {
		Event event = parser.getNextEvent();
		if(event.type!=MAPPING_START)
			throw new IllegalStateException();
		YamlMapping element = new YamlMapping();
		MappingStartEvent mapping = (MappingStartEvent)event;
		element.setTag(mapping.tag);
		element.setAnchor(mapping.anchor);
		readMappingElements(element);
		return element;
	}

	private void readMappingElements(YamlMapping mapping) {
		while(true) {
			Event event = parser.peekNextEvent();
			if(event.type == MAPPING_END) {
				parser.getNextEvent(); // consume it
				return;
			} else {
				YamlEntry entry = readEntry();
				mapping.addEntry(entry);
			}
		}
	}

	private YamlEntry readEntry() {
		YamlScalar scalar = readScalarUsingReader();
		YamlElement value = readValue();
		return new YamlEntry(scalar, value);
	}

	private YamlScalar readScalarUsingReader() {
		YamlElement element = new ScalarReader().read(parser);
		if (!(element instanceof YamlScalar)) {
			throw new IllegalStateException("Expected a YamlScalar but got " + element.getClass().getName());
		}
		return (YamlScalar) element;
	}

	private YamlElement readValue() {
		Event event = parser.peekNextEvent();
		YamlElementReader reader = getReaderForEventType(event.type);
		return reader.read(parser);
	}

    private interface YamlElementReader {
        YamlElement read(Parser parser);
    }

    private YamlElementReader getReaderForEventType(EventType eventType) {
        switch (eventType) {
            case SCALAR:
                return new ScalarReader();
            case ALIAS:
                return new AliasReader();
            case MAPPING_START:
                return new MappingReader();
            case SEQUENCE_START:
                return new SequenceReader();
            default:
                throw new IllegalStateException();
        }
    }

    private class ScalarReader implements YamlElementReader {
        @Override
        public YamlElement read(Parser parser) {
            Event event = parser.getNextEvent();
            if(event.type!= SCALAR)
                throw new IllegalStateException();
            ScalarEvent scalar = (ScalarEvent)event;
            YamlScalar element = new YamlScalar();
            element.setTag(scalar.tag);
            element.setAnchor(scalar.anchor);
            element.setValue(scalar.value);
            return element;
        }
    }

    private class SequenceReader implements YamlElementReader {
        @Override
        public YamlElement read(Parser parser) {
            Event event = parser.getNextEvent();
            if(event.type!=SEQUENCE_START)
                throw new IllegalStateException();
            YamlSequence element = new YamlSequence();
            SequenceStartEvent sequence = (SequenceStartEvent)event;
            element.setTag(sequence.tag);
            element.setAnchor(sequence.anchor);
            readSequenceElements(element);
            return element;
        }
    }

    private class AliasReader implements YamlElementReader {
        @Override
        public YamlElement read(Parser parser) {
            Event event = parser.getNextEvent();
            if(event.type!=ALIAS)
                throw new IllegalStateException();
            YamlAlias element = new YamlAlias();
            AliasEvent alias = (AliasEvent)event;
            element.setAnchor(alias.anchor);
            return element;
        }
    }

    private class MappingReader implements YamlElementReader {
        @Override
        public YamlElement read(Parser parser) {
			Event event = parser.getNextEvent();
			if(event.type!=MAPPING_START)
				throw new IllegalStateException();
			YamlMapping element = new YamlMapping();
			MappingStartEvent mapping = (MappingStartEvent)event;
			element.setTag(mapping.tag);
			element.setAnchor(mapping.anchor);
			readMappingElements(element);
			return element;
        }
    }

	private void readSequenceElements(YamlSequence sequence) {
		while(true) {
			Event event = parser.peekNextEvent();
			if(event.type==SEQUENCE_END) {
				parser.getNextEvent(); // consume it
				return;
			} else {
				YamlElement element = readValue();
				sequence.addElement(element);
			}
		}
	}
}

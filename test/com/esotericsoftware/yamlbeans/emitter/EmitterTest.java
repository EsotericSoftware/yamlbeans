package com.esotericsoftware.yamlbeans.emitter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.esotericsoftware.yamlbeans.parser.DocumentStartEvent;
import com.esotericsoftware.yamlbeans.parser.Event;
import com.esotericsoftware.yamlbeans.parser.Parser;
import com.esotericsoftware.yamlbeans.parser.ScalarEvent;

public class EmitterTest {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private StringWriter stringWriter = null;
	private Emitter emitter = null;

	@Before
	public void setup() {
		stringWriter = new StringWriter();
		emitter = new Emitter(stringWriter);
	}

	@Test
	public void testEmitterConstructor() throws IOException {
		try {
			new Emitter(null);
		} catch (IllegalArgumentException e) {
			assertEquals("stream cannot be null.", e.getMessage());
		}

		StringWriter writer = new StringWriter();
		new Emitter(writer);

		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		EmitterConfig emitterConfig = new EmitterConfig();
		new Emitter(bufferedWriter, emitterConfig);
	}

	@Test(expected = EmitterException.class)
	public void testStateStreamStart() throws IOException {
		emitter.emit(Event.STREAM_START);
		emitter.emit(new DocumentStartEvent(false, null, null));
		emitter.emit(Event.DOCUMENT_END_TRUE);
	}

	@Test(expected = EmitterException.class)
	public void testStateNOTHING() throws EmitterException, IOException {
		emitter.emit(Event.STREAM_START);
		emitter.emit(Event.STREAM_END);
		assertEquals(3, emitter.state);
		emitter.emit(Event.DOCUMENT_END_TRUE);
	}

	@Test(expected = EmitterException.class)
	public void testDocumentEnd() throws EmitterException, IOException {
		emitter.emit(Event.STREAM_START);
		emitter.emit(new DocumentStartEvent(false, null, null));
		emitter.emit(new ScalarEvent(null, null, new boolean[] { true, true }, "test", '\0'));
		emitter.emit(Event.STREAM_END);
	}

	@Test
	public void testEmptySequence() throws YamlException {
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setFlowStyle(true);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		List<String> list = new ArrayList<String>();
		yamlWriter.write(list);
		yamlWriter.close();
		assertEquals("[]" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testStateFlowMapingKey() throws YamlException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setFlowStyle(true);
		yamlConfig.writeConfig.setWriteRootTags(false);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(map);
		yamlWriter.close();
		assertEquals("{key1: value1, key2: value2}" + LINE_SEPARATOR, stringWriter.toString());

	}

	@Test
	public void testStateFlowMapingKeyCanonicalIsTrue() throws EmitterException, IOException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setFlowStyle(true);
		yamlConfig.writeConfig.setWriteRootTags(false);
		yamlConfig.writeConfig.setCanonical(true);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(map);
		yamlWriter.close();
		assertEquals(
				"--- " + LINE_SEPARATOR + "{" + LINE_SEPARATOR + "   ? !java.lang.String \"key1\"" + LINE_SEPARATOR
						+ "   : !java.lang.String \"value1\"," + LINE_SEPARATOR + "   ? !java.lang.String \"key2\""
						+ LINE_SEPARATOR + "   : !java.lang.String \"value2\"" + LINE_SEPARATOR + "}" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test(expected = EmitterException.class)
	public void testDocumentStart() throws EmitterException, IOException {
		emitter.emit(Event.STREAM_START);
		emitter.emit(new ScalarEvent(null, null, new boolean[] { true, true }, "test", '\0'));
	}

	@Test
	public void testBlockMappingValueMultiline() throws EmitterException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("555\n666", "value");
		YamlWriter yamlWriter = new YamlWriter(stringWriter);
		yamlWriter.write(map);
		yamlWriter.close();

		assertEquals("? |-" + LINE_SEPARATOR + "   555" + LINE_SEPARATOR + "   666" + LINE_SEPARATOR + ": value"
				+ LINE_SEPARATOR, stringWriter.toString());
	}

	@Test(expected = EmitterException.class)
	public void testExpectNode() throws EmitterException, IOException {
		emitter.emit(Event.STREAM_START);
		emitter.emit(new DocumentStartEvent(false, null, null));
		emitter.emit(Event.MAPPING_END);
	}

	@Test
	public void testParserToEmitter() throws EmitterException, IOException {
		Parser parser = new Parser(new FileReader("test/test.yml"));
		Emitter emitter = new Emitter(new OutputStreamWriter(System.out));
		while (true) {
			Event event = parser.getNextEvent();
			if (event == null)
				break;
			emitter.emit(event);
		}
		emitter.close();
	}
}

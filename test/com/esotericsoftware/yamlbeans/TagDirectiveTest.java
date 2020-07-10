package com.esotericsoftware.yamlbeans;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.document.YamlDocumentReader;
import com.esotericsoftware.yamlbeans.document.YamlElement;
import com.esotericsoftware.yamlbeans.document.YamlSequence;

public class TagDirectiveTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Test
	public void testTagDirective() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("%TAG !yaml! tag:yaml.org,2002:").append(LINE_SEPARATOR);
		sb.append("---").append(LINE_SEPARATOR);
		sb.append("!yaml!str \"foo\"");

		YamlReader yamlReader = new YamlReader(sb.toString());
		assertEquals("foo", yamlReader.read().toString());
	}

	@Test(expected = YamlException.class)
	public void testRepeatedTagDirective() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("%TAG %TAG ! !foo").append(LINE_SEPARATOR);
		sb.append("%TAG %TAG ! !foo").append(LINE_SEPARATOR);
		sb.append("---").append(LINE_SEPARATOR);
		sb.append("bar");

		new YamlReader(sb.toString()).read();
	}

	@Test
	public void testTagHandles() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("%TAG !      !").append(LINE_SEPARATOR);
		sb.append("%TAG !yaml! tag:yaml.org,2002:").append(LINE_SEPARATOR);
		sb.append("%TAG !o! tag:ben-kiki.org,2000:").append(LINE_SEPARATOR);
		sb.append("---").append(LINE_SEPARATOR);
		sb.append("- !foo bar").append(LINE_SEPARATOR);
		sb.append("- !!str string").append(LINE_SEPARATOR);
		sb.append("- !o!type baz").append(LINE_SEPARATOR);
		YamlDocumentReader yamlDocumentReader = new YamlDocumentReader(sb.toString());
		Iterator<YamlElement> iterator = yamlDocumentReader.read(YamlSequence.class).iterator();

		YamlElement yamlElement = iterator.next();
		assertEquals("!foo", yamlElement.getTag());

		yamlElement = iterator.next();
		assertEquals("tag:yaml.org,2002:str", yamlElement.getTag());

		yamlElement = iterator.next();
		assertEquals("tag:ben-kiki.org,2000:type", yamlElement.getTag());
	}

	@Test
	public void testVersion() throws YamlException {
		String yaml = "!str test";
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.readConfig.setDefaultVersion(new Version(1, 0));
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		assertEquals("test", yamlReader.read());

		yaml = "!!str test";
		yamlConfig.readConfig.setDefaultVersion(new Version(1, 1));
		yamlReader = new YamlReader(yaml, yamlConfig);
		assertEquals("test", yamlReader.read());

		yaml = "!!str test";
		yamlConfig.readConfig.setDefaultVersion(new Version(1, 0));
		yamlReader = new YamlReader(yaml, yamlConfig);
		try {
			yamlReader.read();
		} catch (YamlException e) {
			assertEquals("Error parsing YAML.", e.getMessage());
		}
	}
}

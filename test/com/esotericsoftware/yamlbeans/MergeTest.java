package com.esotericsoftware.yamlbeans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlReader.YamlReaderException;

import static org.junit.Assert.*;

public class MergeTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testMerge() throws FileNotFoundException, YamlException {
		InputStream input = new FileInputStream("test/test-merge.yml");
		Reader reader = new InputStreamReader(input);
		Map data = new YamlReader(reader).read(Map.class);
		Map stuff = (Map)data.get("merged");
		assertEquals("v1", stuff.get("v1"));
		assertEquals("v2", stuff.get("v2"));
		assertEquals("v3", stuff.get("v3"));
		assertNull(stuff.get("<<"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMergeMultipleMaps() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("test1: &1\n").append("  - key1: value1\n").append("  - key2: value2\n");
		sb.append("test2:\n").append("  << : *1");

		YamlReader yamlReader = new YamlReader(sb.toString());
		Map<String, Object> map = (Map<String, Object>) yamlReader.read();
		assertEquals("value1", ((Map<String, String>) map.get("test2")).get("key1"));
		assertEquals("value2", ((Map<String, String>) map.get("test2")).get("key2"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMergeUpdateValue() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("test1: &1\n").append("  - key1: value1\n").append("  - key2: value2\n").append("  - key3: value3\n");

		sb.append("test2:\n").append("  key2: value22\n").append("  << : *1\n").append("  key3: value33\n");

		YamlReader yamlReader = new YamlReader(sb.toString());
		Map<String, Object> map = (Map<String, Object>) yamlReader.read();
		assertEquals("value1", ((Map<String, String>) map.get("test2")).get("key1"));
		assertEquals("value22", ((Map<String, String>) map.get("test2")).get("key2"));
		assertEquals("value33", ((Map<String, String>) map.get("test2")).get("key3"));
	}

	@Test
	public void testMergeExpectThrowYamlReaderException() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("test1: &1 123\n");
		sb.append("<< : *1\n");

		YamlReader yamlReader = new YamlReader(sb.toString());
		try {
			yamlReader.read();
			fail("Expected a mapping or a sequence of mappings");
		} catch (YamlReaderException e) {
		}
	}
}

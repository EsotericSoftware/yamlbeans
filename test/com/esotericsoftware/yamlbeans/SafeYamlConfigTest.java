
package com.esotericsoftware.yamlbeans;

import static junit.framework.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SafeYamlConfigTest {
	private static final String TESTOBJECT_TAG = "!com.esotericsoftware.yamlbeans.SafeYamlConfigTest$TestObject";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Test
	public void testDeserializationOfClassTag () throws YamlException {
		SafeYamlConfig yamlConfig = new SafeYamlConfig();
		StringBuilder yamlData = new StringBuilder();
		yamlData.append(TESTOBJECT_TAG).append(LINE_SEPARATOR).append("a: test").append(LINE_SEPARATOR);
		YamlReader reader = new YamlReader(yamlData.toString(), yamlConfig);
		Object data = reader.read();
		assertTrue(data instanceof HashMap);
		Map dataMap = (Map)data;
		assertTrue(dataMap.containsKey("a"));
		assertEquals("test", dataMap.get("a"));
	}

	@Test
	public void testIgnoreAnchor () throws YamlException {
		SafeYamlConfig yamlConfig = new SafeYamlConfig();
		StringBuilder yamlData = new StringBuilder();
		yamlData.append("oldest friend:").append(LINE_SEPARATOR).append("    &1 !contact").append(LINE_SEPARATOR)
			.append("    name: Bob").append(LINE_SEPARATOR).append("    age: 29").append(LINE_SEPARATOR).append("best friend: *1")
			.append(LINE_SEPARATOR);
		YamlReader reader = new YamlReader(yamlData.toString(), yamlConfig);
		Object data = reader.read();
		assertTrue(data instanceof HashMap);
		Map dataMap = (Map)data;
		assertTrue(dataMap.containsKey("oldest friend"));
		Map old = (Map)dataMap.get("oldest friend");
		assertTrue(old.containsKey("name"));
		assertEquals("Bob", old.get("name"));
		assertNull(dataMap.get("best friend"));
	}

	static class TestObject {
		private String a;
		public int age;
		public String name;
		public Object object;
		public List<Object> objects;

		private TestObject () {
		}

		public TestObject (String a) {
			this.a = a;
		}

		public String getA () {
			return a;
		}

		public void setA (String a) {
			this.a = a;
		}
	}
}

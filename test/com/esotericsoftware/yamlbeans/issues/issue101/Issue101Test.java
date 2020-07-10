package com.esotericsoftware.yamlbeans.issues.issue101;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class Issue101Test {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Test
	public void test() throws YamlException {
		StringBuilder sb = new StringBuilder();
		sb.append("!com.esotericsoftware.yamlbeans.issues.issue101.Issue101Test$TestObject").append(LINE_SEPARATOR);
		sb.append("test: test");
		YamlReader yamlReader = new YamlReader(sb.toString());
		TestObject testObject = (TestObject) yamlReader.read();
		assertEquals("test", testObject.test);

		sb = new StringBuilder();
		sb.append("!<!com.esotericsoftware.yamlbeans.issues.issue101.Issue101Test$TestObject>").append(LINE_SEPARATOR);
		sb.append("test: test");
		yamlReader = new YamlReader(sb.toString());
		testObject = (TestObject) yamlReader.read();
		assertEquals("test", testObject.test);
	}

	static class TestObject {
		public String test;
	}
}

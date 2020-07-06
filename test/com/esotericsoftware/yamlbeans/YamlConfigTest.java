package com.esotericsoftware.yamlbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlConfig.Quote;
import com.esotericsoftware.yamlbeans.YamlReader.YamlReaderException;
import com.esotericsoftware.yamlbeans.scalar.DateSerializer;

public class YamlConfigTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String TESTOBJECT_TAG = "!com.esotericsoftware.yamlbeans.YamlConfigTest$TestObject";

	private YamlConfig yamlConfig;

	@Before
	public void setup() throws Exception {
		yamlConfig = new YamlConfig();
	}

	@Test
	public void testSetClassTag() throws YamlException {
		yamlConfig.setClassTag("String", String.class);
		String yaml = "!String test";
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		assertEquals("test", yamlReader.read());

		try {
			yamlConfig.setClassTag(null, String.class);
		} catch (IllegalArgumentException e) {
			assertEquals("tag cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setClassTag("str", null);
		} catch (IllegalArgumentException e) {
			assertEquals("type cannot be null.", e.getMessage());
		}
	}

	@Test
	public void testSetScalarSerializer() throws YamlException {
		TimeZone timeZone = TimeZone.getTimeZone("GMT+0");
		TimeZone.setDefault(timeZone);

		yamlConfig.setScalarSerializer(Date.class, new DateSerializer());
		String yaml = "!java.util.Date 1970-01-01 00:00:00";
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		Object object = yamlReader.read();
		assertEquals(Date.class, object.getClass());
		assertEquals(0, ((Date) object).getTime());

		try {
			yamlConfig.setScalarSerializer(null, new DateSerializer());
		} catch (IllegalArgumentException e) {
			assertEquals("type cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setScalarSerializer(Date.class, null);
		} catch (IllegalArgumentException e) {
			assertEquals("serializer cannot be null.", e.getMessage());
		}
	}

	@Test
	public void testSetPropertyElementType() throws YamlException {
		yamlConfig.setPropertyElementType(TestObject.class, "objects", Date.class);

		StringBuilder sb = new StringBuilder();
		sb.append("objects:" + LINE_SEPARATOR);
		sb.append("- 2020-06-30 00:00:00" + LINE_SEPARATOR);

		YamlReader yamlReader = new YamlReader(sb.toString(), yamlConfig);
		TestObject testObject = yamlReader.read(TestObject.class);
		assertEquals(Date.class, testObject.objects.get(0).getClass());

		try {
			yamlConfig.setPropertyElementType(null, "objects", Date.class);
		} catch (IllegalArgumentException e) {
			assertEquals("type cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyElementType(TestObject.class, null, Date.class);
		} catch (IllegalArgumentException e) {
			assertEquals("propertyName cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyElementType(TestObject.class, "objects", null);
		} catch (IllegalArgumentException e) {
			assertEquals("propertyType cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyElementType(TestObject.class, "aaa", Object.class);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("does not have a property named"));
		}

		try {
			yamlConfig.setPropertyElementType(TestObject.class, "object", Date.class);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("class must be a Collection or Map"));
		}
	}

	@Test
	public void testSetPropertyDefaultType() throws YamlException {
		yamlConfig.setPropertyDefaultType(TestObject.class, "object", Date.class);

		String yaml = "object: 2020-06-30 00:00:00";

		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		TestObject testObject = yamlReader.read(TestObject.class);
		assertEquals(Date.class, testObject.object.getClass());

		try {
			yamlConfig.setPropertyDefaultType(null, "object", Date.class);
		} catch (IllegalArgumentException e) {
			assertEquals("type cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyDefaultType(TestObject.class, null, Date.class);
		} catch (IllegalArgumentException e) {
			assertEquals("propertyName cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyDefaultType(TestObject.class, "object", null);
		} catch (IllegalArgumentException e) {
			assertEquals("defaultType cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.setPropertyDefaultType(TestObject.class, "aaa", Date.class);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("does not have a property named"));
		}
	}

	@Test
	public void testSetBeanProperties() throws YamlException {
		String yaml = "a: test";
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		TestObject testObject = yamlReader.read(TestObject.class);
		assertEquals("test", testObject.a);

		yamlConfig.setBeanProperties(false);
		yamlReader = new YamlReader(yaml, yamlConfig);
		try {
			yamlReader.read(TestObject.class);
		} catch (YamlReaderException e) {
			assertTrue(e.getMessage().contains("Unable to find property"));
		}
	}

	@Test
	public void testSetPrivateConstructors() throws YamlException {
		String yaml = "a: test";
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		TestObject testObject = yamlReader.read(TestObject.class);
		assertEquals("test", testObject.a);

		yamlConfig.setPrivateConstructors(false);
		yamlReader = new YamlReader(yaml, yamlConfig);
		try {
			yamlReader.read(TestObject.class);
		} catch (YamlReaderException e) {
			assertTrue(e.getMessage().contains("Error creating object"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetTagSuffix() throws YamlException {
		StringWriter stringWriter = new StringWriter();
		yamlConfig.setTagSuffix("tag");
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "111");
		map.put("btag", "222");
		yamlWriter.write(map);
		yamlWriter.close();

		YamlReader yamlReader = new YamlReader(stringWriter.toString(), yamlConfig);
		Map<String, String> result = yamlReader.read(Map.class);
		assertTrue(result.containsKey("a"));
		assertFalse(result.containsKey("btag"));
		assertTrue(result.containsKey("atag"));
	}

	@Test
	public void testSetExplicitEndDocument() throws YamlException {
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();

		assertEquals("test" + LINE_SEPARATOR, stringWriter.toString());

		stringWriter = new StringWriter();
		yamlConfig.writeConfig.setExplicitEndDocument(true);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("test" + LINE_SEPARATOR + "..." + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetWriteRootTags() throws YamlException {
		TestObject to = new TestObject();
		to.a = "test";

		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(to);
		yamlWriter.close();

		assertEquals(TESTOBJECT_TAG + LINE_SEPARATOR + "a: test" + LINE_SEPARATOR, stringWriter.toString());

		yamlConfig.writeConfig.setWriteRootTags(false);
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(to);
		yamlWriter.close();
		assertEquals("a: test" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetWriteRootElementTags() throws YamlException {
		TestObject to1 = new TestObject();
		to1.a = "test";
		List<TestObject> list = new ArrayList<TestObject>();
		list.add(to1);

		TestObject to2 = new TestObject();
		to2.a = "test";
		Map<String, TestObject> map = new HashMap<String, TestObject>();
		map.put("test", to2);

		yamlConfig.writeConfig.setWriteRootElementTags(true);
		int indentSize = 3;
		StringWriter stringWriter = new StringWriter();
		yamlConfig.writeConfig.setIndentSize(indentSize);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.write(map);
		yamlWriter.close();

		assertEquals("- " + TESTOBJECT_TAG + LINE_SEPARATOR + multipleSpaces(indentSize) + "a: test" + LINE_SEPARATOR
				+ "--- " + LINE_SEPARATOR + "test: " + TESTOBJECT_TAG + LINE_SEPARATOR + multipleSpaces(indentSize)
				+ "a: test" + LINE_SEPARATOR, stringWriter.toString());

		yamlConfig.writeConfig.setWriteRootElementTags(false);
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.write(map);
		yamlWriter.close();

		assertEquals(
				"-" + multipleSpaces(indentSize - 1) + "a: test" + LINE_SEPARATOR + "--- " + LINE_SEPARATOR + "test: "
						+ LINE_SEPARATOR + multipleSpaces(indentSize) + "a: test" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test
	public void testSetWriteDefaultValues() throws YamlException {
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		TestObject testObject = new TestObject();
		yamlWriter.write(testObject);
		yamlWriter.close();

		assertEquals(TESTOBJECT_TAG + " {}" + LINE_SEPARATOR, stringWriter.toString());

		yamlConfig.writeConfig.setWriteDefaultValues(true);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(testObject);
		yamlWriter.close();
		assertFalse((TESTOBJECT_TAG + " {}" + LINE_SEPARATOR).equals(stringWriter.toString()));
		assertTrue(stringWriter.toString().contains(TESTOBJECT_TAG + LINE_SEPARATOR));
		assertTrue(stringWriter.toString().contains("a: " + LINE_SEPARATOR));

		assertTrue(stringWriter.toString().contains("object: " + LINE_SEPARATOR));
	}

	@Test
	public void testSetKeepBeanPropertyOrder() throws YamlException {
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		TestObject testObject = new TestObject();
		testObject.name = "Jack";
		testObject.age = 18;
		yamlWriter.write(testObject);
		yamlWriter.close();

		assertEquals(TESTOBJECT_TAG + LINE_SEPARATOR + "name: Jack" + LINE_SEPARATOR + "age: 18" + LINE_SEPARATOR,
				stringWriter.toString());

		yamlConfig.writeConfig.setKeepBeanPropertyOrder(true);
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(testObject);
		yamlWriter.close();
		assertEquals(TESTOBJECT_TAG + LINE_SEPARATOR + "age: 18" + LINE_SEPARATOR + "name: Jack" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test
	public void testSetVersion() throws YamlException {

		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("test" + LINE_SEPARATOR, stringWriter.toString());

		Version version = new Version(1, 0);
		yamlConfig.writeConfig.setVersion(version);
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("%YAML 1.0" + LINE_SEPARATOR + "--- test" + LINE_SEPARATOR, stringWriter.toString());

		version = new Version(1, 1);
		yamlConfig.writeConfig.setVersion(version);
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("%YAML 1.1" + LINE_SEPARATOR + "--- test" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetTags() throws YamlException {
		Map<String, String> tags = new HashMap<String, String>();
		tags.put("!foo!", "bar");
		yamlConfig.writeConfig.setTags(tags);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("%TAG !foo! bar" + LINE_SEPARATOR + "--- test" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetCanonical() throws YamlException {
		int indentSize = 3;
		yamlConfig.writeConfig.setIndentSize(indentSize);
		yamlConfig.writeConfig.setCanonical(true);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("test");
		yamlWriter.close();
		assertEquals("--- " + LINE_SEPARATOR + "!java.lang.String \"test\"" + LINE_SEPARATOR, stringWriter.toString());

		List<String> list = new ArrayList<String>();
		list.add("111");
		list.add("222");
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.close();
		assertEquals("--- " + LINE_SEPARATOR + "[" + LINE_SEPARATOR + multipleSpaces(indentSize)
				+ "!java.lang.String \"111\"," + LINE_SEPARATOR + multipleSpaces(indentSize)
				+ "!java.lang.String \"222\"" + LINE_SEPARATOR + "]" + LINE_SEPARATOR, stringWriter.toString());

		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		stringWriter = new StringWriter();
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(map);
		yamlWriter.close();
		assertEquals(
				"--- " + LINE_SEPARATOR + "{" + LINE_SEPARATOR + multipleSpaces(indentSize)
						+ "? !java.lang.String \"key\"" + LINE_SEPARATOR + multipleSpaces(indentSize)
						+ ": !java.lang.String \"value\"" + LINE_SEPARATOR + "}" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test
	public void testSetIndentSize() throws YamlException {
		int indentSize = 5;
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);

		StringWriter stringWriter = new StringWriter();
		yamlConfig.writeConfig.setCanonical(true);
		yamlConfig.writeConfig.setIndentSize(indentSize);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.close();
		assertEquals("--- " + LINE_SEPARATOR + "[" + LINE_SEPARATOR + multipleSpaces(indentSize)
				+ "!java.lang.Integer \"1\"" + LINE_SEPARATOR + "]" + LINE_SEPARATOR, stringWriter.toString());

		try {
			yamlConfig.writeConfig.setIndentSize(1);
		} catch (Exception e) {
			assertEquals("indentSize cannot be less than 2.", e.getMessage());
		}
	}

	@Test
	public void testSetWrapColumn() throws YamlException {
		String yaml = "aaaaaa aaaaa";
		int indentSize = 3;
		yamlConfig.writeConfig.setIndentSize(indentSize);
		yamlConfig.writeConfig.setWrapColumn(5);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(yaml);
		yamlWriter.close();

		assertEquals("aaaaaa" + LINE_SEPARATOR + multipleSpaces(indentSize) + "aaaaa" + LINE_SEPARATOR,
				stringWriter.toString());

		YamlReader yamlReader = new YamlReader(stringWriter.toString());
		assertEquals(yaml, yamlReader.read(String.class));

		try {
			yamlConfig.writeConfig.setWrapColumn(3);
		} catch (IllegalArgumentException e) {
			assertEquals("wrapColumn must be greater than 4.", e.getMessage());
		}

	}

	@Test
	public void testSetUseVerbatimTags() throws YamlException {
		List<Integer> list = new LinkedList<Integer>();
		list.add(1);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.close();

		assertEquals("!java.util.LinkedList" + LINE_SEPARATOR + "- 1" + LINE_SEPARATOR, stringWriter.toString());

		stringWriter = new StringWriter();
		yamlConfig.writeConfig.setUseVerbatimTags(true);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write(list);
		yamlWriter.close();

		assertEquals("!<java.util.LinkedList>" + LINE_SEPARATOR + "- 1" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetQuoteChar() throws YamlException {

		StringWriter stringWriter = new StringWriter();
		yamlConfig.writeConfig.setQuoteChar(Quote.NONE);
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("123");
		yamlWriter.close();
		assertEquals("123" + LINE_SEPARATOR, stringWriter.toString());

		stringWriter = new StringWriter();
		yamlConfig.writeConfig.setQuoteChar(Quote.SINGLE);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("123");
		yamlWriter.close();
		assertEquals("\'123\'" + LINE_SEPARATOR, stringWriter.toString());

		stringWriter = new StringWriter();
		yamlConfig.writeConfig.setQuoteChar(Quote.DOUBLE);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("123");
		yamlWriter.close();
		assertEquals("\"123\"" + LINE_SEPARATOR, stringWriter.toString());
	}

	@Test
	public void testSetDefaultVersion() throws YamlException {
		String yaml = "!!str test";
		yamlConfig.readConfig.setDefaultVersion(new Version(1, 1));
		YamlReader yamlReader = new YamlReader(yaml, yamlConfig);
		assertEquals("test", yamlReader.read(String.class));

		try {
			yamlConfig.readConfig.setDefaultVersion(null);
		} catch (IllegalArgumentException e) {
			assertEquals("defaultVersion cannot be null.", e.getMessage());
		}

	}

	@Test
	public void testConstructorParameters() throws YamlException {
		yamlConfig.readConfig.setConstructorParameters(TestObject.class, new Class[] { String.class },
				new String[] { "a" });
		YamlReader yamlReader = new YamlReader("a: test", yamlConfig);
		assertEquals("test", yamlReader.read(TestObject.class).a);

		try {
			yamlConfig.readConfig.setConstructorParameters(null, new Class[] { String.class }, new String[] { "a" });
		} catch (IllegalArgumentException e) {
			assertEquals("type cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.readConfig.setConstructorParameters(TestObject.class, null, new String[] { "a" });
		} catch (IllegalArgumentException e) {
			assertEquals("parameterTypes cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.readConfig.setConstructorParameters(TestObject.class, new Class[] { String.class }, null);
		} catch (IllegalArgumentException e) {
			assertEquals("parameterNames cannot be null.", e.getMessage());
		}

		try {
			yamlConfig.readConfig.setConstructorParameters(TestObject.class, new Class[] { String.class, String.class },
					new String[] { "a", "name" });
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith("Unable to find constructor: "));
		}

	}

	private String multipleSpaces(int indentSize) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentSize; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	static class TestObject {
		private String a;
		public int age;
		public String name;
		public Object object;
		public List<Object> objects;

		private TestObject() {
		}

		public TestObject(String a) {
			this.a = a;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}
	}
}

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

import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

import junit.framework.TestCase;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class YamlWriterTest extends TestCase {
	public void testPrivateFields () throws Exception {
		ArrayList list = new ArrayList();
		list.add("abc");
		list.add("123");
		roundTrip(new PrivateFields(list, 123l));
	}

	static public interface Key<ID> {
		ID getId ();

		void setId (ID id);
	}

	static public class Obj implements Key<Long> {
		public Obj () {
		}

		public Obj (Long id) {
			this.id = id;
		}

		Long id;

		public Long getId () {
			return id;
		}

		public void setId (Long id) {
			this.id = id;
		}
	}

	public void testYamlLong () throws Exception {
		StringWriter stringWriter = new StringWriter();
		YamlWriter writer = new YamlWriter(stringWriter);
		writer.getConfig().setClassTag("obj", Obj.class);
		writer.write(new Obj(1l));
		writer.close();

		YamlReader reader = new YamlReader(stringWriter.toString());
		reader.getConfig().setClassTag("obj", Obj.class);
		Obj obj = reader.read(Obj.class);
		System.out.println(obj.getId());
	}

	public void testSimpleTypes () throws Exception {
		String string = "simple string";
		assertEquals(string, roundTrip(string));

		Map map = new HashMap();
		map.put("moo", "cow");
		assertEquals(map, roundTrip(map));

		List list = new ArrayList();
		list.add("moo");
		list.add("cow");
		assertEquals(list, roundTrip(list));

		map.put("fubar", list);
		assertEquals(map, roundTrip(map));

		String[] stringArray = new String[] {"moo", "meow", "cow", "gato"};
		List result = (List)roundTrip(stringArray);
		assertEquals(stringArray[0], result.get(0));
		assertEquals(stringArray[1], result.get(1));
		assertEquals(stringArray[2], result.get(2));
		assertEquals(stringArray[3], result.get(3));
	}

	public void testUnicodeStrings () throws Exception {
		String string = "unicode \ufec9 string";
		assertEquals(string, roundTrip(string));

		string = "unicode \ufec9 string";
		YamlConfig config = new YamlConfig();
		config.writeConfig.setEscapeUnicode(false);
		assertEquals(string, roundTrip(string, null, config));

		Map<String, String> map = new HashMap();
		map.put("moo\ufec9cow", "sw\ufec9\ufec9t!");
		assertEquals(map, roundTrip(map));
		assertEquals(map, roundTrip(map, null, config));
	}

	public void testStringEscaping () throws Exception {
		Test test = new Test();
		test.stringValue = "" + //
			"if (chr != ' ' && chr != '\\t' && chr != '\\r'\n" + //
			"\t&& chr != '\\n') {\n" + //
			"\tbreak\n" + //
			"\t}\n";
		roundTrip(test);
	}

	public void testObjects () throws Exception {
		Test test = new Test();
		test.stringValue = "yay";
		test.intValue = 123;
		test.booleanValue = true;
		test.floatValue = 1.2f;
		test.doubleValue = 1.5;
		test.child = new Test2();
		((Test2)test.child).setValue("weeeee");
		test.listValues = new LinkedList();
		test.listValues.add("woo");
		test.listValues.add(new Test());
		test.listValues.add(123);
		test.listValues.add(test.child);
		test.value = Value.c;
		test.date = new Date();

		YamlConfig config = new YamlConfig();
		config.writeConfig.setAutoAnchor(true);
		Test result = roundTrip(test, Test.class, config);
		assertEquals(test.stringValue, result.stringValue);
		assertEquals(test.intValue, result.intValue);
		assertEquals(test.booleanValue, result.booleanValue);
		assertEquals(test.floatValue, result.floatValue);
		assertEquals(test.doubleValue, result.doubleValue);
		assertEquals(test.listValues.size(), result.listValues.size());
		assertEquals(test.listValues.getClass(), result.listValues.getClass());
		assertEquals(((Test2)test.child).getValue(), "weeeee");
		assertEquals(test.value, result.value);
		assertTrue(result.child == result.listValues.get(3));

		config.writeConfig.setAutoAnchor(false);
		config.setClassTag("otherTest", Test2.class);
		result = roundTrip(test, Test.class, config);
		assertEquals(test.stringValue, result.stringValue);
		assertEquals(test.intValue, result.intValue);
		assertEquals(test.booleanValue, result.booleanValue);
		assertEquals(test.floatValue, result.floatValue);
		assertEquals(test.doubleValue, result.doubleValue);
		assertEquals(test.listValues.size(), result.listValues.size());
		assertEquals(test.listValues.getClass(), result.listValues.getClass());
		assertEquals(((Test2)test.child).getValue(), "weeeee");
		assertEquals(test.value, result.value);
		assertTrue(result.child != result.listValues.get(3));

		config.writeConfig.setAutoAnchor(true);
		roundTrip(test, Test.class, config);

		test.listValues.clear();
		test.listValues.add(new Test2("a"));
		test.listValues.add(new Test2("b"));
		test.listValues.add(new Test2("c"));
		test.listValues.add(test.listValues.get(0));
		test.listValues.add(new Test2("d"));
		config.setPropertyElementType(Test.class, "listValues", Test2.class);
		roundTrip(test, Test.class, config);
	}

	public void testLongMapKey () throws Exception {
		StringBuffer longKey = new StringBuffer();
		for (int i = 0; i < 128; i++)
			longKey.append("A");
		Map<String, String> map = new HashMap();
		map.put(longKey.toString(), "X");
		Map copy = (Map)roundTrip(map);
		assertEquals("X", copy.get(longKey.toString()));
	}

	public void testSingleQuotes () throws Exception {
		Test test = new Test();
		test.stringValue = "" + //
			"if (chr != ' ' && chr != 't' && chr != 'r'\n" + //
			"\t&& chr != 'n') {\n" + //
			"\tbreak\n" + //
			"\t}\n";
		roundTrip(test);
	}

	public void testExplicitDocumentEnd () throws Exception {
		YamlConfig config = new YamlConfig();
		config.writeConfig.explicitEndDocument = true;

		StringWriter buffer = new StringWriter();
		YamlWriter writer = new YamlWriter(buffer, config);
		writer.write("test");
		writer.close();
		assertEquals("test\n...\n", buffer.toString());

		buffer = new StringWriter();
		writer = new YamlWriter(buffer, config);
		writer.write("test");
		writer.write("test");
		writer.close();
		assertEquals("test\n...\n--- test\n...\n", buffer.toString());
	}

	void checkWriterOutput (String example, String expected) throws Exception {
		StringWriter buffer = new StringWriter();
		YamlWriter writer = new YamlWriter(buffer, new YamlConfig());
		writer.write(example);
		writer.close();
		assertEquals(expected, buffer.toString());
	}

	public void testSingleQuotesDoubling () throws Exception {
		checkWriterOutput("abc", "abc\n");
		checkWriterOutput("abc def", "abc def\n");
		// single quotes around output as soon as we have a colon in the input
		checkWriterOutput("abc: def", "'abc: def'\n");
		checkWriterOutput("abc: def'ghi", "'abc: def''ghi'\n");
		checkWriterOutput("abc: def 'ghi", "'abc: def ''ghi'\n");
		// That was the initial bug:
		checkWriterOutput("A: 'X'", "'A: ''X'''\n");
	}

	public void testObjectField () throws Exception {
		ValueHolder object = new ValueHolder();
		object.value = "XYZ";
		ValueHolder roundTrip = (ValueHolder)roundTrip(object);
		assertEquals("XYZ", roundTrip.value);
	}

	public void testRootList () throws Exception {
		ArrayList list = new ArrayList();
		list.add(new PhoneNumber("206-555-1234"));
		list.add(new PhoneNumber("206-555-4321"));
		list.add(new PhoneNumber("206-555-6789"));
		list.add(new PhoneNumber("206-555-9876"));
		List roundTrip = roundTrip(list, ArrayList.class, new YamlConfig());
		assertEquals("206-555-1234", ((PhoneNumber)roundTrip.get(0)).number);
		assertEquals("206-555-4321", ((PhoneNumber)roundTrip.get(1)).number);
		assertEquals("206-555-6789", ((PhoneNumber)roundTrip.get(2)).number);
		assertEquals("206-555-9876", ((PhoneNumber)roundTrip.get(3)).number);

		Contact contact = new Contact();
		contact.name = "Bill";
		contact.phoneNumbers = list;
		roundTrip = roundTrip(contact, Contact.class, new YamlConfig()).phoneNumbers;
		assertEquals("206-555-1234", ((PhoneNumber)roundTrip.get(0)).number);
		assertEquals("206-555-4321", ((PhoneNumber)roundTrip.get(1)).number);
		assertEquals("206-555-6789", ((PhoneNumber)roundTrip.get(2)).number);
		assertEquals("206-555-9876", ((PhoneNumber)roundTrip.get(3)).number);
	}

	public void testConstructorProperties () throws Exception {
		ConstructorPropertiesSample object = new ConstructorPropertiesSample(1, 2, 3);
		ConstructorPropertiesSample roundTrip = (ConstructorPropertiesSample)roundTrip(object);
		assertEquals(1, roundTrip.getX());
		assertEquals(2, roundTrip.getY());
		assertEquals(3, roundTrip.getZ());
	}

	public void testConstructorPropertiesMixed () throws Exception {
		ConstructorPropertiesSampleMixed object = new ConstructorPropertiesSampleMixed(1, 2);
		object.setZ(3);
		ConstructorPropertiesSampleMixed roundTrip = (ConstructorPropertiesSampleMixed)roundTrip(object);
		assertEquals(1, roundTrip.getX());
		assertEquals(2, roundTrip.getY());
		assertEquals(3, roundTrip.getZ());
	}

	public void testScalarSerializer () throws Exception {
		YamlConfig config = new YamlConfig();
		config.setScalarSerializer(File.class, new ScalarSerializer<File>() {
			public File read (String value) throws YamlException {
				return new File(value);
			}

			public String write (File file) throws YamlException {
				return file.toString();
			}
		});
		ClassWithFile object = new ClassWithFile();
		object.file = new File("some/path/andFile.txt");
		ClassWithFile roundTrip = roundTrip(object, ClassWithFile.class, config);
		assertEquals(object.file, roundTrip.file);
	}

	public void testSimpleTypes_String_WithWriteConverter () throws Exception {
		String expected = "simple string\n";

		String string = "simple string";
		assertEquals(string, roundTripUpperCase(string, expected));
	}

	public void testSimpleTypes_Map_WithWriteConverter () throws Exception {
		String expected = "moo: cow\n";

		Map map = new HashMap();
		map.put("moo", "cow");
		assertEquals(map, roundTripUpperCase(map, expected));
	}

	public void testSimpleTypes_List_WithWriteConverter () throws Exception {
		String expected = "- moo\n" +
				"- cow\n";

		List list = new ArrayList();
		list.add("moo");
		list.add("cow");
		assertEquals(list, roundTripUpperCase(list, expected));
	}

	public void testSimpleTypes_NestedList_WithWriteConverter () throws Exception {
		String expected = "moo: cow\n" +
				"fubar: \n" +
				"- moo\n" +
				"- cow\n";

		List list = new ArrayList();
		list.add("moo");
		list.add("cow");

		Map map = new HashMap();
		map.put("moo", "cow");
		map.put("fubar", list);

		assertEquals(map, roundTripUpperCase(map, expected));
	}

	public void testSimpleTypes_StringArray_WithWriteConverter () throws Exception {
		String expected = "- moo\n" +
				"- meow\n" +
				"- cow\n" +
				"- gato\n";

		String[] stringArray = new String[] {"moo", "meow", "cow", "gato"};
		List result = (List)roundTripUpperCase(stringArray, expected);
		assertEquals(stringArray[0], result.get(0));
		assertEquals(stringArray[1], result.get(1));
		assertEquals(stringArray[2], result.get(2));
		assertEquals(stringArray[3], result.get(3));
	}

	public void testObjects_WithWriteConverter () throws Exception {
		String expected = "!com.esotericsoftware.yamlbeans.YamlWriterTest$Test\n" +
				"BOOLEANVALUE: true\n" +
				"CHILD: &1 !com.esotericsoftware.yamlbeans.YamlWriterTest$Test2\n" +
				"   VALUE: weeeee\n" +
				"DATE: 2015-07-10 17:45:56\n" +
				"DOUBLEVALUE: 1.5\n" +
				"FLOATVALUE: 1.2\n" +
				"INTVALUE: 123\n" +
				"LISTVALUES: !java.util.LinkedList\n" +
				"- woo\n" +
				"- !com.esotericsoftware.yamlbeans.YamlWriterTest$Test {}\n" +
				"- 123\n" +
				"- *1\n" +
				"STRINGVALUE: yay\n" +
				"VALUE: c\n";

		Test test = new Test();
		test.stringValue = "yay";
		test.intValue = 123;
		test.booleanValue = true;
		test.floatValue = 1.2f;
		test.doubleValue = 1.5;
		test.child = new Test2();
		((Test2)test.child).setValue("weeeee");
		test.listValues = new LinkedList();
		test.listValues.add("woo");
		test.listValues.add(new Test());
		test.listValues.add(123);
		test.listValues.add(test.child);
		test.value = Value.c;
		test.date = new GregorianCalendar(2015, 6, 10, 17, 45, 56).getTime();


		YamlConfig config = new YamlConfig();
		config.setPropertyNameConverter(new TestPropertyNameConverter());
		config.writeConfig.setAutoAnchor(true);
		Test result = roundTrip(test, Test.class, config, expected);
		assertEquals(test.stringValue, result.stringValue);
		assertEquals(test.intValue, result.intValue);
		assertEquals(test.booleanValue, result.booleanValue);
		assertEquals(test.floatValue, result.floatValue);
		assertEquals(test.doubleValue, result.doubleValue);
		assertEquals(test.listValues.size(), result.listValues.size());
		assertEquals(test.listValues.getClass(), result.listValues.getClass());
		assertEquals(((Test2)test.child).getValue(), "weeeee");
		assertEquals(test.value, result.value);
		assertTrue(result.child == result.listValues.get(3));
	}

	public void testObjectField_WithWriteConverter () throws Exception {
		String expected = "!com.esotericsoftware.yamlbeans.YamlWriterTest$ValueHolder\n" +
				"VALUE: XYZ\n";

		ValueHolder object = new ValueHolder();
		object.value = "XYZ";
		ValueHolder roundTrip = (ValueHolder)roundTripUpperCase(object, expected);
		assertEquals("XYZ", roundTrip.value);
	}

	public void testConstructorProperties_WithWriteConverter () throws Exception {
		String expected = "!com.esotericsoftware.yamlbeans.YamlWriterTest$ConstructorPropertiesSample\n" +
				"X: 1\n" +
				"Y: 2\n" +
				"Z: 3\n";

		ConstructorPropertiesSample object = new ConstructorPropertiesSample(1, 2, 3);
		ConstructorPropertiesSample roundTrip = (ConstructorPropertiesSample)roundTripUpperCase(object, expected);
		assertEquals(1, roundTrip.getX());
		assertEquals(2, roundTrip.getY());
		assertEquals(3, roundTrip.getZ());
	}

	public void testConstructorPropertiesMixed_WithPropertyConverter () throws Exception {
		String expected = "!com.esotericsoftware.yamlbeans.YamlWriterTest$ConstructorPropertiesSampleMixed\n" +
				"X: 1\n" +
				"Y: 2\n" +
				"Z: 3\n";

		ConstructorPropertiesSampleMixed object = new ConstructorPropertiesSampleMixed(1, 2);
		object.setZ(3);
		ConstructorPropertiesSampleMixed roundTrip = (ConstructorPropertiesSampleMixed)roundTripUpperCase(object, expected);
		assertEquals(1, roundTrip.getX());
		assertEquals(2, roundTrip.getY());
		assertEquals(3, roundTrip.getZ());
	}

	private Object roundTripUpperCase (Object object, String expectedOutput) throws Exception {
		YamlConfig config = new YamlConfig();
		config.setPropertyNameConverter(new TestPropertyNameConverter());
		return roundTrip(object, null, config, expectedOutput);
	}

	private Object roundTrip (Object object) throws Exception {
		return roundTrip(object, null, new YamlConfig());
	}

	private <T> T roundTrip (Object object, Class<T> type, YamlConfig config) throws Exception {
		StringWriter buffer = new StringWriter();

		YamlWriter writer = new YamlWriter(buffer, config);
		writer.write(object);
		writer.close();

		if (true) System.out.println(buffer);

		YamlReader reader = new YamlReader(buffer.toString(), config);
		return reader.read(type);
	}

	private <T> T roundTrip (Object object, Class<T> type, YamlConfig config, String expectedOutput) throws Exception {
		StringWriter buffer = new StringWriter();

		YamlWriter writer = new YamlWriter(buffer, config);
		writer.write(object);
		writer.close();

		if (true) System.out.println(buffer);

		assertEquals(expectedOutput, buffer.toString());

		YamlReader reader = new YamlReader(buffer.toString(), config);
		return reader.read(type);
	}

	static public class Test {
		public String stringValue;
		public int intValue;
		public boolean booleanValue;
		public float floatValue;
		public double doubleValue;
		public long longValue;
		public short shortValue;
		public char charValue = ' ';
		public byte byteValue;
		public List listValues;
		public Object[] arrayObjects;
		public String[] arrayStrings;
		public int[] arrayInts;
		public Test child;
		public Value value;
		public Date date;

		public String getMooCow () {
			return stringValue;
		}

		public void setMooCow (String value) {
			stringValue = value;
		}
	}

	static public class Test2 extends Test {
		String value;

		public Test2 () {
		}

		public Test2 (String value) {
			setValue(value);
		}

		public String getValue () {
			return value;
		}

		public void setValue (String value) {
			this.value = value;
		}
	}

	static public enum Value {
		a, b, c, d;
	}

	static public class ValueHolder<T> {
		public Object value;
	}

	static public class Contact {
		public String name;
		public List phoneNumbers;
	}

	static public class PhoneNumber {
		public String number;

		public PhoneNumber () {
		}

		public PhoneNumber (String number) {
			this.number = number;
		}
	}

	static public class ConstructorPropertiesSample {
		private int x, y, z;

		@ConstructorProperties({"x", "y", "z"})
		public ConstructorPropertiesSample (int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public int getX () {
			return x;
		}

		public int getY () {
			return y;
		}

		public int getZ () {
			return z;
		}
	}

	static public class ConstructorPropertiesSampleMixed {
		private int x, y, z;

		@ConstructorProperties({"x", "y"})
		public ConstructorPropertiesSampleMixed (int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX () {
			return x;
		}

		public int getY () {
			return y;
		}

		public int getZ () {
			return z;
		}

		public void setZ (int z) {
			this.z = z;
		}
	}

	static public class ClassWithFile {
		public File file;
	}

	static public class PrivateFields {
		private Long QTime = null;
		private List<String> result;

		@ConstructorProperties({"result", "QTime"})
		public PrivateFields (List<String> result, Long QTime) {
			this.result = result;
			this.QTime = QTime;
		}

		public List<String> getResult () {
			return result;
		}

		public Long getQTime () {
			return QTime;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PrivateFields other = (PrivateFields)obj;
			if (QTime == null) {
				if (other.QTime != null) return false;
			} else if (!QTime.equals(other.QTime)) return false;
			if (result == null) {
				if (other.result != null) return false;
			} else if (!result.equals(other.result)) return false;
			return true;
		}
	}
}

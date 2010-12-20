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

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

/**
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
public class YamlWriterTest extends TestCase {
	public void testPrivateFields () throws Exception {
		ArrayList list = new ArrayList();
		list.add("abc");
		list.add("123");
		roundTrip(new PrivateFields(list, 123l));
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

		@ConstructorProperties({"x", "y", "z"}) public ConstructorPropertiesSample (int x, int y, int z) {
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

		@ConstructorProperties({"x", "y"}) public ConstructorPropertiesSampleMixed (int x, int y) {
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

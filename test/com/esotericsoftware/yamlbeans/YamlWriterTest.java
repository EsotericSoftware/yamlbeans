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

import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a>
 */
public class YamlWriterTest extends TestCase {
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

	public void testSingleQuotes () throws Exception {
		Test test = new Test();
		test.stringValue = "" + //
			"if (chr != ' ' && chr != 't' && chr != 'r'\n" + //
			"\t&& chr != 'n') {\n" + //
			"\tbreak\n" + //
			"\t}\n";
		roundTrip(test);
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
}

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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/** @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class YamlReaderTest extends TestCase {
	public void testSimpleFields () throws Exception {
		Test test = read( //
		"stringValue: moo\ufec9moo\n" + //
			"intValue: !!int 123\n" + //
			"floatValue: 0.3\n" + //
			"doubleValue: 0.0002 # comment\n" + //
			"longValue: 999999\n" + //
			"shortValue: 125\n" + //
			"charValue: j\n" + //
			"testEnum: b\n" + //
			"byteValue: 14" //
		);

		assertEquals("moo\ufec9moo", test.stringValue);
		assertEquals(123, test.intValue);
		assertEquals(0.3f, test.floatValue);
		assertEquals(0.0002d, test.doubleValue);
		assertEquals(999999, test.longValue);
		assertEquals(125, test.shortValue);
		assertEquals('j', test.charValue);
		assertEquals(14, test.byteValue);
		assertEquals(TestEnum.b, test.testEnum);

		assertEquals(true, read("booleanValue: true").booleanValue);
		assertEquals(false, read("booleanValue: 123").booleanValue);
		assertEquals(false, read("booleanValue: 0").booleanValue);
		assertEquals(false, read("booleanValue: false").booleanValue);
	}

	public void testUnicodeStrings () throws Exception {
		Map<String, String> map = (Map)new YamlReader("moo\ufec9cow: sw\ufec9\ufec9t!").read();
		System.out.println(map + "\n\n");
		assertEquals("moo\ufec9cow", map.keySet().iterator().next());
		assertEquals("sw\ufec9\ufec9t!", map.values().iterator().next());
	}

	public void testSequence () throws Exception {
		Test test = read("listValues: [moo, 2]");
		assertEquals(2, test.listValues.size());
		assertEquals("moo", test.listValues.get(0));
		assertEquals("2", test.listValues.get(1));

		test = read("arrayObjects: [moo, 2]");
		assertEquals(2, test.arrayObjects.length);
		assertEquals("moo", test.arrayObjects[0]);
		assertEquals("2", test.arrayObjects[1]);

		test = read("arrayStrings: [moo, 2]");
		assertEquals(2, test.arrayStrings.length);
		assertEquals("moo", test.arrayStrings[0]);
		assertEquals("2", test.arrayStrings[1]);

		test = read("arrayInts: [34, 21]");
		assertEquals(2, test.arrayInts.length);
		assertEquals(34, test.arrayInts[0]);
		assertEquals(21, test.arrayInts[1]);

		test = read("listValues:\n- moo\n- 2");
		assertEquals(2, test.listValues.size());
		assertEquals("moo", test.listValues.get(0));
		assertEquals("2", test.listValues.get(1));

		test = read("listValues:\n  - moo\n  - 2");
		assertEquals(2, test.listValues.size());
		assertEquals("moo", test.listValues.get(0));
		assertEquals("2", test.listValues.get(1));

		test = read("arrayObjects:\n  - moo\n  - 2");
		assertEquals(2, test.arrayObjects.length);
		assertEquals("moo", test.arrayObjects[0]);
		assertEquals("2", test.arrayObjects[1]);

		test = read("arrayStrings:\n  - moo\n  - 2");
		assertEquals(2, test.arrayStrings.length);
		assertEquals("moo", test.arrayStrings[0]);
		assertEquals("2", test.arrayStrings[1]);

		test = read("arrayInts:\n  - 34\n  - 21");
		assertEquals(2, test.arrayInts.length);
		assertEquals(34, test.arrayInts[0]);
		assertEquals(21, test.arrayInts[1]);
	}

	public void testAnchors () throws Exception {
		Test test = read("child1: &myanchor\n  stringValue: meow\n  intValue: 4321\nintValue: 3\nchild2: *myanchor");
		assertEquals(test.intValue, 3);
		assertTrue(test.child1 == test.child2);
		assertEquals(test.child2.stringValue, "meow");
		assertEquals(test.child1.intValue, 4321);
		assertNull(test.stringValue);
	}

	public void testSingleQuotes () throws Exception {
		String value = "moo:| " + //
			"   if (chr != ' ' && chr != '\\t' && chr != '\\r'\n" + //
			"   && chr != '\\n') {\n" + //
			"   break\n" + //
			"   }\n";
		System.out.println(11111111);
		System.out.println(new YamlReader(value).read());
	}

	static public class Node {
		public Node left, right, parent;
		public String value;
	}

	public void testCyclicReferences () throws Exception {
		String yaml = "" + //
			"&1 !node\n" + //
			"left: !node\n" + //
			"   parent: *1\n" + //
			"   value: Left Node\n" + //
			"right: !node\n" + //
			"   parent: *1\n" + //
			"   value: Right Node\n" + //
			"value: The Root\n";

		YamlReader reader = new YamlReader(yaml);
		reader.getConfig().setClassTag("node", Node.class);
		Node root = (Node)reader.read();
		assertEquals("The Root", root.value);
		assertEquals("Left Node", root.left.value);
		assertEquals("Right Node", root.right.value);
		assertTrue(root.left.parent == root);
		assertTrue(root.left.left == null);
		assertTrue(root.left.right == null);
		assertTrue(root.right.parent == root);
		assertTrue(root.right.left == null);
		assertTrue(root.right.right == null);
	}

	public void testReaderMaintainsOrderWhenReadingMap() throws Exception {
		YamlReader reader = new YamlReader("a: b\nkey1: value1\nc: d\n");
		Map map = (Map)reader.read();
		Iterator entrySetIterator = map.entrySet().iterator();

		Map.Entry mapEntry = (Map.Entry)entrySetIterator.next();
		assertEquals(mapEntry.getKey(), "a");
		assertEquals(mapEntry.getValue(), "b");

		mapEntry = (Map.Entry)entrySetIterator.next();
		assertEquals(mapEntry.getKey(), "key1");
		assertEquals(mapEntry.getValue(), "value1");

		mapEntry = (Map.Entry)entrySetIterator.next();
		assertEquals(mapEntry.getKey(), "c");
		assertEquals(mapEntry.getValue(), "d");
	}

	private Test read (String yaml) throws Exception {
		if (true) {
			System.out.println(yaml);
			System.out.println("===");
			Object obj = new YamlReader(yaml).read(null);
			System.out.println(obj);
			System.out.println();
			System.out.println();
		}
		return new YamlReader(yaml).read(Test.class);
	}

	static public class Test {
		public String stringValue;
		public int intValue;
		public boolean booleanValue;
		public float floatValue;
		public double doubleValue;
		public long longValue;
		public short shortValue;
		public char charValue;
		public byte byteValue;
		public List listValues;
		public Object[] arrayObjects;
		public String[] arrayStrings;
		public int[] arrayInts;
		public Test child1;
		public Test child2;
		public TestEnum testEnum;
	}

	static public enum TestEnum {
		a, b, c
	}

	public void testPropertyElementType () throws Exception {
		YamlConfig config = new YamlConfig();
		config.setPropertyElementType(Moo1.class, "ints", Value.class);
		config.setPropertyElementType(Moo2.class, "ints", Value.class);
		assertEquals(Value.class, new YamlReader("ints:\n- value: 1\n- value: 2", config).read(Moo1.class).getInts().get(0)
			.getClass());
		assertEquals(2, new YamlReader("ints:\n- value: 1\n- value: 2", config).read(Moo1.class).getInts().get(1).getValue());
		assertEquals(Value.class, new YamlReader("ints:\n- value: 3\n- value: 4", config).read(Moo2.class).getInts().get(0)
			.getClass());
		assertEquals(String.class, new YamlReader("strings:\n- 3\n- 4", config).read(Moo1.class).getStrings().get(0).getClass());
	}

	public void testConstructorArgs () throws Exception {
		YamlConfig config = new YamlConfig();
		config.readConfig.setConstructorParameters(ConstructorArgs.class, new Class[] {int.class, int.class}, new String[] {"x",
			"y"});
		ConstructorArgs object = new YamlReader("x: 1\ny: 2", config).read(ConstructorArgs.class);
		assertEquals(1, object.getX());
		assertEquals(2, object.getY());
		assertEquals(0, object.getZ());
		object = new YamlReader("x: 3\ny: 4\nz: 5", config).read(ConstructorArgs.class);
		assertEquals(3, object.getX());
		assertEquals(4, object.getY());
		assertEquals(5, object.getZ());
	}

	static public class Moo1 {
		private List<Value> ints;
		public List strings;

		public List<Value> getInts () {
			return ints;
		}

		public void setInts (List<Value> ints) {
			this.ints = ints;
		}

		public List getStrings () {
			return strings;
		}

		public void setStrings (List strings) {
			this.strings = strings;
		}
	}

	static public class Moo2 {
		private List<Value> ints;

		public List<Value> getInts () {
			return ints;
		}

		public void setInts (List<Value> ints) {
			this.ints = ints;
		}
	}

	static public class Value {
		public int value;

		public int getValue () {
			return value;
		}

		public void setValue (int value) {
			this.value = value;
		}
	}

	static public class ConstructorArgs {
		private int x, y, z;

		public ConstructorArgs (int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX () {
			return x;
		}

		public void setX (int x) {
			this.x = x;
		}

		public int getY () {
			return y;
		}

		public void setY (int y) {
			this.y = y;
		}

		public int getZ () {
			return z;
		}

		public void setZ (int z) {
			this.z = z;
		}
	}

	static class ACD {
		public int a, c, d = 4;
	}

	public void testIgnoreUnknownProperties () {
		String input = "a: 1\nb: 2\nc: 3";
		YamlConfig config = new YamlConfig();
		try {
			new YamlReader(input, config).read(ACD.class);
			fail("Unknown properties were not supposed to be allowed.");
		} catch (YamlException e) {
		}

		config.readConfig.setIgnoreUnknownProperties(true);
		try {
			ACD pojo = new YamlReader(input, config).read(ACD.class);
			assertEquals(1, pojo.a);
			assertEquals(3, pojo.c);
			assertEquals(4, pojo.d);
		} catch (YamlException e) {
			fail("Unknown properties were supposed to be allowed.");
		}
	}
	
	public void testDuplicateKeysAreNotAllowedIfAllowDuplicateIsSetSo () {
		String inputWithDuplicates = "a: 1\na: 2\nc: 3";
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.setAllowDuplicates(false);
		YamlReader yamlReader = new YamlReader(inputWithDuplicates, yamlConfig);
		try {
			yamlReader.read();
			fail("Duplicates should not have been allowed.");
		} catch (YamlException e) {
		}

		String inputWithoutDuplicates = "a: 1\nb: 2\nc: 3";
		YamlReader yamlReader1 = new YamlReader(inputWithoutDuplicates, yamlConfig);
		try {
			yamlReader1.read();
		} catch (YamlException e) {
			fail("Duplicates should have been allowed.");
		}
	}

	public void testDuplicateKeysAreAllowedIfAllowDuplicateIsSetSo () {
		String inputWithDuplicates = "a: 1\na: 2\nc: 3";
		YamlReader yamlReader = new YamlReader(inputWithDuplicates);
		try {
			yamlReader.read();
		} catch (YamlException e) {
			e.printStackTrace();
			fail("Duplicates should have been allowed.");
		}

		String inputWithoutDuplicates = "a: 1\nb: 2\nc: 3";
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.setAllowDuplicates(false);
		YamlReader yamlReader1 = new YamlReader(inputWithoutDuplicates, yamlConfig);
		try {
			yamlReader1.read();
		} catch (YamlException e) {
			e.printStackTrace();
			fail("Duplicates should have been allowed.");
		}
	}

	private static class TypeTagIgnoringReader extends YamlReader {

		public TypeTagIgnoringReader(String yaml) {
			super(yaml);
		}
		
		@Override
		protected Class<?> findTagClass(String tag, ClassLoader classLoader) {
			// Ignore all tags.
			return null;
		}
		
	}
	
	public void testIgnoreTypeTagsTopLevel () throws YamlException {
		// We are parsing this document that was output by another program using YamlBeans, that includes
		// a type tag for a class that we don't have on our classpath.
		String input = "!com.example.not.on.classpath.Fish\n" + //
				"species: Walleye\n" + //
				"weight: 24";
	
		try {
			new YamlReader(input).read(Map.class);
			fail("A type tag with an unknown class fails to parse, even if we ask for it as a simple Map.");
		} catch (YamlException e) {
			// Expected behavior.
		}
		
		// If we ignore type tags, the type tag in the document does not override our request for a Map.
		Map<?, ?> map = new TypeTagIgnoringReader(input).read(Map.class);
		assertEquals("Walleye", map.get("species"));		
	}
	
	static class Lake {
		public String name;
		public List<Fish> fish;
	}
	
	static class Fish {
		public String species;
		public int weight;
	}
	
	public void testIgnoreTypeTagsEmbedded () throws YamlException {
		// We are parsing this document that was output by another program using YamlBeans, that includes
		// type tags for multiple classes that we don't have on our classpath. One of those type tags is
		// embedded within the document, not at the top level.
		String input = "!com.example.not.on.classpath.Lake\n" + //
				"name: Superior\n" + //
				"fish:\n" + 
				"  - !com.example.not.on.classpath.Fish\n" + //
				"    species: Walleye\n" + //
				"    weight: 24";
	
		try {
			new YamlReader(input).read(Lake.class);
			fail("A type tag with an unknown class fails to parse, even if we ask for it as a known class.");
		} catch (YamlException e) {
			// Expected behavior.
		}
		
		// If we ignore type tags, we can specify the return type & embedded types instead of failing to parse.
		Lake lake = new TypeTagIgnoringReader(input).read(Lake.class);
		assertEquals("Superior", lake.name);
		assertEquals(1, lake.fish.size());
		assertEquals("Walleye", lake.fish.get(0).species);
	}

	/**
	 * issue #105
	 *
	 * @throws YamlException
	 */
	public void testGuessNumberTypes() throws YamlException {

		long invoice = 3484312312313131l;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("invoice", invoice);
		map.put("date", "2001-01-23");

		YamlConfig config = new YamlConfig();
		config.readConfig.guessNumberTypes = true;
		StringWriter sw = new StringWriter();
		YamlWriter writer = new YamlWriter(sw, config);
		writer.write(map);
		writer.close();
		YamlReader reader = new YamlReader(sw.toString(), config);
		Map<String, Object> result = (Map<String, Object>) reader.read();
		long invoiceValue = (Long) result.get("invoice");
		assertEquals(invoice, invoiceValue);

		Double money = 123.456;
		map.put("money", money);

		writer = new YamlWriter(sw, config);
		writer.write(map);
		writer.close();

		reader = new YamlReader(sw.toString(), config);
		result = (Map<String, Object>) reader.read();
		double moneyValue = (Double) result.get("money");
		assertEquals(money, moneyValue);
	}

    /**
     * issue #34
     *
     * @throws YamlException
     */
    public void testWhiteSpaceCharacters() throws YamlException {

        String stringWithSpace = "test test";
        YamlReader reader = new YamlReader(stringWithSpace);
        assertEquals(stringWithSpace, reader.read(String.class));

        String stringWithTab = "test\ttesttest";
        reader = new YamlReader(stringWithTab);
        assertEquals(stringWithTab, reader.read(String.class));

        String stringWithSpaceAndTab = "test test\ttest \ttest";
        reader = new YamlReader(stringWithSpaceAndTab);
        assertEquals(stringWithSpaceAndTab, reader.read(String.class));
    }
}

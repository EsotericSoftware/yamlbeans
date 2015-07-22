package com.esotericsoftware.yamlbeans;

import com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericTest extends TestCase {
	private static final String YAML =
			"integerList: \n"+
			"- 1\n"+
			"- 100500\n"+
			"- 10\n"+
			"stringMap: \n"+
			"   a: av\n"+
			"   b: bv\n"+
			"structList: \n"+
			"-  i: 10\n"+
			"   str: aaa\n"+
			"-  i: 20\n"+
			"   str: bbb\n"+
			"structMap: \n"+
			"   a: \n"+
			"      i: 1\n"+
			"      str: aa\n"+
			"   b: \n"+
			"      i: 2\n"+
			"      str: ab\n";

	public void testRead () throws YamlException {
		Test test = createTest();

		Test read = new YamlReader(YAML).read(Test.class);
		Assert.assertEquals(test, read);
	}

	public void testWrite () throws YamlException {
		Test test = createTest();
		StringWriter stringWriter = new StringWriter();

		YamlWriter yamlWriter = new YamlWriter(stringWriter);
		yamlWriter.getConfig().writeConfig.setWriteClassname(WriteClassName.NEVER);
		yamlWriter.write(test);
		yamlWriter.close();

		Assert.assertEquals(YAML, stringWriter.getBuffer().toString());
	}

	private Test createTest () {
		Map<String, String> stringMap = new HashMap<String, String>();
		stringMap.put("a", "av");
		stringMap.put("b", "bv");

		Map<String,Struct> structMap = new HashMap<String, Struct>();
		structMap.put("a", new Struct(1, "aa"));
		structMap.put("b", new Struct(2, "ab"));

		List<Integer> integerList = new ArrayList<Integer>();
		integerList.add(1);
		integerList.add(100500);
		integerList.add(10);

		List<Struct> structList = new ArrayList<Struct>();
		structList.add(new Struct(10, "aaa"));
		structList.add(new Struct(20, "bbb"));

		Test test = new Test();
		test.stringMap = stringMap;
		test.structMap = structMap;
		test.integerList = integerList;
		test.structList =  structList;

		return test;
	}

	static class Struct {
		public int i;
		public String str;

		Struct (int i, String str) {
			this.i = i;
			this.str = str;
		}

		Struct () {
		}

		@Override
		public boolean equals (Object o) {
			if (this == o) { return true; }
			if (!(o instanceof Struct)) { return false; }

			Struct struct = (Struct) o;

			if (i != struct.i) { return false; }
			if (str != null ? !str.equals(struct.str) : struct.str != null) { return false; }

			return true;
		}

		@Override
		public int hashCode () {
			int result = i;
			result = 31 * result + (str != null ? str.hashCode() : 0);
			return result;
		}
	}

	static class Test {
		public Map<String, String> stringMap;
		public Map<String, Struct> structMap;
		public List<Integer> integerList;
		public List<Struct> structList;

		@Override
		public boolean equals (Object o) {
			if (this == o) { return true; }
			if (!(o instanceof Test)) { return false; }

			Test test = (Test) o;

			if (integerList != null ? !integerList.equals(test.integerList) : test.integerList != null) { return false; }
			if (stringMap != null ? !stringMap.equals(test.stringMap) : test.stringMap != null) { return false; }
			if (structList != null ? !structList.equals(test.structList) : test.structList != null) { return false; }
			if (structMap != null ? !structMap.equals(test.structMap) : test.structMap != null) { return false; }

			return true;
		}

		@Override
		public int hashCode () {
			int result = stringMap != null ? stringMap.hashCode() : 0;
			result = 31 * result + (structMap != null ? structMap.hashCode() : 0);
			result = 31 * result + (integerList != null ? integerList.hashCode() : 0);
			result = 31 * result + (structList != null ? structList.hashCode() : 0);
			return result;
		}
	}
}

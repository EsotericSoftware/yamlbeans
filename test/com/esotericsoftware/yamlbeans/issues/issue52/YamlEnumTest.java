package com.esotericsoftware.yamlbeans.issues.issue52;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class YamlEnumTest extends TestCase {

	public static class TestObject {
		public EnumIface[] enumArray = new EnumIface[] { CustomEnum.V1, CustomEnum.V2 };
	}

	public interface EnumIface {

	}

	public enum CustomEnum implements EnumIface {
		V1, V2
	}

	public static class NonEnum implements EnumIface {
		public String v1, v2;

		public NonEnum() {
		}

		public NonEnum(String v1, String v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
	}

	public YamlEnumTest() {
	}

	public static String write(Object object) throws IOException {
		StringWriter stringWriter = new StringWriter();
		YamlWriter writer = new YamlWriter(stringWriter);

		writer.getConfig().setPrivateFields(false);
		writer.write(object);
		writer.close();

		String string = stringWriter.toString();
		System.out.println("Wrote: \n" + string);

		return string;
	}

	public void read(String string) throws IOException {
		YamlReader reader = new YamlReader(string);
		reader.read(TestObject.class);
		reader.close();
	}

	public void testNonEnum() throws IOException {
		TestObject testObject = new TestObject();
		testObject.enumArray = new EnumIface[] { new NonEnum("v1", "v2"), new NonEnum("v1", "v2") };

		read(write(testObject));
	}

	public void testOnlyEnums() throws IOException {
		TestObject testObject = new TestObject();
		testObject.enumArray = new EnumIface[] { CustomEnum.V1, CustomEnum.V2 };

		read(write(testObject));
	}

	public void testMixEnumsAndNonEnums() throws IOException {
		TestObject testObject = new TestObject();
		testObject.enumArray = new EnumIface[] { new NonEnum("v1", "v2"), CustomEnum.V2 };

		read(write(testObject));
	}

	public void testFixedEnum() throws IOException {
		read("!com.esotericsoftware.yamlbeans.issues.issue52.YamlEnumTest$TestObject\n" + "enumArray:\n"
				+ "- !com.esotericsoftware.yamlbeans.issues.issue52.YamlEnumTest$CustomEnum V1\n"
				+ "- !com.esotericsoftware.yamlbeans.issues.issue52.YamlEnumTest$CustomEnum V2\n");
	}
}

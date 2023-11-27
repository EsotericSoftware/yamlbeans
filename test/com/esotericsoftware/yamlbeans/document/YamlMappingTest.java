package com.esotericsoftware.yamlbeans.document;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;

public class YamlMappingTest {

	private YamlMapping yamlMapping = null;

	@Before
	public void setUp() throws YamlException {
		String yaml = "key1: value1\nkey2: value2\nkey3: value3\n";
		YamlDocumentReader reader = new YamlDocumentReader(yaml);
		yamlMapping = (YamlMapping) reader.read();
		yamlMapping.setAnchor("mapping");
		yamlMapping.setTag("HashMap");
	}

	@Test
	public void testSize() {
		assertEquals(3, yamlMapping.size());
	}

	@Test
	public void testAddEntry() throws YamlException {
		YamlScalar key = new YamlScalar();
		key.setValue("test");
		YamlScalar value = new YamlScalar();
		value.setValue("111");
		YamlEntry entry = new YamlEntry(key, value);
		yamlMapping.addEntry(entry);

		assertEquals(entry, yamlMapping.getEntry("test"));
	}

	@Test
	public void testDeleteEntry() {
		assertEquals(true, yamlMapping.deleteEntry("key1"));
		assertEquals(false, yamlMapping.deleteEntry("key4"));
	}

	@Test
	public void testGetEntryByKey() throws YamlException {
		assertEquals(true, yamlMapping.getEntry("key1") != null);
		assertEquals(true, yamlMapping.getEntry("key4") == null);
	}

	@Test
	public void testGetEntryByIndex() throws YamlException {
		assertEquals(true, yamlMapping.getEntry(0) != null);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetEntryByIndexThrowsIndexOutOfBoundsException() throws YamlException {
		yamlMapping.getEntry(3);
	}

	@Test
	public void testToString() {
		assertEquals("&mapping  !HashMap{key1:value1,key2:value2,key3:value3}", yamlMapping.toString());
	}

	@Test
	public void testSetEntryUseBooleanValue() throws YamlException {
		yamlMapping.setEntry("test", true);
		assertEquals("true", ((YamlScalar) yamlMapping.getEntry("test").getValue()).getValue());
	}

	@Test
	public void testSetEntryUseNumberValue() throws YamlException {
		yamlMapping.setEntry("test", 1);
		assertEquals("1", ((YamlScalar) yamlMapping.getEntry("test").getValue()).getValue());
	}

	@Test
	public void testSetEntryUseStringValue() throws YamlException {
		yamlMapping.setEntry("test", "test");
		assertEquals("test", ((YamlScalar) yamlMapping.getEntry("test").getValue()).getValue());
	}

	@Test(expected = YamlException.class)
	public void testGetElementByItemThrowsYamlException() throws YamlException {
		yamlMapping.getElement(0);
	}

	@Test(expected = YamlException.class)
	public void testDeleteElementThrowsYamlException() throws YamlException {
		yamlMapping.deleteElement(0);
	}

	@Test(expected = YamlException.class)
	public void testSetElementUseBooleanThrowsYamlException() throws YamlException {
		yamlMapping.setElement(0, true);
	}

	@Test(expected = YamlException.class)
	public void testSetElementUseNumberThrowsYamlException() throws YamlException {
		yamlMapping.setElement(0, 1);
	}

	@Test(expected = YamlException.class)
	public void testSetElementUseStringThrowsYamlException() throws YamlException {
		yamlMapping.setElement(0, "test");
	}

	@Test(expected = YamlException.class)
	public void testSetElementUseYamlElementThrowsYamlException() throws YamlException {
		YamlElement YamlElement = new YamlScalar();
		yamlMapping.setElement(0, YamlElement);
	}

	@Test(expected = YamlException.class)
	public void testAddElementUseBooleanThrowsYamlException() throws YamlException {
		yamlMapping.addElement(true);
	}

	@Test(expected = YamlException.class)
	public void testAddElementUseNumberThrowsYamlException() throws YamlException {
		yamlMapping.addElement(1);
	}

	@Test(expected = YamlException.class)
	public void testAddElementUseStringThrowsYamlException() throws YamlException {
		yamlMapping.addElement("test");
	}

	@Test(expected = YamlException.class)
	public void testAddElementUseYamlElementThrowsYamlException() throws YamlException {
		YamlElement YamlElement = new YamlScalar();
		yamlMapping.addElement(YamlElement);
	}
}

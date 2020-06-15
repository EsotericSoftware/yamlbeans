package com.esotericsoftware.yamlbeans.document;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;

public class YamlSequenceTest {
	private YamlSequence yamlSequence = null;

	@Before
	public void setUp() throws YamlException {
		String yaml = "- 1\n- 2\n- 3\n";
		YamlDocumentReader reader = new YamlDocumentReader(yaml);
		yamlSequence = (YamlSequence) reader.read();
		yamlSequence.setAnchor("sequence");
		yamlSequence.setTag("LinkedList");
	}

	@Test
	public void testSize() {
		assertEquals(3, yamlSequence.size());
	}

	@Test
	public void testAddElement() throws YamlException {
		YamlElement yamlElement = new YamlScalar();
		yamlSequence.addElement(yamlElement);
		assertEquals(yamlElement, yamlSequence.getElement(3));
	}

	@Test
	public void testDeleteElement() throws YamlException {
		yamlSequence.deleteElement(0);
		assertEquals(2, yamlSequence.size());
	}

	@Test
	public void testToStrig() {
		assertEquals("&sequence  !LinkedList[1,2,3]", yamlSequence.toString());
	}

	@Test(expected = YamlException.class)
	public void testGetEntryByKeyThrowsYamlException() throws YamlException {
		yamlSequence.getEntry("key");
	}

	@Test(expected = YamlException.class)
	public void testGetEntryByIndexThrowsYamlException() throws YamlException {
		yamlSequence.getEntry(0);
	}

	@Test(expected = YamlException.class)
	public void testDeleteEntryByKeyThrowsYamlException() throws YamlException {
		yamlSequence.deleteEntry("key");
	}

	@Test(expected = YamlException.class)
	public void testSetEntryUseBooleanValueThrowsYamlException() throws YamlException {
		yamlSequence.setEntry("key", true);
	}

	@Test(expected = YamlException.class)
	public void testSetEntryUseNumberValueThrowsYamlException() throws YamlException {
		yamlSequence.setEntry("key", 1);
	}

	@Test(expected = YamlException.class)
	public void testSetEntryUseStringValueThrowsYamlException() throws YamlException {
		yamlSequence.setEntry("key", "111");
	}

	@Test(expected = YamlException.class)
	public void testSetEntryUseYamlElementValueThrowsYamlException() throws YamlException {
		YamlElement yamlElement = new YamlScalar();
		yamlSequence.setEntry("key", yamlElement);
	}

	@Test
	public void testSetElementUseBooleanValue() throws YamlException {
		yamlSequence.setElement(0, true);
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(0);
		assertEquals("true", yamlScalar.getValue());
	}

	@Test
	public void testSetElementUseNumberValue() throws YamlException {
		yamlSequence.setElement(0, 1);
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(0);
		assertEquals("1", yamlScalar.getValue());
	}

	@Test
	public void testSetElementUseStringValue() throws YamlException {
		yamlSequence.setElement(0, "testStr");
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(0);
		assertEquals("testStr", yamlScalar.getValue());
	}

	@Test
	public void testSetElementUseYamlElementValue() throws YamlException {
		YamlElement yamlElement = new YamlScalar();
		yamlSequence.setElement(0, yamlElement);
		assertEquals(yamlElement, yamlSequence.getElement(0));
	}

	@Test
	public void testAddElementUseBoolean() throws YamlException {
		yamlSequence.addElement(true);
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(yamlSequence.size() - 1);
		assertEquals("true", yamlScalar.getValue());
	}

	@Test
	public void testAddElementUseNumber() throws YamlException {
		yamlSequence.addElement(1);
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(yamlSequence.size() - 1);
		assertEquals("1", yamlScalar.getValue());
	}

	@Test
	public void testAddElementUseString() throws YamlException {
		yamlSequence.addElement("testStr");
		YamlScalar yamlScalar = (YamlScalar) yamlSequence.getElement(yamlSequence.size() - 1);
		assertEquals("testStr", yamlScalar.getValue());
	}
}

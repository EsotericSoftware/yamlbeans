package com.esotericsoftware.yamlbeans.document;

import java.io.StringWriter;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName;
import com.esotericsoftware.yamlbeans.document.YamlDocument;
import com.esotericsoftware.yamlbeans.document.YamlDocumentReader;

import static org.junit.Assert.*;

public class YamlDocumentTest {
	
	@Test
	public void testThatTaggedDocumentIsCopied() throws Exception {
		testEquals("--- !someTag\nscalar: value\n");
	}

	@Test
	public void testThatScalarValueIsCopied() throws Exception {
		testEquals("scalar: value\n");
	}

	@Test
	public void testThatTaggedScalarValueIsCopied() throws Exception {
		testEquals("scalar: value !someTag\n");
	}

	@Test
	public void testThatAnchoredScalarValueIsCopied() throws Exception {
		testEquals("scalar: &anchor value\n");
	}

	@Test
	public void testThatAliasedScalarValueIsCopied() throws Exception {
		testEquals("scalar: *alias\n");
	}

	@Test
	public void testThatSequenceValueIsCopied() throws Exception {
		testEquals("-  scalar1: value\n-  scalar2: value\n");
	}

	@Test
	public void testThatTaggedSequenceValueIsCopied() throws Exception {
		testEquals("-  scalar1: value !someTag1\n-  scalar2: value !someTag2\n");
	}

	@Test
	public void testThatAnchoredSequenceValueIsCopied() throws Exception {
		testEquals("sequence: &anchor\n-  scalar1: value\n-  scalar2: value\n");
	}

	@Test
	public void testThatAliasedSequenceValueIsCopied() throws Exception {
		testEquals("sequence: *alias\n");
	}

	@Test
	public void testThatMappingValueIsCopied() throws Exception {
		testEquals("mapping: \n   scalar1: value\n   scalar2: value\n");
	}

	@Test
	public void testThatTaggedMappingValueIsCopied() throws Exception {
		testEquals("mapping: !someTag1\n   scalar1: value\n   scalar2: value !someTag2\n");
	}


	@Test
	public void testThatAnchoredMappingValueIsCopied() throws Exception {
		testEquals("mapping: &anchor\n   scalar1: value\n   scalar2: value\n");
	}

	@Test
	public void testThatAliasedMappingValueIsCopied() throws Exception {
		testEquals("mapping: *alias\n");
	}

	@Test
	public void testThatMappingEntryIsChanged() throws YamlException {
		YamlDocument yaml = readDocument("scalar: value\n");
		yaml.setEntry("scalar", 123);
		String actual = writeDocument(yaml);
		assertEquals("scalar: 123\n", actual);
	}
	
	@Test
	public void testThatMappingEntryIsAdded() throws YamlException {
		YamlDocument yaml = readDocument("scalar: value\n");
		yaml.setEntry("scalar2", 123);
		String actual = writeDocument(yaml);
		assertEquals("scalar: value\nscalar2: 123\n", actual);
	}
	
	@Test
	public void testThatMappingEntryIsRemoved() throws YamlException {
		YamlDocument yaml = readDocument("scalar: value\nscalar2: 123\n");
		yaml.deleteEntry("scalar2");
		String actual = writeDocument(yaml);
		assertEquals("scalar: value\n", actual);
	}
	
	@Test
	public void testThatSequenceItemIsChanged() throws YamlException {
		YamlDocument yaml = readDocument("- value\n");
		yaml.setElement(0, 123);
		String actual = writeDocument(yaml);
		assertEquals("- 123\n", actual);
	}
	
	@Test
	public void testThatSequenceItemIsAdded() throws YamlException {
		YamlDocument yaml = readDocument("- value\n");
		yaml.addElement(123);
		String actual = writeDocument(yaml);
		assertEquals("- value\n- 123\n", actual);
	}
	
	@Test
	public void testThatSequenceItemIsRemoved() throws YamlException {
		YamlDocument yaml = readDocument("- value\n- 123\n");
		yaml.deleteElement(0);
		String actual = writeDocument(yaml);
		assertEquals("- 123\n", actual);
	}


	private YamlDocument readDocument(String yaml) throws YamlException {
		YamlDocumentReader reader = new YamlDocumentReader(yaml);
		return reader.read();
	}
	
	private String writeDocument(YamlDocument yaml) throws YamlException {
		StringWriter writer = new StringWriter();
		YamlConfig config = new YamlConfig();
		config.writeConfig.setExplicitFirstDocument(yaml.getTag()!=null);
		config.writeConfig.setWriteClassname(WriteClassName.NEVER);
		config.writeConfig.setAutoAnchor(false);
		YamlWriter yamlWriter = new YamlWriter(writer, config);
		yamlWriter.write(yaml);
		yamlWriter.close();	
		return writer.toString();
	}

	private void testEquals(String yaml) throws Exception {
		YamlDocument document = readDocument(yaml);
		String actual = writeDocument(document);
		assertEquals(yaml, actual);
	}
	

}

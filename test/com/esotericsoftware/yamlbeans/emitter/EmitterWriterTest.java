package com.esotericsoftware.yamlbeans.emitter;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.esotericsoftware.yamlbeans.YamlConfig.Quote;

public class EmitterWriterTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Test
	public void testWriteDoubleQuoted() throws YamlException {
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setQuoteChar(Quote.DOUBLE);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("\u0001\u0011\u0111\u1111\u0007");
		yamlWriter.close();

		assertEquals("\"\\u0001\\u0011\\u0111\\u1111\\a\"" + LINE_SEPARATOR, stringWriter.toString());

		stringWriter = new StringWriter();
		yamlConfig.writeConfig.setWrapColumn(5);
		yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("testt est");
		yamlWriter.close();
		assertEquals("\"testt\\" + LINE_SEPARATOR + "   \\ e\\" + LINE_SEPARATOR + "   st\"" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test
	public void testWriteSingleQuoted() throws YamlException {
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setQuoteChar(Quote.SINGLE);

		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("\ntest");
		yamlWriter.close();
		assertEquals("\'" + LINE_SEPARATOR + "   test\'" + LINE_SEPARATOR, stringWriter.toString());

	}

	@Test
	public void testWriteFolded() throws YamlException {
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setQuoteChar(Quote.FOLDED);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("111\n222 333");
		yamlWriter.close();
		assertEquals(">-" + LINE_SEPARATOR + "   111" + LINE_SEPARATOR + LINE_SEPARATOR + "   222 333" + LINE_SEPARATOR,
				stringWriter.toString());
	}

	@Test
	public void testWriteLiteral() throws YamlException {
		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.writeConfig.setQuoteChar(Quote.LITERAL);
		StringWriter stringWriter = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(stringWriter, yamlConfig);
		yamlWriter.write("111\n222");
		yamlWriter.close();
		assertEquals("|-" + LINE_SEPARATOR + "   111" + LINE_SEPARATOR + "   222" + LINE_SEPARATOR,
				stringWriter.toString());
	}
}

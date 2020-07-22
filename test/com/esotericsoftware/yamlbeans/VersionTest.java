package com.esotericsoftware.yamlbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class VersionTest {

	@Test
	public void testV1_0() {
		Version version = Version.V1_0;
		assertEquals(1, version.getMajor());
		assertEquals(0, version.getMinor());
		assertEquals("1.0", version.toString());
	}

	@Test
	public void testV1_1() {
		Version version = Version.V1_1;
		assertEquals(1, version.getMajor());
		assertEquals(1, version.getMinor());
		assertEquals("1.1", version.toString());
	}

	@Test
	public void testGetVersion() {
		assertEquals(Version.V1_0, Version.getVersion("1.0"));
		assertEquals(Version.V1_1, Version.getVersion("1.1"));
		assertNull(Version.getVersion(null));
		assertNull(Version.getVersion("0"));
		assertNull(Version.getVersion(".0"));
		assertNull(Version.getVersion("0.1"));
		assertNull(Version.getVersion("1.2"));
		assertNull(Version.getVersion("2.0"));
		assertNull(Version.getVersion("1.a"));
		assertNull(Version.getVersion("a.1"));
	}
}

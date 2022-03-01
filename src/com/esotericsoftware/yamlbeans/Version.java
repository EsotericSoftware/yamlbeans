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

/** Represents the version of a YAML document.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class Version {

	public static final Version V1_0 = new Version(1, 0);

	public static final Version V1_1 = new Version(1, 1);

	/**
	 * YAML 1.1
	 */
	public static final Version DEFAULT_VERSION = V1_1;

	private final int major;
	private final int minor;

	private Version (int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public static Version getVersion(String value) {
		Version version = null;
		if (value != null) {
			int dotIndex = value.indexOf('.');
			int major = 0;
			int minor = 0;
			if (dotIndex > 0) {
				try {
					major = Integer.parseInt(value.substring(0, dotIndex));
					minor = Integer.parseInt(value.substring(dotIndex + 1));
				} catch (NumberFormatException e) {
					return null;
				}
			}

			if (major == V1_0.major && minor == V1_0.minor) {
				version = V1_0;
			} else if (major == V1_1.major && minor == V1_1.minor) {
				version = V1_1;
			}
		}
		return version;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public String toString () {
		return major + "." + minor;
	}
}

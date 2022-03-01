package com.esotericsoftware.yamlbeans.issues.issue37;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

public class SexTypeSerializer implements ScalarSerializer<SexType> {

	public String write(SexType object) throws YamlException {
		return object.name().toLowerCase();
	}

	public SexType read(String value) throws YamlException {
		return SexType.valueOf(value.toUpperCase());
	}
}

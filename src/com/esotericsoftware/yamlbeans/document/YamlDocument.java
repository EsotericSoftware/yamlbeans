package com.esotericsoftware.yamlbeans.document;

import com.esotericsoftware.yamlbeans.YamlException;

public interface YamlDocument {
	
	String getTag();
	int size();
	YamlEntry getEntry(String key) throws YamlException;
	YamlEntry getEntry(int index) throws YamlException;
	boolean deleteEntry(String key) throws YamlException;
	void setEntry(String key, boolean value) throws YamlException;
	void setEntry(String key, Number value) throws YamlException;
	void setEntry(String key, String value) throws YamlException;
	void setEntry(String key, YamlElement value) throws YamlException;
	YamlElement getElement(int item) throws YamlException;
	void deleteElement(int element) throws YamlException;
	void setElement(int item, boolean value) throws YamlException;
	void setElement(int item, Number value) throws YamlException;
	void setElement(int item, String value) throws YamlException;
	void setElement(int item, YamlElement element) throws YamlException;
	void addElement(boolean value) throws YamlException;
	void addElement(Number value) throws YamlException;
	void addElement(String value) throws YamlException;
	void addElement(YamlElement element) throws YamlException;
	
}

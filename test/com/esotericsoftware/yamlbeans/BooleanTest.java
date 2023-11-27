package com.esotericsoftware.yamlbeans;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class BooleanTest extends TestCase {
    public void testBooleanBean() throws Exception {
        // Create a bean with a value differing from it's default
        BeanWithBoolean val = new BeanWithBoolean();
        val.setBool(true);
        val.setBoolObj(true);
        val.setBoolean(true);

        // Store the bean
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YamlWriter yamlWriter = new YamlWriter(new OutputStreamWriter(out));
        yamlWriter.write(val);
        yamlWriter.close();

        // Load the bean
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        YamlReader yamlReader = new YamlReader(new InputStreamReader(in));
        BeanWithBoolean got = yamlReader.read(BeanWithBoolean.class);

        assertEquals(val, got);
    }

    public static class BeanWithBoolean {
        private boolean bool = false;
        private Boolean boolObj = false;
        private boolean isBoolean = false;

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public Boolean getBoolObj() {
            return boolObj;
        }

        public void setBoolObj(Boolean boolObj) {
            this.boolObj = boolObj;
        }

        public boolean isBoolean() {
            return isBoolean;
        }

        public void setBoolean(boolean isBoolean) {
            this.isBoolean = isBoolean;
        }

		@Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BeanWithBoolean that = (BeanWithBoolean) o;

            if (bool != that.bool) return false;
            if (isBoolean != that.isBoolean) return false;
            return !(boolObj != null ? !boolObj.equals(that.boolObj) : that.boolObj != null);

        }

        @Override
        public int hashCode() {
            int result = (bool ? 1 : 0);
            result += (isBoolean ? 2 : 0);
            result = 31 * result + (boolObj != null ? boolObj.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "BeanWithBoolean{" +
                    "bool=" + bool +
                    ", boolObj=" + boolObj +
                    ", isBoolean=" + isBoolean +
                    '}';
        }
    }
}

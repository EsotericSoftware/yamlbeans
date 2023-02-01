package com.esotericsoftware.yamlbeans;


/**
 * SafeYamlConfig extends YamlConfig and hard codes the read anchor and read class tag flags to false.
 * When these flags are enabled, it is possible to perform a deserialization attack if the Yaml being parsed is from an
 * untrusted source.
 * Using SafeYamlConfig is the equivalent of using YamlConfig after setting
 *    yamlConfig.readConfig.setAnchors(false);
 *    yamlConfig.readConfig.setClassTags(false);
 *
 * It should be noted by setting these two values neither anchors or specifying class names are supported.
 * It is still possible to deserialize back to a specific object, but you need to specify the Class type in the code.
 * e.g
 *   SafeYamlConfig yamlConfig = new SafeYamlConfig();
 *   YamlReader reader = new YamlReader(yamlData.toString(),yamlConfig);
 *   Data data = reader.read(Data.class);
 *
 */
public class SafeYamlConfig extends YamlConfig {


    public SafeYamlConfig () {
        super();
        super.readConfig = new SafeReadConfig();
    }

    static public class SafeReadConfig extends ReadConfig {

        public SafeReadConfig(){
            super.anchors = false;
            super.classTags = false;
        }

        @Override
        public void setClassTags(boolean classTags) {
            if(classTags) {
                throw new IllegalArgumentException("Class Tags cannot be enabled in SafeYamlConfig.");
            }
        }

        @Override
        public void setAnchors(boolean anchors) {
            if(anchors) {
                throw new IllegalArgumentException("Anchors cannot be enabled in SafeYamlConfig.");
            }
        }
    }

}

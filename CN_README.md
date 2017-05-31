## YamlBeans

若需更多支持，请移步[YamlBeans讨论组](http://groups.google.com/group/yamlbeans-users)

## 概述

**YAML**是一种人性化的数据格式，使用YAML来替代XML和properties文件，可以获得更多的表现力（支持lists，maps，anchors等数据结构），以及更容易的手工编辑。
而**YamlBeans**则可以让Java对象和YAML格式之间的转换（序列化和反序列化）变得更容易。

Maven 仓库:
http://repo1.maven.org/maven2/com/esotericsoftware/yamlbeans/yamlbeans/

## 基本的反序列化

**YamlReader**这个类用于将YAML格式数据反序列化为Java对象。下面定义了一个包含4个实体的Map,其中最后一个实体项`phone numbers`又是一个包含了2个item的List集合，而每一个item又是一个Map结构。

```yaml
    name: Nathan Sweet
    age: 28
    address: 4011 16th Ave S
    phone numbers:
     - name: Home
       number: 206-555-5138
     - name: Work
       number: 425-555-2306
```

“**read()**”方法可用来读取`contact.yml`文件中描述的YAML对象，并将其反序列化为对应的HashMaps，ArrayLists和Strings。因为我们已经知道上面示例的YAML文件中定义的对象是一个Map，所以下面示例中我们可以直接转换为java对象并使用它。

```java
    YamlReader reader = new YamlReader(new FileReader("contact.yml"));
    Object object = reader.read();
    System.out.println(object);
    Map map = (Map)object;
    System.out.println(map.get("address"));
```

## 多个对象

一个YAML格式的文件中可以包含多个YAML对象,多个YAML对象之间用`---`隔开（开头第一个可以省略）

```yaml
    name: Nathan Sweet
    age: 28
    ---
    name: Some One
    age: 25
```
下面示例中，当while循环中每次调用YamlReader类中read（）方法时，就会将contact.yml文件中对应顺序的YAML对象反序列化成一个与之对应结构的Java对象。
下面代码会**先后输出**字符串`"28"`,`"25"`:

```java
    YamlReader reader = new YamlReader(new FileReader("contact.yml"));
    while (true) {
    	Map contact = reader.read();
    	if (contact == null) break;
    	System.out.println(contact.get("age"));
    }
```

## 反序列化其他类

有两种方法来反序列化除HashMaps，ArrayLists和Strings之外的其他自定义数据格式，例如下面这个YAML文件和Java类:

```yaml
    name: Nathan Sweet
    age: 28
```
```java
    public class Contact {
    	public String name;
    	public int age;
    }
```

在“**read()**”方法的入参中传递一个类，这样YamlReader就可以直接反序列化为指定的类:

```java
    YamlReader reader = new YamlReader(new FileReader("contact.yml"));
    Contact contact = reader.read(Contact.class);
    System.out.println(contact.age);
```


上面YamlReader创建了一个Contact.class的实例对象，并给“name”和“age”字段赋值，且YamlReader会把YAML中“age”的值转换为int。如果age不是int类型，则反序列化将失败。


除了上面这种反序列化方法外，还可以在YAML中使用添加`!全限定类名`方式来直接指定类型：

```yaml
    !com.example.Contact
    name: Nathan Sweet
    age: 28
```

## 序列化对象

 **YamlWriter**这个类用来把Java对象序列化为YAML格式。且“**write()**”方法会自动识别处理public字段和getter方法(一般private属性会生成getter方法)。

```java
    Contact contact = new Contact();
    contact.name = "Nathan Sweet";
    contact.age = 28;
    YamlWriter writer = new YamlWriter(new FileWriter("output.yml"));
    writer.write(contact);
    writer.close();
```

输出：

```yaml
    !com.example.Contact
    name: Nathan Sweet
    age: 28
```

上面`!com.example.Contact`部分会根据需要自动输出，以便YamlReader类能够反序列化时重建对应的Java对象。但序列化ArrayList时则不会输出任何类似`!com.example.Contact`的格式内容,因为YamlReader默认就会用ArrayList。

```java
    List list = new ArrayList();
    list.add("moo");
    list.add("cow");
```
```yaml
    - moo
    - cow
```

但是如果List的接口实现是LinkedList，而不是ArrayList（默认）,那么YamlWriter类就会输出，例如下面：

```java
    List list = new LinkedList();
    list.add("moo");
    list.add("cow");
```
```yaml
    !java.util.LinkedList
    - moo
    - cow
```

Note that it is not advisable to subclass Collection or Map. YamlBeans will only serialize the collection or map and its elements, not any additional fields.

## 复杂结构

**YamlBeans**可序列化任何对象。

```java
    public class Contact {
    	public String name;
    	public int age;
    	public List phoneNumbers;
    }
    
    public class Phone {
    	public String name;
    	public String number;
    }
```
```yaml
    friends:
      - !com.example.Contact
        name: Bob
        age: 29
        phoneNumbers:
            - !com.example.Phone
              name: Home
              number: 206-555-1234
            - !com.example.Phone
              name: Work
              number: 206-555-5678
      - !com.example.Contact
        name: Mike
        age: 31
        phoneNumbers:
            - !com.example.Phone
              number: 206-555-4321
    enemies:
      - !com.example.Contact
        name: Bill
        phoneNumbers:
            - !com.example.Phone
              name: Cell
              number: 206-555-1234
```

上面是一个由Contact类的List集合构成的复杂Map结构，而且Contact类中还包含phoneNumbers这个List属性。另外，public类型声明的字段也可以是java bean的属性（而不仅仅是getter方法对应的private类型字段）。

## 标签截取

`!com.example.Contact`这种形式的YAML标签有时可能会很长，会让YAML格式显得混乱不堪，不利于阅读。这时可以给类定义一个替代标签来代替，而不是用类的全限定类名。

```java
    YamlWriter writer = new YamlWriter(new FileWriter("output.yml"));
    writer.getConfig().setClassTag("contact", Contact.class);
    writer.write(contact);
    writer.close();
```

下面输出不再包含Contact类的完整类名。

```yaml
    !contact
    name: Nathan Sweet
    age: 28
```

## Lists and maps

When reading or writing a List or Map, YamlBeans cannot know what type of objects are supposed to be in the List or Map, so it will write out a tag.

```yaml
    !com.example.Contact
    name: Bill
        phoneNumbers:
            - !com.example.Phone
              number: 206-555-1234
            - !com.example.Phone
              number: 206-555-5678
            - !com.example.Phone
              number: 206-555-7654
```

This can make the YAML less readable. To improve this, you may define what element type should be expected for a List or Map field on your object.

```java
    YamlWriter writer = new YamlWriter(new FileWriter("output.yml"));
    writer.getConfig().setPropertyElementType(Contact.class, "phoneNumbers", Phone.class);
    writer.write(contact);
    writer.close();
```

Now YamlBeans knows what to expect for elements of the "phoneNumbers" field, so extra tags will not be output.

```yaml
    !com.example.Contact
    name: Bill
        phoneNumbers:
            - number: 206-555-1234
            - number: 206-555-5678
            - number: 206-555-7654
```

Setting the element type for a Map field tells YamlBeans what to expect for values in the Map. Keys in a Map are always Strings.

## Anchors

When an object graph contains multiple references to the same object, an anchor may be used so that the object only needs to be defined once in the YAML.

```yaml
    oldest friend:
        &1 !contact
        name: Bob
        age: 29
    best friend: *1
```

In this map, the "oldest friend" and "best friend" keys reference the same object. The YamlReader automatically handles the anchors in the YAML when rebuilding the object graph. By default, the YamlWriter automatically outputs anchors when writing objects.

```java
    Contact contact = new Contact();
    contact.name = "Bob";
    contact.age = 29;
    Map map = new HashMap();
    map.put("oldest friend", contact);
    map.put("best friend", contact);
```

## Duplicate key validation

By default, the behaviour of this YAML parser is to ignore duplicate keys if you have. e.g if you have the following

```yaml
    name: Nathan Sweet
    age: 28
    address:
      line1: 485 Madison Ave S
      line1: 711 3rd Ave S
      line2: NYC
```

The above YAML will give you an `address` object with attribute `line1` set to `711 3rd Ave S`. This is because the key `line1` in the above YAML is duplicated and thus the last value of `line1` will be retained. YAML parser will not complain about it. However, if your business logic requires you to validate YAML for such duplicates, then you can still do using `allowDuplicates` option of the `YamlConfig` object. Following is how its done:

```java
    try {
        YamlConfig yamlConfig = new YamlConfig();
        yamlConfig.setAllowDuplicates(false); // default value is true
        YamlReader reader = new YamlReader(new FileReader("contact.yml"), yamlConfig);
        Object object = reader.read();
        System.out.println(object);
        Map map = (Map)object;
        System.out.println(map.get("address"));
    } catch (YamlException ex) {
        ex.printStackTrace();
        // or handle duplicate key case here according to your business logic
    }
```

The above code will not print anything, but throw `YamlReaderException` at line 5 saying, `Duplicate key found 'line1'`.

## Architecture

The YAML tokenizer, parser, and emitter are based on those from the JvYAML project. They have been heavily refactored, bugs fixed, etc. The rest of the JvYAML project was not used because of its complexity. YamlBeans strives for the simplest possible thing that works, with the goal being to make it easy to use the YAML data format with Java.

YamlBeans supports YAML version 1.0 and 1.1.

## More info

See the javadocs for various other features available on the YamlConfig class.


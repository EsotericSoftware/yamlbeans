## YamlBeans

若需更多支持，请移步[YamlBeans讨论组](http://groups.google.com/group/yamlbeans-users)

## 概述

**YAML**是一种人性化的数据格式，使用YAML来替代XML和properties文件，可以获得更多的表现力（支持lists，maps，anchors等结构），以及更容易的手工编辑。
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

***注意***，在yaml中把集合或Map设置为子类节点是不可取的。YamlBeans只会序列化集合或Map及其中的元素，而不会对其他字段进行序列化。

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

`!com.example.Contact`这种形式的YAML标签有时可能会很长，会让YAML格式显得混乱不堪，不利于阅读。这时可以给类指定一个替代标签来代替，而不是用类的完整类名。

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

## List 和 Map

在读取或写入一个List或Map时，YamlBeans有时压根不知道List或Map中应该是什么类型的对象，因此它会输出一个类似`!com.example.Contact`的标签。

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

这样会导致YAML的可读性变的更差。为了提高可读性，你可以在List或Map对象的字段中，指定该字段所属的类型，像下面这样：

```java
    YamlWriter writer = new YamlWriter(new FileWriter("output.yml"));
    writer.getConfig().setPropertyElementType(Contact.class, "phoneNumbers", Phone.class);
    writer.write(contact);
    writer.close();
```


现在，YamlBeans知道“phoneNumbers”字段的类型是什么了，因此不会额外输出多余的标签。

```yaml
    !com.example.Contact
    name: Bill
        phoneNumbers:
            - number: 206-555-1234
            - number: 206-555-5678
            - number: 206-555-7654
```

Map中value值的类型，可以根据期望的情况来指定，但Map中的键总是字符串类型。

## 锚点

当一个对象的结构中包含对其他同一对象的多个引用时，可以设置一个锚点，这样这个被引用的对象只需要在YAML中定义一次。

```yaml
    oldest friend:
        &1 !contact
        name: Bob
        age: 29
    best friend: *1
```

在上面map中，"oldest friend" 和 "best friend" 字段引用了同一个对象。在反序列化构建对象时，YamlReader会自动处理YAML中的锚点。同时，在默认情况下，YamlWriter在序列化对象时也会自动输出锚点。

```java
    Contact contact = new Contact();
    contact.name = "Bob";
    contact.age = 29;
    Map map = new HashMap();
    map.put("oldest friend", contact);
    map.put("best friend", contact);
```

## 让重复字段生效

默认情况下，YAML在解析时是忽略重复字段的。例如，以下情况：

```yaml
    name: Nathan Sweet
    age: 28
    address:
      line1: 485 Madison Ave S
      line1: 711 3rd Ave S
      line2: NYC
```


上面的YAML将会为`address`的`line1`字段赋值为`711 3rd Ave S`而不是`485 Madison Ave S`。这是因为上面YAML中的字段line1是重复的，因此line1的最后一个值将会被保留。但是，如果你的业务逻辑要求重复字段都生效，那么你可以在YamlConfig类进行设置。以下是设置方法:

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

上面的代码不会打印任何东西，但会在第5行抛出`YamlReaderException`异常说`Duplicate key found 'line1'`

## 体系结构

YAML的tokenizer，parser，emitter组件是基于JvYAML项目中的。这些功能已被重构，修复bug等。由于JvYAML的复杂性，剩下部分未被使用。 YamlBeans努力于实现简单可行的事情---让使用Java操作YAML数据格式变得更加容易。

YamlBeans 支持 YAML 1.0 和 1.1 版本 。

## 更多信息

有关YamlConfig类的其他更多功能，请参阅javadocs


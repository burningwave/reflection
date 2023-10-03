# Burningwave Reflection [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=%40burningwave_org%20Reflection%2C%20the%20%23advanced%20reflection%20engine%20%28works%20on%20%23Java8%20%23Java9%20%23Java10%20%23Java11%20%23Java12%20%23Java13%20%23Java14%20%23Java15%20%23Java16%20%23Java17%20%23Java18%20%23Java19%20%23Java20%20%23Java21%29&url=https://burningwave.github.io/reflection/)

<a href="https://www.burningwave.org">
<img src="https://raw.githubusercontent.com/burningwave/burningwave.github.io/main/logo.png" alt="logo.png" height="180px" align="right"/>
</a>

[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.burningwave/reflection/3)](https://maven-badges.herokuapp.com/maven-central/org.burningwave/reflection/)
[![GitHub](https://img.shields.io/github/license/burningwave/reflection)](https://github.com/burningwave/reflection/blob/master/LICENSE)

[![Platforms](https://img.shields.io/badge/platforms-Windows%2C%20Mac%20OS%2C%20Linux-orange)](https://github.com/burningwave/reflection/actions/runs/6394711772)

[![Supported JVM](https://img.shields.io/badge/supported%20JVM-7%2C%208%2C%209+-blueviolet)](https://github.com/burningwave/reflection/actions/runs/6394711772)

[![Coveralls github branch](https://img.shields.io/coveralls/github/burningwave/reflection/main)](https://coveralls.io/github/burningwave/reflection?branch=main)
[![GitHub open issues](https://img.shields.io/github/issues/burningwave/reflection)](https://github.com/burningwave/reflection/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/burningwave/reflection)](https://github.com/burningwave/reflection/issues?q=is%3Aissue+is%3Aclosed)

[![Artifact downloads](https://www.burningwave.org/generators/generate-burningwave-artifact-downloads-badge.php?artifactId=reflection)](https://www.burningwave.org/artifact-downloads/?show-overall-trend-chart=false&artifactId=reflection&startDate=2022-11)
[![Repository dependents](https://badgen.net/github/dependents-repo/burningwave/reflection)](https://github.com/burningwave/reflection/network/dependents)
[![HitCount](https://www.burningwave.org/generators/generate-visited-pages-badge.php)](https://www.burningwave.org#bw-counters)

Extracted from [**Burningwave Core**](https://burningwave.github.io/core/), Burningwave Reflection is an advanced, free and open source reflection engine.

The main difference with Burningwave Core's reflection engine is that **the use of the [JVM Driver](https://burningwave.github.io/jvm-driver/) is optional and if the containing library is not present in the class path it is not used by default**. 

And now we will see:
* [including Burningwave Reflection in your project](#Including-Burningwave-Reflection-in-your-project)
* [handling privates and all other members of an object](#Handling-privates-and-all-other-members-of-an-object)
* [getting and setting properties of a Java bean through path](#Getting-and-setting-properties-of-a-Java-bean-through-path)
* [configuration](#Configuration)
* [**how to ask for assistance**](#Ask-for-assistance)

<br/>

## <a name="Including-Burningwave-Core-in-your-project"></a>Including Burningwave Reflection in your project 
If you are on Java 8 and later, to include Burningwave Reflection library in your projects simply use with **Apache Maven**:

```xml
<dependency>
    <groupId>org.burningwave</groupId>
    <artifactId>reflection</artifactId>
    <version>3.6.1</version>
</dependency>
```

If you are on **Java 7 and later**, use this instead:
```xml
<dependency>
    <groupId>org.burningwave</groupId>
    <artifactId>reflection</artifactId>
    <version>3.6.1-fjs</version>
</dependency>
```

### Requiring the Burningwave Reflection module

To use Burningwave Reflection as a Java module you need to add the following to your `module-info.java`: 

```java
requires org.burningwave.reflection;
```
### Enabling the JVM Driver
By default the JVM Driver is not imported by the Reflection library, so if you want to use it you just need to add the following to your pom:
```xml
<dependency>
    <groupId>org.burningwave</groupId>
    <artifactId>jvm-driver</artifactId>
    <version>8.14.2</version>
</dependency>	
```
... And the following to your `module-info.java`: 

```java
requires jdk.unsupported;
requires io.github.toolfactory.jvm;
requires org.burningwave.jvm;
```
If you want to add the JVM Driver to your project it is also possible to [disable it programmatically](#Configuration).

<br/>

## <a name="Handling-privates-and-all-other-members-of-an-object"></a>Handling privates and all other members of an object
Through **Fields**, **Constructors** and **Methods** components it is possible to get or set fields value, invoking or finding constructors or methods of an object.
**Members handlers use to cache all members for faster access**.
For fields handling we are going to use **Fields** component:
```java
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.burningwave.reflection.FieldCriteria;
import org.burningwave.reflection.Fields;

public class FieldsHandler {

    public static void execute() {
        Object obj = new Object() {
            volatile List<Object> objectValue;
            volatile int intValue = 1;
            volatile long longValue = 2l;
            volatile float floatValue = 3f;
            volatile double doubleValue = 4.1d;
            volatile boolean booleanValue = true;
            volatile byte byteValue = (byte)5;
            volatile char charValue = 'c';
        };

        //Get all filtered field values of an object
        Collection<?> values = Fields.INSTANCE.getAll(
            FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(field -> {
                return field.getType().isPrimitive();
            }),
            obj
        ).values();

        Fields.INSTANCE.set(obj, "intValue", 15);
        System.out.println(Fields.INSTANCE.get(obj, "intValue").toString());

    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Collection<Class<?>> loadedClasses = Fields.INSTANCE.get(classLoader, "classes");
        Map<Field, ?> valueForField = Fields.INSTANCE.getAll(classLoader);
    }

    public static void main(String[] args) {
        execute();
    }

}
```
For methods handling we are going to use **Methods** component:
```java
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collection;

import org.burningwave.reflection.Classes;
import org.burningwave.reflection.MethodCriteria;
import org.burningwave.reflection.Methods;


public class MethodsHandler {
    
    public static void execute() {
        Methods.INSTANCE.invoke(System.out, "println", "Hello World");
        Integer number = Methods.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);
        
        //Invoking a method: the invoke method tries to execute the target method via MethodHandle and
        //if that fails it tries with standard reflection
        Methods.INSTANCE.invoke(System.out, "println", number);
        
        //Filtering and obtaining a MethodHandle reference
        MethodHandle methodHandle = Methods.INSTANCE.findFirstDirectHandle(
            MethodCriteria.byScanUpTo((cls) ->
                //We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
                cls.getName().equals(ClassLoader.class.getName())
            ).name(
                "defineClass"::equals
            ).and().parameterTypes(params -> 
                params.length == 3
            ).and().parameterTypesAreAssignableFrom(
                String.class, ByteBuffer.class, ProtectionDomain.class
            ).and().returnType((cls) -> 
                cls.getName().equals(Class.class.getName())
            ), ClassLoader.class
        );        
        
        //Filtering and obtaining all methods of ClassLoader class that have at least
        //one input parameter of Class type
        Collection<Method> methods = Methods.INSTANCE.findAll(
            MethodCriteria.byScanUpTo((cls) ->
                //We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
                cls.getName().equals(ClassLoader.class.getName())
            ).parameter((params, idx) -> {
                return Classes.INSTANCE.isAssignableFrom(params[idx].getType(), Class.class);
            }), ClassLoader.class
        );
    }
    
    public static void main(String[] args) {
        execute();
    }
    
}
```

For constructors handling we are going to use **Constructors** component:
```java
import org.burningwave.reflection.Constructors;

public class ConstructorsHandler {

    public static void execute() {
        //Invoking constructor
    	ForTest object = Constructors.INSTANCE.newInstanceOf(ForTest.class, 10);
    }

    public static class ForTest {

    	int value;

    	public ForTest(int value) {
    		this.value = value;
    		System.out.println("Constructor invoked, value: " + value);
    	}

    }
    
    public static void main(String[] args) {
        execute();
    }

}
```

<br/>

## <a name="Getting-and-setting-properties-of-a-Java-bean-through-path"></a>Getting and setting Field of an object through path
Through **FieldAccessor** it is possible to get and set field of an object bean by using path. So for this example we will use these Java beans:

```java
package org.burningwave.reflection.examples.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Complex {
    private Complex.Data data;
    
    public Complex() {
        setData(new Data());
    }
    
    
    public Complex.Data getData() {
        return data;
    }
    
    public void setData(Complex.Data data) {
        this.data = data;
    }


    public static class Data {
        private Data.Item[][] items;
        private List<Data.Item> itemsList;
        private Map<String, Data.Item[][]> itemsMap;
        
        public Data() {
            items = new Data.Item[][] {
                new Data.Item[] {
                    new Item("Hello"),
                    new Item("World!"),
                    new Item("How do you do?")
                },
                new Data.Item[] {
                    new Item("How do you do?"),
                    new Item("Hello"),
                    new Item("Bye")
                }
            };
            itemsMap = new LinkedHashMap<>();
            itemsMap.put("items", items);
        }
        
        public Data.Item[][] getItems() {
            return items;
        }
        public void setItems(Data.Item[][] items) {
            this.items = items;
        }
        
        public List<Data.Item> getItemsList() {
            return itemsList;
        }
        public void setItemsList(List<Data.Item> itemsList) {
            this.itemsList = itemsList;
        }
        
        public Map<String, Data.Item[][]> getItemsMap() {
            return itemsMap;
        }
        public void setItemsMap(Map<String, Data.Item[][]> itemsMap) {
            this.itemsMap = itemsMap;
        }
        
        public static class Item {
            private String name;
            
            public Item(String name) {
                this.name = name;
            }
            
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
```
... And now we are going to get and set some fields:
```java
import org.burningwave.reflection.FieldAccessor;

import org.burningwave.reflection.examples.bean.Complex;

public class GetAndSetPropertiesThroughPath{
    
    public void execute() {
        Complex complex = new Complex();
        //The field accessor try to access by field introspection: if no field was found
        //it will search getter method and invokes it
        String nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.items[1][0].name");
        String nameFromObjectMap = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
        System.out.println(nameFromObjectMap);
        nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.items[1][2].name");
        nameFromObjectMap = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
        System.out.println(nameFromObjectMap);
        FieldAccessor.INSTANCE.set(complex, "data.itemsMap[items][1][1].name", "Good evening!");
        nameFromObjectInArray = FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name");
        System.out.println(nameFromObjectInArray);
    }
    
    public static void main(String[] args) {
        new GetAndSetPropertiesThroughPath().execute();
    }
    
}
```

<br/>

## <a name="Configuration"></a>Configuration

With Burningwave Reflection it is possible disable the use of the JVM Driver programmatically with the following line of code before you starting to use the reflection components:
```java
org.burningwave.reflection.Facade.Configuration.disableDriver();
```


### [**Official site**](https://www.burningwave.org/)

<br />

# <a name="Ask-for-assistance"></a>Ask for assistance
If this guide can't help you, you can:
* [open a discussion](https://github.com/burningwave/reflection/discussions) here on GitHub
* [report a bug](https://github.com/burningwave/reflection/issues) here on GitHub
* ask on [Stack Overflow](https://stackoverflow.com/search?q=burningwave)

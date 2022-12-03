package org.burningwave.reflection.examples;

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
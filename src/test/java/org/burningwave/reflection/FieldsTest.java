package org.burningwave.reflection;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.burningwave.reflection.Facade;
import org.burningwave.reflection.FieldCriteria;
import org.burningwave.reflection.Fields;
import org.junit.jupiter.api.Test;


@SuppressWarnings("unused")
public class FieldsTest extends BaseTest {

	@Test
	public void getAllTestOne() {
		testNotEmpty(
			() -> {
				return Fields.INSTANCE.getAll(Facade.INSTANCE).values();
			},
		true);
	}

	@Test
	public void getAllDirectTestOne() {
		testNotEmpty(
			() -> {
				return Fields.INSTANCE.getAllDirect(Facade.INSTANCE).values();
			},
		true);
	}

	@Test
	public void setDirectTestOne() {
		testDoesNotThrow(() -> {
			Object obj = new Object() {
				List<Object> objectValue;
				int intValue;
				long longValue;
				float floatValue;
				double doubleValue;
				boolean booleanValue;
				byte byteValue;
				char charValue;
			};
			List<Object> objectValue = new ArrayList<>();
			Fields.INSTANCE.set(obj, "objectValue", objectValue);
			List<Object> objectValue2Var = Fields.INSTANCE.get(obj, "objectValue");
			assertTrue(objectValue2Var == objectValue);
			Fields.INSTANCE.set(obj, "intValue", 1);
			int intValue = Fields.INSTANCE.get(obj, "intValue");
			assertTrue(intValue == 1);
			Fields.INSTANCE.set(obj, "longValue", 2l);
			long longValue = Fields.INSTANCE.get(obj, "longValue");
			assertTrue(longValue == 2l);
			Fields.INSTANCE.set(obj, "floatValue", 3f);
			float floatValue = Fields.INSTANCE.get(obj, "floatValue");
			assertTrue(floatValue == 3f);
			Fields.INSTANCE.set(obj, "doubleValue", 4.0d);
			double doubleValue = Fields.INSTANCE.get(obj, "doubleValue");
			assertTrue(doubleValue == 4.0);
			Fields.INSTANCE.set(obj, "booleanValue", true);
			boolean booleanValue = Fields.INSTANCE.get(obj, "booleanValue");
			assertTrue(booleanValue);
			Fields.INSTANCE.set(obj, "byteValue", (byte)5);
			byte byteValue = Fields.INSTANCE.get(obj, "byteValue");
			assertTrue(byteValue == 5);
			Fields.INSTANCE.set(obj, "charValue", 'a');
			char charValue = Fields.INSTANCE.get(obj, "charValue");
			assertTrue(charValue == 'a');
		});
	}

	@Test
	public void setDirectVolatileTestOne() {
		testDoesNotThrow(() -> {
			Object obj = new Object() {
				volatile List<Object> objectValue;
				volatile int intValue;
				volatile long longValue;
				volatile float floatValue;
				volatile double doubleValue;
				volatile boolean booleanValue;
				volatile byte byteValue;
				volatile char charValue;
			};
			List<Object> objectValue = new ArrayList<>();
			Fields.INSTANCE.set(obj, "objectValue", objectValue);
			List<Object> objectValue2Var = Fields.INSTANCE.get(obj, "objectValue");
			assertTrue(objectValue2Var == objectValue);
			Fields.INSTANCE.set(obj, "intValue", 1);
			int intValue = Fields.INSTANCE.get(obj, "intValue");
			assertTrue(intValue == 1);
			Fields.INSTANCE.set(obj, "longValue", 2l);
			long longValue = Fields.INSTANCE.get(obj, "longValue");
			assertTrue(longValue == 2l);
			Fields.INSTANCE.set(obj, "floatValue", 3f);
			float floatValue = Fields.INSTANCE.get(obj, "floatValue");
			assertTrue(floatValue == 3f);
			Fields.INSTANCE.set(obj, "doubleValue", 4.0d);
			double doubleValue = Fields.INSTANCE.get(obj, "doubleValue");
			assertTrue(doubleValue == 4.0);
			Fields.INSTANCE.set(obj, "booleanValue", true);
			boolean booleanValue = Fields.INSTANCE.get(obj, "booleanValue");
			assertTrue(booleanValue);
			Fields.INSTANCE.set(obj, "byteValue", (byte)5);
			byte byteValue = Fields.INSTANCE.get(obj, "byteValue");
			assertTrue(byteValue == 5);
			Fields.INSTANCE.set(obj, "charValue", 'a');
			char charValue = Fields.INSTANCE.get(obj, "charValue");
			assertTrue(charValue == 'a');
		});
	}


	@Test
	public void getAllTestTwo() {
		testNotEmpty(() -> {
			Object obj = new Object() {
				volatile List<Object> objectValue;
				volatile int intValue;
				volatile long longValue;
				volatile float floatValue;
				volatile double doubleValue;
				volatile boolean booleanValue;
				volatile byte byteValue;
				volatile char charValue;
			};
			return Fields.INSTANCE.getAll(FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(field -> {
				return field.getType().isPrimitive();
			}), obj).values();
		}, true);
	}
}

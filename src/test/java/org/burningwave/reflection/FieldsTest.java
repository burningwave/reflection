package org.burningwave.reflection;


import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.burningwave.function.ThrowingPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;


@SuppressWarnings("unused")
public class FieldsTest extends BaseTest {

	@Test
	public void getAllTestOne() {
		testNotEmpty(
			new ThrowingSupplier<Collection<?>>() {
				@Override
				public Collection<?> get() throws Throwable {
					return Fields.INSTANCE.getAll(Facade.INSTANCE).values();
				}
			},
		true);
	}

	@Test
	public void getAllDirectTestOne() {
		testNotEmpty(
			new ThrowingSupplier<Collection<?>>() {
				@Override
				public Collection<?> get() throws Throwable {
					return Fields.INSTANCE.getAllDirect(Facade.INSTANCE).values();
				}
			},
		true);
	}

	@Test
	public void setDirectTestOne() {
		testDoesNotThrow(new Executable() {
			@Override
			public void execute() throws Throwable {
				final Object obj = new Object() {
					List<Object> objectValue;
					int intValue;
					long longValue;
					float floatValue;
					double doubleValue;
					boolean booleanValue;
					byte byteValue;
					char charValue;
				};
				final List<Object> objectValue = new ArrayList<>();
				Fields.INSTANCE.set(obj, "objectValue", objectValue);
				final List<Object> objectValue2Var = Fields.INSTANCE.get(obj, "objectValue");
				assertTrue(objectValue2Var == objectValue);
				Fields.INSTANCE.set(obj, "intValue", 1);
				final int intValue = Fields.INSTANCE.get(obj, "intValue");
				assertTrue(intValue == 1);
				Fields.INSTANCE.set(obj, "longValue", 2l);
				final long longValue = Fields.INSTANCE.get(obj, "longValue");
				assertTrue(longValue == 2l);
				Fields.INSTANCE.set(obj, "floatValue", 3f);
				final float floatValue = Fields.INSTANCE.get(obj, "floatValue");
				assertTrue(floatValue == 3f);
				Fields.INSTANCE.set(obj, "doubleValue", 4.0d);
				final double doubleValue = Fields.INSTANCE.get(obj, "doubleValue");
				assertTrue(doubleValue == 4.0);
				Fields.INSTANCE.set(obj, "booleanValue", true);
				final boolean booleanValue = Fields.INSTANCE.get(obj, "booleanValue");
				assertTrue(booleanValue);
				Fields.INSTANCE.set(obj, "byteValue", (byte)5);
				final byte byteValue = Fields.INSTANCE.get(obj, "byteValue");
				assertTrue(byteValue == 5);
				Fields.INSTANCE.set(obj, "charValue", 'a');
				final char charValue = Fields.INSTANCE.get(obj, "charValue");
				assertTrue(charValue == 'a');
			}
		});
	}

	@Test
	public void setDirectVolatileTestOne() {
		testDoesNotThrow(new Executable() {
			@Override
			public void execute() throws Throwable {
				final Object obj = new Object() {
					volatile List<Object> objectValue;
					volatile int intValue;
					volatile long longValue;
					volatile float floatValue;
					volatile double doubleValue;
					volatile boolean booleanValue;
					volatile byte byteValue;
					volatile char charValue;
				};
				final List<Object> objectValue = new ArrayList<>();
				Fields.INSTANCE.set(obj, "objectValue", objectValue);
				final List<Object> objectValue2Var = Fields.INSTANCE.get(obj, "objectValue");
				assertTrue(objectValue2Var == objectValue);
				Fields.INSTANCE.set(obj, "intValue", 1);
				final int intValue = Fields.INSTANCE.get(obj, "intValue");
				assertTrue(intValue == 1);
				Fields.INSTANCE.set(obj, "longValue", 2l);
				final long longValue = Fields.INSTANCE.get(obj, "longValue");
				assertTrue(longValue == 2l);
				Fields.INSTANCE.set(obj, "floatValue", 3f);
				final float floatValue = Fields.INSTANCE.get(obj, "floatValue");
				assertTrue(floatValue == 3f);
				Fields.INSTANCE.set(obj, "doubleValue", 4.0d);
				final double doubleValue = Fields.INSTANCE.get(obj, "doubleValue");
				assertTrue(doubleValue == 4.0);
				Fields.INSTANCE.set(obj, "booleanValue", true);
				final boolean booleanValue = Fields.INSTANCE.get(obj, "booleanValue");
				assertTrue(booleanValue);
				Fields.INSTANCE.set(obj, "byteValue", (byte)5);
				final byte byteValue = Fields.INSTANCE.get(obj, "byteValue");
				assertTrue(byteValue == 5);
				Fields.INSTANCE.set(obj, "charValue", 'a');
				final char charValue = Fields.INSTANCE.get(obj, "charValue");
				assertTrue(charValue == 'a');
			}
		});
	}


	@Test
	public void getAllTestTwo() {
		testNotEmpty(new ThrowingSupplier<Collection<?>>() {
			@Override
			public Collection<?> get() throws Throwable {
				final Object obj = new Object() {
					volatile List<Object> objectValue;
					volatile int intValue;
					volatile long longValue;
					volatile float floatValue;
					volatile double doubleValue;
					volatile boolean booleanValue;
					volatile byte byteValue;
					volatile char charValue;
				};
				return Fields.INSTANCE.getAll(FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(new ThrowingPredicate<Field, Throwable>() {
					@Override
					public boolean test(Field field) throws Throwable {
						return field.getType().isPrimitive();
					}
				}), obj).values();
			}
		}, true);
	}
}

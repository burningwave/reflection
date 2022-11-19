package org.burningwave.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.burningwave.reflection.bean.Complex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class FieldAccessorTest extends BaseTest {

	@Test
	public void getTestOne() {
		final Complex complex = new Complex();
		assertNotNull(FieldAccessor.INSTANCE.get(complex, "data.items[1][1].name"));
		assertNotNull(FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items][1][1].name"));
	}

	@Test
	public void setTestOne() {
		final Complex complex = new Complex();
		final String newName = "Peter";
		FieldAccessor.INSTANCE.set(complex, "data.items[0][2].name", newName);
		assertEquals(FieldAccessor.INSTANCE.get(complex, "data.items[0][2].name"), newName);
	}

	@Test
	void setTestTwo() {
		final Complex complex = new Complex();
		final Complex.Data.Item newItem = new Complex.Data.Item("Sam");
		FieldAccessor.INSTANCE.set(complex, "data.items[0][1]", newItem);
		assertEquals(FieldAccessor.INSTANCE.get(complex, "data.items[0][1]"), newItem);
	}

	@Test
	void setTestThree() {
		final Complex complex = new Complex();
		FieldAccessor.INSTANCE.set(complex, "data.itemsMap[items]",
				FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items]"));
		assertEquals((Object) FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items]"),
				(Object) FieldAccessor.INSTANCE.get(complex, "data.itemsMap[items]"));
	}

	@Test
	void setTestFour() {
		final Complex complex = new Complex();
		FieldAccessor.INSTANCE.set(complex, "data", FieldAccessor.INSTANCE.get(complex, "data"));
		assertEquals((Object) FieldAccessor.INSTANCE.get(complex, "data"),
				(Object) FieldAccessor.INSTANCE.get(complex, "data"));
	}

	@Test
	void setTestFive() {
		final Map<String, Map<String, Map<String, Object>>> nestedMaps= new LinkedHashMap<>();
		final Map<String, Map<String, Object>> innerMapLevelOne = new LinkedHashMap<>();
		final Map<String, Object> innerMapLevelTwo = new LinkedHashMap<>();

		FieldAccessor.INSTANCE.set(nestedMaps, "[data]", innerMapLevelOne);
		FieldAccessor.INSTANCE.set(innerMapLevelOne, "[data]", innerMapLevelTwo);
		FieldAccessor.INSTANCE.set(nestedMaps, "[data][data][data]", "Hello");
		assertEquals((Object) FieldAccessor.INSTANCE.get(nestedMaps, "[data][data][data]"),
				"Hello");
	}

	@Test
	void setTestSix() {
		setIndexedValue(new Supplier<Collection>() {
			@Override
			public Collection get() {
				return new ArrayList();
			}
		});
	}

	@Test
	void setTestSeven() {
		final AtomicInteger hashCodeGenerator = new AtomicInteger(1);
		setIndexedValue(new Supplier<Collection>() {
			@Override
			public Collection get() {
				return new HashSet() {
					private int hashCode = hashCodeGenerator.getAndIncrement();
					@Override
					public int hashCode() {
						return hashCode;
					}
					@Override
					public String toString() {
						return " hash code: " + String.valueOf(hashCode) + " - " + super.toString();
					}
				};
			}
		});
	}

	public<T> void setIndexedValue(final Supplier<Collection> collSupplier) {
		testDoesNotThrow(new Executable() {
			@Override
			public void execute() throws Throwable {
				final Collection<Collection<Collection<String>>> nestedCollections = collSupplier.get();
				nestedCollections.add(collSupplier.get());
				nestedCollections.add(collSupplier.get());
				nestedCollections.add(collSupplier.get());
				final Collection<Collection<String>> nestedCollectionsLevelOne = collSupplier.get();
				nestedCollectionsLevelOne.add(collSupplier.get());
				nestedCollectionsLevelOne.add(collSupplier.get());
				nestedCollectionsLevelOne.add(collSupplier.get());
				final Collection<String> nestedCollectionsLevelTwo = collSupplier.get();
				nestedCollectionsLevelTwo.add("a");
				nestedCollectionsLevelTwo.add("b");
				nestedCollectionsLevelTwo.add("c");
				FieldAccessor.INSTANCE.set(nestedCollections, "[2]", nestedCollectionsLevelOne);
				FieldAccessor.INSTANCE.set(nestedCollectionsLevelOne, "[2]", nestedCollectionsLevelTwo);
				FieldAccessor.INSTANCE.set(nestedCollections, "[2][2][2]", "Hello");
				assertEquals((Object) FieldAccessor.INSTANCE.get(nestedCollections, "[2][2][2]"), "Hello");
			}
		});
	}

}

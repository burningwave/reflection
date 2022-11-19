package org.burningwave.reflection;


import org.burningwave.reflection.service.ExtendedService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

@SuppressWarnings("all")
public class ConstructorsTest extends BaseTest {

	@Test
	public void newInstanceDirectOfTestOne() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Constructors.INSTANCE.newInstanceOf(ExtendedService.class);
			}
		});
	}

	@Test
	public void newInstanceDirectOfTestTwo() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Constructors.INSTANCE.newInstanceOf(
						ExtendedService.class,
						"Extended service name"
					);
			}
		}
		);
	}

	@Test
	public void newInstanceOfTestOne() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Constructors.INSTANCE.newInstanceOf(ExtendedService.class);
			}
		});
	}

	@Test
	public void newInstanceOfTestTwo() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Constructors.INSTANCE.newInstanceOf(
						ExtendedService.class,
						"Extended service name"
					);
			}
		}
		);
	}

	@Test
	public void convertToMethodHandleTestOne() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Constructors.INSTANCE.findDirectHandle(
					Constructors.INSTANCE.findOneAndMakeItAccessible(ExtendedService.class)
				).invokeWithArguments();
			}
		}
		);
	}

}

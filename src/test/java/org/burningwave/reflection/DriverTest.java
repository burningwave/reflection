package org.burningwave.reflection;

import org.burningwave.function.ThrowingSupplier;
import org.junit.Test;

public class DriverTest extends BaseTest {

	@Test
	public void getDriverTest() {
		testNotNull(new ThrowingSupplier<Object, Throwable>() {
			@Override
			public Object get() {
				return Facade.INSTANCE.getDriver();
			}
		});
	}

	@Test
	public void disableDriverTest() {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Facade.INSTANCE.disableDriver();
			}
		});
	}

}

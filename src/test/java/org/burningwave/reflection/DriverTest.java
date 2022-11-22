package org.burningwave.reflection;

import org.junit.jupiter.api.Test;

public class DriverTest extends BaseTest {

	@Test
	public void getDriverTest() {
		testNotNull(() -> {
			return Facade.INSTANCE.getDriver();
		});
	}

	@Test
	public void disableDriverTest() {
		testNotNull(() -> {
			return Facade.INSTANCE.disableDriver();
		});
	}

}

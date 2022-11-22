package org.burningwave.reflection;

import org.junit.jupiter.api.Test;

public class CacheTest extends BaseTest {

	@Test
	public void clearCacheTest() {
		testDoesNotThrow(() -> {
			Facade.INSTANCE.clearCache();
		});
	}

}

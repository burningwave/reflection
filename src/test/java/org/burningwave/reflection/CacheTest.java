package org.burningwave.reflection;

import org.junit.Test;

public class CacheTest extends BaseTest {

	@Test
	public void clearCacheTest() {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Facade.INSTANCE.clearCache();
			}
		});
	}

}

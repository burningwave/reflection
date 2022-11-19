package org.burningwave.reflection.service;

import org.burningwave.function.Supplier;
import org.burningwave.reflection.BaseTest;

public class ExtendedService extends Service {

	public ExtendedService() {
		super();
	}

	public ExtendedService(final String name) {
		super(name);
	}

	@Override
	public String apply(final Object value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return ExtendedService.this.getClass().getName();
			}
		}, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

}

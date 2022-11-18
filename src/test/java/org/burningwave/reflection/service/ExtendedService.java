package org.burningwave.reflection.service;

import org.burningwave.reflection.BaseTest;

public class ExtendedService extends Service {

	public ExtendedService() {
		super();
	}

	public ExtendedService(String name) {
		super(name);
	}

	@Override
	public String apply(Object value_01, String value_02, String value_03) {
		BaseTest.logInfo(this.getClass()::getName, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

}

package org.burningwave.reflection.service;

import org.burningwave.function.Supplier;
import org.burningwave.reflection.BaseTest;

public abstract class ServiceInterface {

	public void printMyName() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return this.getClass().getName();
			}
		}, "My name is" + this.getClass().getName());
	}

}

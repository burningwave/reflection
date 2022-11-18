package org.burningwave.reflection.service;

import org.burningwave.reflection.BaseTest;

public interface ServiceInterface {

	public default void printMyName() {
		BaseTest.logInfo(this.getClass()::getName, "My name is" + this.getClass().getName());
	}

}

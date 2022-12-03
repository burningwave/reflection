package org.burningwave.reflection.examples;

import org.burningwave.reflection.Constructors;

public class ConstructorsHandler {

    public static void execute() {
        //Invoking constructor
    	ForTest object = Constructors.INSTANCE.newInstanceOf(ForTest.class, 10);
    }

    public static class ForTest {

    	int value;

    	public ForTest(int value) {
    		this.value = value;
    		System.out.println("Constructor invoked, value: " + value);
    	}

    }

    public static void main(String[] args) {
        execute();
    }

}

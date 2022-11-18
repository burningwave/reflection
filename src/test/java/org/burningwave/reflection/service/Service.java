package org.burningwave.reflection.service;

import java.util.Arrays;

import org.burningwave.reflection.BaseTest;


public class Service implements ServiceInterface {


	private String name;

	public Service() {
		this("Default name");
	}

	public Service(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void printName() {
		BaseTest.logInfo(this.getClass()::getName, "My name is {}", this.name);
	}

	public static Object retrieve() {
		BaseTest.logInfo(Service.class::getName, "static retrieve");
		return new Object();
	}

	@SuppressWarnings("unused")
	private Service supply() {
		BaseTest.logInfo(this.getClass()::getName, "supply");
		return new Service();
	}

	public static void consume(Integer obj) {
		BaseTest.logInfo(Service.class::getName, "Consumer Integer: " + obj.toString());
	}

	public void consume(String obj) {
		BaseTest.logInfo(this.getClass()::getName, "Consumer String: " + obj);
	}

	public String apply(String obj) {
		BaseTest.logInfo(this.getClass()::getName, "Function String execute String: " + obj);
		return obj;
	}

	public Long apply(Long obj) {
		BaseTest.logInfo(this.getClass()::getName, "Function Long execute String: " + obj);
		return obj;
	}

	public void run() {
		BaseTest.logInfo(this.getClass()::getName, "static run");
	}

	public void methodWithVarArgs(String... arg) {
		BaseTest.logInfo(this.getClass()::getName, "methodWithVarArgs");
	}

	public static void staticRun() {
		BaseTest.logInfo(Service.class::getName, "static run");
	}

//	public String apply(String value_01, String value_02) {
//		BaseTest.logInfo(this.getClass()::getName, "BiFunction: " + value_01 + " " + value_02);
//		return "";
//	}

	public String apply(String value_01, String value_02, String... value_03) {
		BaseTest.logInfo(this.getClass()::getName, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public String apply(Object value_01, String value_02, String value_03) {
		BaseTest.logInfo(this.getClass()::getName, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public static String staticApply(Object value_01, String value_02, String value_03) {
		BaseTest.logInfo(Service.class::getName, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public static String staticApply(Object value_01, String value_02, String value_03, String... value_04) {
		BaseTest.logInfo(Service.class::getName, "TriFunction: " + value_01 + " " + value_02 + " " + value_03 + " " + value_04);
		return "";
	}

	public boolean test(Object value_01, String value_02, String value_03) {
		BaseTest.logInfo(this.getClass()::getName, "TriPredicate: " + value_01 + " " + value_02 + " " + value_03);
		return true;
	}

	public void accept(String value_01) {
		BaseTest.logInfo(this.getClass()::getName, "Consumer: " + value_01);
	}

	public void accept(String value_01, String value_02) {
		BaseTest.logInfo(this.getClass()::getName, "BiConsumer: " + value_01 + " " + value_02);
	}

	public void accept(String value_01, String value_02, String value_03) {
		BaseTest.logInfo(this.getClass()::getName, "TriConsumer: " + value_01 + " " + value_02 + " " + value_03);
	}

	public static void staticAccept(Service service, String value_01, String value_02, String value_03) {
		BaseTest.logInfo(Service.class::getName, "QuadConsumer: " + value_01 + " " + value_02 + " " + value_03);
	}

	public void withArray(String[] values) {
		BaseTest.logInfo(this.getClass()::getName, "withArray: " + String.join(", ", Arrays.asList(values)));
	}

	public interface Interf {

		public default void printSomethingNew() {
			System.out.println("Nothing new");
		}

	}
}
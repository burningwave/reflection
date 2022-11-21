package org.burningwave.reflection.service;

import org.burningwave.Strings;
import org.burningwave.function.Supplier;
import org.burningwave.reflection.BaseTest;


public class Service extends ServiceInterface {


	private String name;

	public Service() {
		this("Default name");
	}

	public Service(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void printName() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "My name is {}", this.name);
	}

	public static Object retrieve() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "static retrieve");
		return new Object();
	}

	@SuppressWarnings("unused")
	private Service supply() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "supply");
		return new Service();
	}

	public static void consume(final Integer obj) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "Consumer Integer: " + obj.toString());
	}

	public void consume(final String obj) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "Consumer String: " + obj);
	}

	public String apply(final String obj) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "Function String execute String: " + obj);
		return obj;
	}

	public Long apply(final Long obj) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "Function Long execute String: " + obj);
		return obj;
	}

	public void run() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "static run");
	}

	public void methodWithVarArgs(final String... arg) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "methodWithVarArgs");
	}

	public static void staticRun() {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "static run");
	}

//	public String apply(String value_01, String value_02) {
//		BaseTest.logInfo(() -> this.getClass().getName, "BiFunction: " + value_01 + " " + value_02);
//		return "";
//	}

	public String apply(final String value_01, final String value_02, final String... value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public String apply(final Object value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public static String staticApply(final Object value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "TriFunction: " + value_01 + " " + value_02 + " " + value_03);
		return "";
	}

	public static String staticApply(final Object value_01, final String value_02, final String value_03, final String... value_04) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "TriFunction: " + value_01 + " " + value_02 + " " + value_03 + " " + value_04);
		return "";
	}

	public boolean test(final Object value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "TriPredicate: " + value_01 + " " + value_02 + " " + value_03);
		return true;
	}

	public void accept(final String value_01) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "Consumer: " + value_01);
	}

	public void accept(final String value_01, final String value_02) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "BiConsumer: " + value_01 + " " + value_02);
	}

	public void accept(final String value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "TriConsumer: " + value_01 + " " + value_02 + " " + value_03);
	}

	public static void staticAccept(final Service service, final String value_01, final String value_02, final String value_03) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.class.getName();
			}
		}, "QuadConsumer: " + value_01 + " " + value_02 + " " + value_03);
	}

	public void withArray(final String[] values) {
		BaseTest.logInfo(new Supplier<String>() {
			@Override
			public String get() {
				return Service.this.getClass().getName();
			}
		}, "withArray: " + Strings.INSTANCE.join(", ", values));
	}

	/*public interface Interf {

		public static void printSomethingNew() {
			System.out.println("Nothing new");
		}

	}*/
}
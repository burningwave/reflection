package org.burningwave.reflection;

import java.util.Collection;

import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingSupplier;
import org.burningwave.function.ThrowingTriPredicate;
import org.burningwave.reflection.service.Service;
import org.junit.Test;



@SuppressWarnings("all")
public class MethodsTest extends BaseTest {

	@Test
	public void invokeTestOne() {
		testNotNull(
			new ThrowingSupplier<Object, Throwable>() {
				@Override
				public Object get() throws Throwable {
					return Methods.INSTANCE.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);
				}
			}
		);
	}

	@Test
	public void invokeDirectTestOne() {
		testNotNull(
			new ThrowingSupplier<Object, Throwable>() {
				@Override
				public Object get() throws Throwable {
					return Methods.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);
				}
			}
		);
	}

	@Test
	public void findAllAndMakeThemAccessibleTestOne() {
		testNotEmpty(
			new ThrowingSupplier<Collection<?>, Throwable>() {
				@Override
				public Collection<?> get() throws Throwable {
					Methods.INSTANCE.findAllAndMakeThemAccessible(System.out.getClass());
					return Methods.INSTANCE.findAllAndMakeThemAccessible(System.out.getClass());
				}
			},
		true);
	}

	@Test
	public void invokeVoidTestOne() {
		testDoesNotThrow(
			new Runnable() {
				@Override
				public void run() {
					Methods.INSTANCE.invoke(System.out, "println", "Hello World");
				}
			}
		);
	}

	@Test
	public void invokeVoidTestThree() {
		testDoesNotThrow(
			new Runnable() {
				@Override
				public void run() {
					final Object empty = new Object() {
						void print(final String value) {
							System.out.println(value);
						}
					};
					Methods.INSTANCE.invoke(empty, "print", null);
				}
			}
		);
	}

	@Test
	public void invokeDirectVoidTestThree() {
		testDoesNotThrow(
			new Runnable() {
				@Override
				public void run() {
					final Object empty = new Object() {
						void print(final String value) {
							System.out.println(value);
						}
					};
					Methods.INSTANCE.invoke(empty, "print", null);
				}
			}
		);
	}

	@Test
	public void invokeDirectVoidTestTwo() {
		testDoesNotThrow(
			new Runnable() {
				@Override
				public void run() {
					Methods.INSTANCE.invoke(System.out, "println", "Hello World");
				}
			}
		);
	}

	@Test
	public void invokeVoidTestTwo() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", new String[]{"How are you?"});
			}
		});
	}

	@Test
	public void invokeInterfaceDefaultMethod() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "printMyName");
			}
		});
	}

	@Test
	public void invokeDirectInterfaceDefault() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "printMyName");
			}
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", new String[]{"How are you?"});
			}
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", null);
			}
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", null);
			}
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestThree() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!");
			}
		});
	}

	@Test
	public void invokeStaticTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?");
			}
		});
	}

	@Test
	public void invokeStaticWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?", "I'm well");
			}
		});
	}

	@Test
	public void invokeStaticDirectWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?", "I'm well");
			}
		});
	}

	@Test
	public void invokeDirectStaticTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?");
			}
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestThree() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "");
			}
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestFour() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again", "... And again");
			}
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestFour() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again", "... And again");
			}
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestFive() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again");
			}
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestFive() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again");
			}
		});
	}

	@Test
	public void invokeNoArgs() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "supply");
			}
		});
	}

	@Test
	public void invokeDirectNoArgs() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "supply");
			}
		});
	}

	@Test
	public void invokeMethodWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs");
			}
		});
	}

	@Test
	public void invokeDirectMethodWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs");
			}
		});
	}

	@Test
	public void invokeMethodWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs", "Hello!");
			}
		});
	}

	@Test
	public void invokeDirectMethodWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs", "Hello!");
			}
		});
	}

	@Test
	public void invokeDirectMethodWithArrayTestOne() throws Throwable {
		testDoesNotThrow(new Runnable() {
			@Override
			public void run() {
				Methods.INSTANCE.invoke(new Service(), "withArray", new Object[] {new String[] {"methodWithArray"}});
			}
		});
	}

	@Test
	public void findAllTestOne() {
        testNotEmpty(new ThrowingSupplier<Collection<?>, Throwable>() {
			@Override
			public Collection<?> get() throws Throwable {
				return Methods.INSTANCE.findAll(
				    MethodCriteria.byScanUpTo(new ThrowingPredicate<Class<?>, Throwable>() {
						@Override
						public boolean test(Class<?> cls) throws Throwable {
							return //We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
							cls.getName().equals(ClassLoader.class.getName());
						}
					}
				    ).parameter(new ThrowingTriPredicate<Class<?>[], Boolean, Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Boolean isVarArgs, Integer idx) throws Throwable {
						    return Classes.INSTANCE.isAssignableFrom(params[idx], Class.class);
						}
					}), ClassLoader.class
				);
			}
		}, true
	    );
	}
}

package org.burningwave.reflection;

import org.burningwave.reflection.service.Service;
import org.junit.jupiter.api.Test;


@SuppressWarnings("all")
public class MethodsTest extends BaseTest {

	@Test
	public void invokeTestOne() {
		testNotNull(
			() -> {
				return Methods.INSTANCE.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);
			}
		);
	}

	@Test
	public void invokeDirectTestOne() {
		testNotNull(
			() -> {
				return Methods.INSTANCE.invokeStatic(Integer.class, "valueOf", 1);
			}
		);
	}

	@Test
	public void findAllAndMakeThemAccessibleTestOne() {
		testNotEmpty(
			() -> {
				Methods.INSTANCE.findAllAndMakeThemAccessible(System.out.getClass());
				return Methods.INSTANCE.findAllAndMakeThemAccessible(System.out.getClass());
			},
		true);
	}

	@Test
	public void invokeVoidTestOne() {
		testDoesNotThrow(
			() -> {
				Methods.INSTANCE.invoke(System.out, "println", "Hello World");
			}
		);
	}

	@Test
	public void invokeVoidTestThree() {
		testDoesNotThrow(
			() -> {
				Object empty = new Object() {
					void print(String value) {
						System.out.println(value);
					}
				};
				Methods.INSTANCE.invoke(empty, "print", null);
			}
		);
	}

	@Test
	public void invokeDirectVoidTestThree() {
		testDoesNotThrow(
			() -> {
				Object empty = new Object() {
					void print(String value) {
						System.out.println(value);
					}
				};
				Methods.INSTANCE.invoke(empty, "print", null);
			}
		);
	}

	@Test
	public void invokeDirectVoidTestTwo() {
		testDoesNotThrow(
			() -> {
				Methods.INSTANCE.invoke(System.out, "println", "Hello World");
			}
		);
	}

	@Test
	public void invokeVoidTestTwo() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", new String[]{"How are you?"});
		});
	}

	@Test
	public void invokeInterfaceDefaultMethod() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "printMyName");
		});
	}

	@Test
	public void invokeDirectInterfaceDefault() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "printMyName");
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", new String[]{"How are you?"});
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", null);
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", null);
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestThree() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!");
		});
	}

	@Test
	public void invokeStaticTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?");
		});
	}

	@Test
	public void invokeStaticWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?", "I'm well");
		});
	}

	@Test
	public void invokeStaticDirectWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?", "I'm well");
		});
	}

	@Test
	public void invokeDirectStaticTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invokeStatic(Service.class, "staticApply", "Hello", "World!", "How are you?");
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestThree() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "");
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestFour() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again", "... And again");
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestFour() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again", "... And again");
		});
	}

	@Test
	public void invokeVoidWithVarArgsTestFive() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again");
		});
	}

	@Test
	public void invokeDirectVoidWithVarArgsTestFive() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "apply", "Hello", "World!", "Hello again");
		});
	}

	@Test
	public void invokeNoArgs() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "supply");
		});
	}

	@Test
	public void invokeDirectNoArgs() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "supply");
		});
	}

	@Test
	public void invokeMethodWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs");
		});
	}

	@Test
	public void invokeDirectMethodWithVarArgsTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs");
		});
	}

	@Test
	public void invokeMethodWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs", "Hello!");
		});
	}

	@Test
	public void invokeDirectMethodWithVarArgsTestTwo() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "methodWithVarArgs", "Hello!");
		});
	}

	@Test
	public void invokeDirectMethodWithArrayTestOne() throws Throwable {
		testDoesNotThrow(() -> {
			Methods.INSTANCE.invoke(new Service(), "withArray", new Object[] {new String[] {"methodWithArray"}});
		});
	}

	@Test
	public void findAllTestOne() {
        testNotEmpty(() ->
	        Methods.INSTANCE.findAll(
	            MethodCriteria.byScanUpTo((cls) ->
	            	//We only analyze the ClassLoader class and not all of its hierarchy (default behavior)
	                cls.getName().equals(ClassLoader.class.getName())
	            ).parameter((params, idx) -> {
	                return Classes.INSTANCE.isAssignableFrom(params[idx].getType(), Class.class);
	            }), ClassLoader.class
	        ), true
	    );
	}
}

package org.burningwave.reflection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.reflection.service.ExtendedService;
import org.burningwave.reflection.service.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;


public class MembersTest extends BaseTest {

	@Test
	public void findOneTestOne() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Members.INSTANCE.findOne(
					MethodCriteria.forEntireClassHierarchy().name(new ThrowingPredicate<String, Throwable>() {
						@Override
						public boolean test(String name) throws Throwable {
							return name.matches("apply");
						}
					}
					).and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 0 && params[idx].equals(Object.class);
						}
					}
					).and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 1 && params[idx].equals(String.class);
						}
					}
					).and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 2 && params[idx].equals(String.class);
						}
					}
					),
					Service.class
				);
			}
		}
		);
	}


	@Test
	public void findOneTestTwo() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Members.INSTANCE.findOne(
					MethodCriteria.forEntireClassHierarchy()
					.name(new ThrowingPredicate<String, Throwable>() {
						@Override
						public boolean test(String name) throws Throwable {
							return name.matches("apply");
						}
					})
					.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 0 && params[idx].equals(Object.class);
						}
					}
					)
					.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 1 && params[idx].equals(String.class);
						}
					}
					)
					.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
						@Override
						public boolean test(Class<?>[] params, Integer idx) throws Throwable {
							return idx == 2 && params[idx].equals(String.class);
						}
					}
					).skip(new ThrowingBiPredicate<Class<?>, Class<?>, Throwable>() {
						@Override
						public boolean test(Class<?> initialClass, Class<?> cls) throws Throwable {
							return Object.class == cls || Service.class == cls;
						}
					}
					),
					ExtendedService.class
				);
			}
		}
		);
	}

	@Test
	public void matchOneTestOne() {
		assertTrue(
			Members.INSTANCE.match(
				MethodCriteria.forEntireClassHierarchy()
				.name(new ThrowingPredicate<String, Throwable>() {
					@Override
					public boolean test(String name) throws Throwable {
						return name.matches("apply");
					}
				})
				.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
					@Override
					public boolean test(Class<?>[] params, Integer idx) throws Throwable {
						return idx == 0 && params[idx].equals(Object.class);
					}
				}
				)
				.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
					@Override
					public boolean test(Class<?>[] params, Integer idx) throws Throwable {
						return idx == 1 && params[idx].equals(String.class);
					}
				}
				)
				.and().parameterType(new ThrowingBiPredicate<Class<?>[], Integer, Throwable>() {
					@Override
					public boolean test(Class<?>[] params, Integer idx) throws Throwable {
						return idx == 2 && params[idx].equals(String.class);
					}
				}
				).skip(new ThrowingBiPredicate<Class<?>, Class<?>, Throwable>() {
					@Override
					public boolean test(Class<?> initialClass, Class<?> cls) throws Throwable {
						return Object.class == cls || Service.class == cls;
					}
				}
				),
				ExtendedService.class
			)
		);
	}

	@Test
	public void findAllTestOne() {
		testNotEmpty(new ThrowingSupplier<Collection<?>>() {
			@Override
			public Collection<?> get() throws Throwable {
				return Members.INSTANCE.findAll(
					MethodCriteria.forEntireClassHierarchy().name(new ThrowingPredicate<String, Throwable>() {
						@Override
						public boolean test(String name) throws Throwable {
							return name.matches("loadClass");
						}
					}
					),
					MembersTest.this.getClass().getClassLoader().getClass()
				);
			}
		}
		);
	}

	@Test
	public void findFirstTestOne() {
		testNotNull(new ThrowingSupplier<Object>() {
			@Override
			public Object get() throws Throwable {
				return Members.INSTANCE.findFirst(
					MethodCriteria.forEntireClassHierarchy().name(new ThrowingPredicate<String, Throwable>() {
						@Override
						public boolean test(String name) throws Throwable {
							return name.matches("loadClass");
						}
					}
					),
					MembersTest.this.getClass().getClassLoader().getClass()
				);
			}
		}
		);
	}
}

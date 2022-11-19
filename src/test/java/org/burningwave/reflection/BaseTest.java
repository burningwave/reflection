package org.burningwave.reflection;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Function;
import org.burningwave.function.Supplier;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

@SuppressWarnings("unused")
//@TestMethodOrder(Random.class)
//@TestMethodOrder(MethodName.class)
public class BaseTest {

	public void testNotNull(final ThrowingSupplier<?> supplier) {
		final long initialTime = System.currentTimeMillis();
		Object object = null;
		try {
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - start execution");
			object = supplier.get();
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - end execution");
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
		}
		logInfo(
			new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			},
			getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(),initialTime)
		);
		assertNotNull(object);
	}

	protected void testNotEmpty(final ThrowingSupplier<Collection<?>> supplier) {
		testNotEmpty(supplier, false);
	}

	protected void testNotEmpty(final ThrowingSupplier<Collection<?>> supplier, final boolean printAllElements) {
		final long initialTime = System.currentTimeMillis();
		Collection<?> coll = null;
		boolean isNotEmpty = false;
		try {
			coll = supplier.get();
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Found " + coll.size() + " items in " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
			isNotEmpty = !coll.isEmpty();
			if (isNotEmpty && printAllElements) {
				for (final Object obj : coll) {
					if (obj != null) {
						logDebug(new Supplier<String>() {
							@Override
							public String get() {
								return getClass().getName();
							}
						}, "{}", obj);
					}
				}
			}
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
		}
		assertTrue(coll != null && !coll.isEmpty());
	}

	private String getCallerMethod() {
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (final StackTraceElement stackTraceElement : stackTraceElements) {
			final String className = stackTraceElement.getClassName();
			if (className.contains("Test") && !className.contains("BaseTest")) {
				return stackTraceElement.getMethodName();
			}
		}
		return null;
	}

	public <T extends AutoCloseable> void testNotEmpty(final Supplier<T> autoCloaseableSupplier, final Function<T, Collection<?>> collSupplier) {
		testNotEmpty(autoCloaseableSupplier, collSupplier, false);
	}

	public <T extends AutoCloseable> void testNotEmpty(final Supplier<T> autoCloaseableSupplier, final Function<T, Collection<?>> collSupplier, final boolean printAllElements) {
		final long initialTime = System.currentTimeMillis();
		Collection<?> coll = null;
		boolean isNotEmpty = false;
		try (T collectionSupplier = autoCloaseableSupplier.get()){
			coll = collSupplier.apply(collectionSupplier);
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Found " + coll.size() + " items in " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
			isNotEmpty = !coll.isEmpty();
			if (isNotEmpty && printAllElements) {
				int line = 0;
				final Iterator<?> collIterator = coll.iterator();
				while (collIterator.hasNext()) {
					final Object obj = collIterator.next();
					logDebug(
						new Supplier<String>() {
							@Override
							public String get() {
								return getClass().getName();
							}
						},
						++line + " " + (obj != null ? obj.toString() : "null")
					);
				}
			}
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
		}
		logInfo(
			new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			},
			getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(),initialTime)
		);
		assertTrue(isNotEmpty);
	}


	public <T extends AutoCloseable> void testNotNull(
		final ThrowingSupplier<T> autoCloseableSupplier,
		final Function<T, ?> objectSupplier
	) {
		final long initialTime = System.currentTimeMillis();
		try (T autoCloseable = autoCloseableSupplier.get()) {
			assertNotNull(objectSupplier.apply(autoCloseable));
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
		}
	}


	public void testDoesNotThrow(final Executable executable) {
		Throwable throwable = null;
		final long initialTime = System.currentTimeMillis();
		try {
			logDebug(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Initializing logger");
			executable.execute();
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
			throwable = exc;
		}
		assertNull(throwable);
	}

	public void testThrow(final Executable executable) {
		Throwable throwable = null;
		final long initialTime = System.currentTimeMillis();
		try {
			logDebug(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Initializing logger");
			executable.execute();
			logInfo(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (final Throwable exc) {
			logError(new Supplier<String>() {
				@Override
				public String get() {
					return getClass().getName();
				}
			}, getCallerMethod() + " - Exception occurred", exc);
			throwable = exc;
		}
		assertNotNull(throwable);
	}


	String getFormattedDifferenceOfMillis(final long value1, final long value2) {
		final String valueFormatted = String.format("%04d", (value1 - value2));
		return valueFormatted.substring(0, valueFormatted.length() - 3) + "," + valueFormatted.substring(valueFormatted.length() -3);
	}

	void waitFor(final long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (final InterruptedException exc) {
			Throwables.INSTANCE.throwException(exc);
		}
	}

	public static void logError(final Supplier<String> classNameSupplier, final String message, final Throwable exc) {
		System.err.println(classNameSupplier.get() + " - " + message);
		exc.printStackTrace();
	}

	public static void logInfo(final Supplier<String> classNameSupplier, final String... arguments) {
		System.out.println(classNameSupplier.get() + " - " + String.join("; ", arguments));

	}

	public static void logDebug(final Supplier<String> classNameSupplier, final String message, final Object... arguments) {
		System.out.println(classNameSupplier.get() + " - " + Strings.INSTANCE.compile(message, arguments));
	}

}
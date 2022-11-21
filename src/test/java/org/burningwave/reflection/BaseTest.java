package org.burningwave.reflection;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

@SuppressWarnings("unused")
//@TestMethodOrder(Random.class)
//@TestMethodOrder(MethodName.class)
public class BaseTest {

	public void testNotNull(ThrowingSupplier<?> supplier) {
		long initialTime = System.currentTimeMillis();
		Object object = null;
		try {
			logInfo(getClass()::getName, getCallerMethod() + " - start execution");
			object = supplier.get();
			logInfo(getClass()::getName, getCallerMethod() + " - end execution");
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
		}
		logInfo(
			getClass()::getName,
			getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(),initialTime)
		);
		assertNotNull(object);
	}

	protected void testNotEmpty(ThrowingSupplier<Collection<?>> supplier) {
		testNotEmpty(supplier, false);
	}

	protected void testNotEmpty(ThrowingSupplier<Collection<?>> supplier, boolean printAllElements) {
		long initialTime = System.currentTimeMillis();
		Collection<?> coll = null;
		boolean isNotEmpty = false;
		try {
			coll = supplier.get();
			logInfo(getClass()::getName, getCallerMethod() + " - Found " + coll.size() + " items in " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
			isNotEmpty = !coll.isEmpty();
			if (isNotEmpty && printAllElements) {
				for (Object obj : coll) {
					if (obj != null) {
						logDebug(getClass()::getName, "{}", obj);
					}
				}
			}
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
		}
		assertTrue(coll != null && !coll.isEmpty());
	}

	private String getCallerMethod() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			String className = stackTraceElement.getClassName();
			if (className.contains("Test") && !className.contains("BaseTest")) {
				return stackTraceElement.getMethodName();
			}
		}
		return null;
	}

	public <T extends AutoCloseable> void testNotEmpty(Supplier<T> autoCloaseableSupplier, Function<T, Collection<?>> collSupplier) {
		testNotEmpty(autoCloaseableSupplier, collSupplier, false);
	}

	public <T extends AutoCloseable> void testNotEmpty(Supplier<T> autoCloaseableSupplier, Function<T, Collection<?>> collSupplier, boolean printAllElements) {
		long initialTime = System.currentTimeMillis();
		Collection<?> coll = null;
		boolean isNotEmpty = false;
		try (T collectionSupplier = autoCloaseableSupplier.get()){
			coll = collSupplier.apply(collectionSupplier);
			logInfo(getClass()::getName, getCallerMethod() + " - Found " + coll.size() + " items in " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
			isNotEmpty = !coll.isEmpty();
			if (isNotEmpty && printAllElements) {
				int line = 0;
				Iterator<?> collIterator = coll.iterator();
				while (collIterator.hasNext()) {
					Object obj = collIterator.next();
					logDebug(
						getClass()::getName,
						++line + " " + (obj != null ? obj.toString() : "null")
					);
				}
			}
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
		}
		logInfo(
			getClass()::getName,
			getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(),initialTime)
		);
		assertTrue(isNotEmpty);
	}


	public <T extends AutoCloseable> void testNotNull(
		ThrowingSupplier<T> autoCloseableSupplier,
		Function<T, ?> objectSupplier
	) {
		long initialTime = System.currentTimeMillis();
		try (T autoCloseable = autoCloseableSupplier.get()) {
			assertNotNull(objectSupplier.apply(autoCloseable));
			logInfo(getClass()::getName, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
		}
	}


	public void testDoesNotThrow(Executable executable) {
		Throwable throwable = null;
		long initialTime = System.currentTimeMillis();
		try {
			logDebug(getClass()::getName, getCallerMethod() + " - Initializing logger");
			executable.execute();
			logInfo(getClass()::getName, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
			throwable = exc;
		}
		assertNull(throwable);
	}

	public void testThrow(Executable executable) {
		Throwable throwable = null;
		long initialTime = System.currentTimeMillis();
		try {
			logDebug(getClass()::getName, getCallerMethod() + " - Initializing logger");
			executable.execute();
			logInfo(getClass()::getName, getCallerMethod() + " - Elapsed time: " + getFormattedDifferenceOfMillis(System.currentTimeMillis(), initialTime));
		} catch (Throwable exc) {
			logError(getClass()::getName, getCallerMethod() + " - Exception occurred", exc);
			throwable = exc;
		}
		assertNotNull(throwable);
	}


	String getFormattedDifferenceOfMillis(long value1, long value2) {
		String valueFormatted = String.format("%04d", (value1 - value2));
		return valueFormatted.substring(0, valueFormatted.length() - 3) + "," + valueFormatted.substring(valueFormatted.length() -3);
	}

	void waitFor(long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException exc) {
			Throwables.INSTANCE.throwException(exc);
		}
	}

	public static void logError(Supplier<String> classNameSupplier, String message, Throwable exc) {
		System.err.println(classNameSupplier.get() + " - " + message);
		exc.printStackTrace();
	}

	public static void logInfo(Supplier<String> classNameSupplier, String... arguments) {
		System.out.println(classNameSupplier.get() + " - " + String.join("; ", arguments));
	}

	public static void logDebug(Supplier<String> classNameSupplier, String message, Object... arguments) {
		System.out.println(classNameSupplier.get() + " - " + Strings.INSTANCE.compile(message, arguments));
	}

}
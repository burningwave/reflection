/*
 * This file is part of Burningwave Reflection.
 *
 * Author: Roberto Gentili
 *
 * Hosted at: https://github.com/burningwave/reflection
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2022 Roberto Gentili
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.burningwave.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Executor;
import org.burningwave.function.ThrowingBiConsumer;
import org.burningwave.function.ThrowingBiFunction;
import org.burningwave.function.ThrowingFunction;
import org.burningwave.function.ThrowingTriConsumer;
import org.burningwave.function.ThrowingTriFunction;

import io.github.toolfactory.jvm.Driver;

@SuppressWarnings("unchecked")
public class Facade {
	public static final Facade INSTANCE;
	private static Driver driver;
	private static ThrowingBiFunction<MethodHandles.Lookup, Class<?>, MethodHandles.Lookup, Throwable> privateLookupIn;

	static {
		if ((boolean)Configuration.freezeAndGet().get(Configuration.Key.JVM_DRIVER_ENABLED)) {
			try {
				driver = io.github.toolfactory.jvm.Driver.Factory.getNewDynamic();
			} catch (Throwable exc) {
				System.err.println(Strings.INSTANCE.compile("JVM driver not instantiated: {}", exc.getMessage()));
			}
		}
		MethodHandles.Lookup consulter = MethodHandles.lookup();
		MethodHandle privateLookupIn = Executor.getFirst(() ->
			consulter.findStatic(
				MethodHandles.class, "privateLookupIn",
				MethodType.methodType(MethodHandles.Lookup.class, Class.class, MethodHandles.Lookup.class)
			), () ->
			consulter.findSpecial(
				MethodHandles.Lookup.class, "in",
				MethodType.methodType(MethodHandles.Lookup.class, Class.class),
				MethodHandles.Lookup.class
			)
		);
		Facade.privateLookupIn = (clazz, cons) ->
			(MethodHandles.Lookup)privateLookupIn.invokeWithArguments(cons, clazz);
		INSTANCE = new Facade();
	}

	private Collection<ThrowingFunction<Class<?>, Field[], Throwable>> fieldRetrievers;
	private Collection<ThrowingFunction<Class<?>, Method[], Throwable>> methodRetrievers;
	private Collection<ThrowingFunction<Class<?>, Constructor<?>[], Throwable>> constructorRetrievers;
	private Collection<ThrowingBiConsumer<AccessibleObject, Boolean, Throwable>> accessibleSetters;
	private Collection<ThrowingBiFunction<Object, Field, Object, Throwable>> fieldValueRetrievers;
	private Collection<ThrowingTriConsumer<Object, Field, Object, Throwable>> fieldValueSetters;
	private Collection<ThrowingTriFunction<Object, Method, Object[], Object, Throwable>> methodInvokers;
	private Collection<ThrowingBiFunction<Constructor<?>, Object[], Object, Throwable>> constructorInvokers;
	private Collection<ThrowingBiFunction<MethodHandles.Lookup, Class<?>, MethodHandles.Lookup, Throwable>> consulterRetrievers;

	private Facade() {
		fieldRetrievers = new ArrayList<>();
		methodRetrievers = new ArrayList<>();
		constructorRetrievers = new ArrayList<>();
		accessibleSetters = new ArrayList<>();
		fieldValueRetrievers = new ArrayList<>();
		fieldValueSetters = new ArrayList<>();
		methodInvokers = new ArrayList<>();
		constructorInvokers = new ArrayList<>();
		consulterRetrievers = new ArrayList<>();

		if (driver != null) {
			fieldRetrievers.add(clazz ->
				driver.getDeclaredFields(clazz)
			);
			methodRetrievers.add(clazz ->
				driver.getDeclaredMethods(clazz)
			);
			constructorRetrievers.add(clazz ->
				driver.getDeclaredConstructors(clazz)
			);
			accessibleSetters.add((accessibleObject, flag) ->
				driver.setAccessible(accessibleObject, flag)
			);
			fieldValueRetrievers.add((target, field) ->
				driver.getFieldValue(target, field)
			);
			fieldValueSetters.add((target, field, value) ->
				driver.setFieldValue(target, field, value)
			);
			methodInvokers.add((target, method, parameters) ->
				driver.invoke(target, method, parameters)
			);
			constructorInvokers.add((constructor, parameters) ->
				driver.newInstance(constructor, parameters)
			);
			consulterRetrievers.add((lookup, clazz) ->
				driver.getConsulter(clazz)
			);
		}
		fieldRetrievers.add(clazz ->
			clazz.getDeclaredFields()
		);
		methodRetrievers.add(clazz ->
			clazz.getDeclaredMethods()
		);
		constructorRetrievers.add(clazz ->
			clazz.getDeclaredConstructors()
		);
		accessibleSetters.add((accessibleObject, flag) ->
			accessibleObject.setAccessible(flag)
		);
		fieldValueRetrievers.add((target, field) ->
			field.get(target)
		);
		fieldValueRetrievers.add((target, field) ->
			setAccessible(field, true).get(target)
		);
		fieldValueSetters.add((target, field, value) ->
			field.set(target, value)
		);
		fieldValueSetters.add((target, field, value) ->
			setAccessible(field, true).set(target, value)
		);
		methodInvokers.add((target, method, parameters) ->
			method.invoke(target, parameters)
		);
		methodInvokers.add((target, method, parameters) ->
			setAccessible(method, true).invoke(target, parameters)
		);
		constructorInvokers.add((constructor, parameters) ->
			constructor.newInstance(parameters)
		);
		constructorInvokers.add((constructor, parameters) ->
			setAccessible(constructor, true).newInstance(parameters)
		);
		consulterRetrievers.add((consulter, clazz) ->
			MethodHandles.lookup()
		);
		consulterRetrievers.add((consulter, clazz) ->
			privateLookupIn.apply(consulter, clazz)
		);
	}

	public Field[] getDeclaredFields(Class<?> clazz) {
		Throwable exception = null;
		for (ThrowingFunction<Class<?>, Field[], Throwable> fieldRetriever : fieldRetrievers) {
			try {
				return fieldRetriever.apply(clazz);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public <T> Constructor<T>[] getDeclaredConstructors(Class<T> clazz) {
		Throwable exception = null;
		for (ThrowingFunction<Class<?>, Constructor<?>[], Throwable> constructorRetriever : constructorRetrievers) {
			try {
				return (Constructor<T>[])constructorRetriever.apply(clazz);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public Method[] getDeclaredMethods(Class<?> clazz) {
		Throwable exception = null;
		for (ThrowingFunction<Class<?>, Method[], Throwable> methodRetriever : methodRetrievers) {
			try {
				return methodRetriever.apply(clazz);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public <R> Map.Entry<MethodHandles.Lookup, R> executeWithConsulter(Class<?> cls, ThrowingFunction<MethodHandles.Lookup, R, ? extends Throwable> executor) {
		Throwable exception = null;
		MethodHandles.Lookup consulter = null;
		for (ThrowingBiFunction<Lookup, Class<?>, Lookup, Throwable> consulterRetriever : consulterRetrievers) {
			try {
				return new AbstractMap.SimpleEntry<>(
					consulter = consulterRetriever.apply(consulter, cls),
					executor.apply(consulter)
				);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public <A extends AccessibleObject> A setAccessible(A accessibleObject, boolean flag) {
		Throwable exception = null;
		for (ThrowingBiConsumer<AccessibleObject, Boolean, Throwable> accessibleSetter : accessibleSetters) {
			try {
				accessibleSetter.accept(accessibleObject, flag);
				return accessibleObject;
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public <T> T newInstance(Constructor<T> constructor, Object... parameters) {
		if (parameters == null) {
			parameters = new Object[] {null};
		}
		Throwable exception = null;
		for (ThrowingBiFunction<Constructor<?>, Object[], Object, Throwable> constructorInvoker : constructorInvokers) {
			try {
				return (T)constructorInvoker.apply(constructor, parameters);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public <T> T getFieldValue(Object target, Field field) {
		Throwable exception = null;
		for (ThrowingBiFunction<Object, Field, Object, Throwable> fieldValueRetriever : fieldValueRetrievers) {
			try {
				return (T)fieldValueRetriever.apply(target, field);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public void setFieldValue(Object target, Field field, Object value) {
		Throwable exception = null;
		for (ThrowingTriConsumer<Object, Field, Object, Throwable> fieldValueSetter : fieldValueSetters) {
			try {
				fieldValueSetter.accept(target, field, value);
				return;
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		Throwables.INSTANCE.throwException(exception);
	}

	public <T> T invoke(Object target, Method method, Object[] params) {
		if (params == null) {
			params = new Object[] {null};
		}
		Throwable exception = null;
		for (ThrowingTriFunction<Object, Method, Object[], Object, Throwable> methodInvoker : methodInvokers) {
			try {
				return (T)methodInvoker.apply(target, method, params);
			} catch (Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

	public static final class Configuration {

		private Configuration() {}

		private static Map<String, Object> values;
		private static boolean freezed;

		public static final class Key {

			private Key() {}

			private static final String JVM_DRIVER_ENABLED = "jvm-driver.enabled";
		}

		static {
			values = new LinkedHashMap<>();
			putConfigurationValue(Configuration.Key.JVM_DRIVER_ENABLED, true);
		}

		public static void disableOriginalObjectRetriever() {
			putConfigurationValue(Configuration.Key.JVM_DRIVER_ENABLED, false);
		}

		private static void putConfigurationValue(String key, Object value) {
			try {
				values.put(key, value);
			} catch (UnsupportedOperationException exc) {
				throw new UnsupportedOperationException("Cannot add configuration value after that the " + Facade.class.getSimpleName() + " has been initialized");
			}
		}

		private static Map<String, Object> freezeAndGet() {
			if (!freezed) {
				values = Collections.unmodifiableMap(Configuration.values);
			}
			return values;
		}

	}

}

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
 * Copyright (c) 2022 Roberto Gentili
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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Consumer;
import org.burningwave.function.Executor;
import org.burningwave.function.Function;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingSupplier;


@SuppressWarnings("unchecked")
public class Constructors extends Members.Handler.OfExecutable<Constructor<?>, ConstructorCriteria>  {
	public final static Constructors INSTANCE;

	static {
		INSTANCE = new Constructors();
	}

	private Constructors() {}

	public Collection<Constructor<?>> findAllAndMakeThemAccessible(
		Class<?> targetClass
	) {
		String cacheKey = getCacheKey(targetClass, "all constructors");
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Collection<Constructor<?>> members = Cache.INSTANCE.uniqueKeyForConstructors.getOrUploadIfAbsent(
			targetClassClassLoader, cacheKey, new Supplier<Collection<Constructor<?>>>() {
				@Override
				public Collection<Constructor<?>> get() {
					return findAllAndApply(
						ConstructorCriteria.withoutConsideringParentClasses(), targetClass, new Consumer<Constructor<?>>() {
							@Override
							public void accept(Constructor<?> member) {
								setAccessible(member, true);
							}
						}
					);
				}
			}
		);
		return members;
	}

	public Collection<Constructor<?>> findAllAndMakeThemAccessible(
		Class<?> targetClass,
		Class<?>... inputParameterTypesOrSubTypes
	) {
		String cacheKey = getCacheKey(targetClass, "all constructors by input parameters assignable from", inputParameterTypesOrSubTypes);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForConstructors.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Constructor<?>>>() {
			@Override
			public Collection<Constructor<?>> get() {
				ConstructorCriteria criteria = ConstructorCriteria.withoutConsideringParentClasses().parameterTypesAreAssignableFrom(inputParameterTypesOrSubTypes);
				if ((inputParameterTypesOrSubTypes != null) && (inputParameterTypesOrSubTypes.length == 0)) {
					criteria.or().parameter(new ThrowingBiPredicate<Parameter[], Integer, Throwable>() {
						@Override
						public boolean test(Parameter[] parameters, Integer idx) {
							return (parameters.length == 1) && parameters[0].isVarArgs();
						}
					});
				}
				return findAllAndApply(
					criteria,
					targetClass,
					new Consumer<Constructor<?>>() {
						@Override
						public void accept(Constructor<?> member) {
							setAccessible(member, true);
						}
					}
				);
			}
		});
	}

	public MethodHandle findDirectHandle(Class<?> targetClass, Class<?>... inputParameterTypesOrSubTypes) {
		return findDirectHandleBox(targetClass, inputParameterTypesOrSubTypes).getHandler();
	}

	public Constructor<?> findFirstAndMakeItAccessible(Class<?> targetClass, Class<?>... inputParameterTypesOrSubTypes) {
		Collection<Constructor<?>> members = findAllAndMakeThemAccessible(targetClass, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.stream().findFirst().get();
		} else if (members.size() > 1) {
			Collection<Constructor<?>> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (!membersThatMatch.isEmpty()) {
				return membersThatMatch.stream().findFirst().get();
			}
			return members.stream().findFirst().get();
		}
		return null;
	}

	public Constructor<?> findOneAndMakeItAccessible(Class<?> targetClass, Class<?>... argumentTypes) {
		Collection<Constructor<?>> members = findAllAndMakeThemAccessible(targetClass, argumentTypes);
		if (members.size() == 1) {
			return members.stream().findFirst().get();
		} else if (members.size() > 1) {
			Collection<Constructor<?>> membersThatMatch = searchForExactMatch(members, argumentTypes);
			if (membersThatMatch.size() == 1) {
				return membersThatMatch.stream().findFirst().get();
			}
			Throwables.INSTANCE.throwException(
				"Found more than one of constructor with argument types {} in {} class",
				Strings.INSTANCE.join(", ", argumentTypes, new Function<Class<?>, String>() {
					@Override
					public String apply(Class<?> cls) {
						return cls.getName();
					}
				}),
				targetClass.getName()
			);
		}
		return null;
	}

	public <T> T newInstanceOf(
		Class<?> targetClass,
		Object... arguments
	) {
		return Executor.getFirst(
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					Class<?>[] argsType = Classes.INSTANCE.retrieveFrom(arguments);
					Members.Handler.OfExecutable.Box<Constructor<?>> methodHandleBox = findDirectHandleBox(targetClass, argsType);
					return Executor.get(new ThrowingSupplier<T, Throwable>() {
						@Override
						public T get() throws Throwable {
								Constructor<?> ctor = methodHandleBox.getExecutable();
								return (T)methodHandleBox.getHandler().invokeWithArguments(
									getFlatArgumentList(ctor, ArrayList::new, arguments)
								);
							}
					}
					);
				}
			}, new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					Constructor<?> ctor = findFirstAndMakeItAccessible(targetClass, Classes.INSTANCE.retrieveFrom(arguments));
					if (ctor == null) {
						Throwables.INSTANCE.throwException("Constructor not found in {}", targetClass.getName());
					}
					return (T)Facade.INSTANCE.newInstance(
						ctor,
						getArgumentArray(
							ctor,
							Constructors.this::getArgumentListWithArrayForVarArgs,
							ArrayList::new,
							arguments
						)
					);
				}
			}
		);

	}

	@Override
	MethodHandle retrieveMethodHandle(MethodHandles.Lookup consulter, Constructor<?> constructor) throws NoSuchMethodException, IllegalAccessException {
		return consulter.findConstructor(
			constructor.getDeclaringClass(),
			MethodType.methodType(void.class, constructor.getParameterTypes())
		);
	}

	String retrieveNameForCaching(Class<?> cls) {
		return Classes.INSTANCE.retrieveSimpleName(cls.getName());
	}

	@Override
	String retrieveNameForCaching(Constructor<?> constructor) {
		return retrieveNameForCaching(constructor.getDeclaringClass());
	}

	private Members.Handler.OfExecutable.Box<Constructor<?>> findDirectHandleBox(Class<?> targetClass, Class<?>... inputParameterTypesOrSubTypes) {
		String nameForCaching = retrieveNameForCaching(targetClass);
		String cacheKey = getCacheKey(targetClass, "equals " + nameForCaching, inputParameterTypesOrSubTypes);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Members.Handler.OfExecutable.Box<Constructor<?>> entry =
			(Box<Constructor<?>>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.get(targetClassClassLoader, cacheKey);
		if (entry == null) {
			Constructor<?> ctor = findFirstAndMakeItAccessible(targetClass, inputParameterTypesOrSubTypes);
			entry = findDirectHandleBox(
				ctor, targetClassClassLoader, cacheKey
			);
		}
		return entry;
	}
}

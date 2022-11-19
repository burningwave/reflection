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
import java.util.List;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Consumer;
import org.burningwave.function.Function;
import org.burningwave.function.Handler;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingSupplier;
import org.burningwave.function.ThrowingTriFunction;


@SuppressWarnings("unchecked")
public class Constructors extends Members.Handler.OfExecutable<Constructor<?>, ConstructorCriteria>  {
	public final static Constructors INSTANCE;

	static {
		INSTANCE = new Constructors();
	}

	private Constructors() {}

	public Collection<Constructor<?>> findAllAndMakeThemAccessible(
		final Class<?> targetClass
	) {
		final String cacheKey = getCacheKey(targetClass, "all constructors");
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		final Collection<Constructor<?>> members = Cache.INSTANCE.uniqueKeyForConstructors.getOrUploadIfAbsent(
			targetClassClassLoader, cacheKey, new Supplier<Collection<Constructor<?>>>() {
				@Override
				public Collection<Constructor<?>> get() {
					return findAllAndApply(
						ConstructorCriteria.withoutConsideringParentClasses(), targetClass, new Consumer<Constructor<?>>() {
							@Override
							public void accept(final Constructor<?> member) {
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
		final Class<?> targetClass,
		final Class<?>... inputParameterTypesOrSubTypes
	) {
		final String cacheKey = getCacheKey(targetClass, "all constructors by input parameters assignable from", inputParameterTypesOrSubTypes);
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForConstructors.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Constructor<?>>>() {
			@Override
			public Collection<Constructor<?>> get() {
				final ConstructorCriteria criteria = ConstructorCriteria.withoutConsideringParentClasses().parameterTypesAreAssignableFrom(inputParameterTypesOrSubTypes);
				if ((inputParameterTypesOrSubTypes != null) && (inputParameterTypesOrSubTypes.length == 0)) {
					criteria.or().parameter(new ThrowingBiPredicate<Parameter[], Integer, Throwable>() {
						@Override
						public boolean test(final Parameter[] parameters, final Integer idx) {
							return (parameters.length == 1) && parameters[0].isVarArgs();
						}
					});
				}
				return findAllAndApply(
					criteria,
					targetClass,
					new Consumer<Constructor<?>>() {
						@Override
						public void accept(final Constructor<?> member) {
							setAccessible(member, true);
						}
					}
				);
			}
		});
	}

	public MethodHandle findDirectHandle(final Class<?> targetClass, final Class<?>... inputParameterTypesOrSubTypes) {
		return findDirectHandleBox(targetClass, inputParameterTypesOrSubTypes).getHandler();
	}

	public Constructor<?> findFirstAndMakeItAccessible(final Class<?> targetClass, final Class<?>... inputParameterTypesOrSubTypes) {
		final Collection<Constructor<?>> members = findAllAndMakeThemAccessible(targetClass, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.iterator().next();
		} else if (members.size() > 1) {
			final Collection<Constructor<?>> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (!membersThatMatch.isEmpty()) {
				return membersThatMatch.iterator().next();
			}
			return members.iterator().next();
		}
		return null;
	}

	public Constructor<?> findOneAndMakeItAccessible(final Class<?> targetClass, final Class<?>... argumentTypes) {
		final Collection<Constructor<?>> members = findAllAndMakeThemAccessible(targetClass, argumentTypes);
		if (members.size() == 1) {
			return members.iterator().next();
		} else if (members.size() > 1) {
			final Collection<Constructor<?>> membersThatMatch = searchForExactMatch(members, argumentTypes);
			if (membersThatMatch.size() == 1) {
				return membersThatMatch.iterator().next();
			}
			Throwables.INSTANCE.throwException(
				"Found more than one of constructor with argument types {} in {} class",
				Strings.INSTANCE.join(", ", argumentTypes, new Function<Class<?>, String>() {
					@Override
					public String apply(final Class<?> cls) {
						return cls.getName();
					}
				}),
				targetClass.getName()
			);
		}
		return null;
	}

	public <T> T newInstanceOf(
		final Class<?> targetClass,
		final Object... arguments
	) {
		return Handler.getFirst(
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					final Class<?>[] argsType = Classes.INSTANCE.retrieveFrom(arguments);
					final Members.Handler.OfExecutable.Box<Constructor<?>> methodHandleBox = findDirectHandleBox(targetClass, argsType);
					return Handler.get(new ThrowingSupplier<T, Throwable>() {
						@Override
						public T get() throws Throwable {
								final Constructor<?> ctor = methodHandleBox.getExecutable();
								return (T)methodHandleBox.getHandler().invokeWithArguments(
									getFlatArgumentList(
										ctor, new Supplier<List<Object>>() {
											@Override
											public List<Object> get() {
												return new ArrayList<>();
											}
										},
										arguments
									)
								);
							}
						}
					);
				}
			}, new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					final Constructor<?> ctor = findFirstAndMakeItAccessible(targetClass, Classes.INSTANCE.retrieveFrom(arguments));
					if (ctor == null) {
						Throwables.INSTANCE.throwException("Constructor not found in {}", targetClass.getName());
					}
					return (T)Facade.INSTANCE.newInstance(
						ctor,
						getArgumentArray(
							ctor,
							new ThrowingTriFunction<Constructor<?>, Supplier<List<Object>>, Object[], List<Object>, Throwable>() {
								@Override
								public List<Object> apply(final Constructor<?> member, final Supplier<List<Object>> collector, final Object[] arguments) throws Throwable {
									return Constructors.this.getArgumentListWithArrayForVarArgs(
										member,
										collector,
										arguments
									);
								}
							},
							new Supplier<List<Object>>() {
								@Override
								public List<Object> get() {
									return new ArrayList<>();
								}
							},
							arguments
						)
					);
				}
			}
		);

	}

	@Override
	MethodHandle retrieveMethodHandle(final MethodHandles.Lookup consulter, final Constructor<?> constructor) throws NoSuchMethodException, IllegalAccessException {
		return consulter.findConstructor(
			constructor.getDeclaringClass(),
			MethodType.methodType(void.class, constructor.getParameterTypes())
		);
	}

	String retrieveNameForCaching(final Class<?> cls) {
		return Classes.INSTANCE.retrieveSimpleName(cls.getName());
	}

	@Override
	String retrieveNameForCaching(final Constructor<?> constructor) {
		return retrieveNameForCaching(constructor.getDeclaringClass());
	}

	private Members.Handler.OfExecutable.Box<Constructor<?>> findDirectHandleBox(final Class<?> targetClass, final Class<?>... inputParameterTypesOrSubTypes) {
		final String nameForCaching = retrieveNameForCaching(targetClass);
		final String cacheKey = getCacheKey(targetClass, "equals " + nameForCaching, inputParameterTypesOrSubTypes);
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Members.Handler.OfExecutable.Box<Constructor<?>> entry =
			(Box<Constructor<?>>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.get(targetClassClassLoader, cacheKey);
		if (entry == null) {
			final Constructor<?> ctor = findFirstAndMakeItAccessible(targetClass, inputParameterTypesOrSubTypes);
			entry = findDirectHandleBox(
				ctor, targetClassClassLoader, cacheKey
			);
		}
		return entry;
	}
}

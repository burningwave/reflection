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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Consumer;
import org.burningwave.function.Function;
import org.burningwave.function.Handler;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingFunction;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingSupplier;
import org.burningwave.function.ThrowingTriFunction;
import org.burningwave.function.ThrowingTriPredicate;

@SuppressWarnings("unchecked")
public class Methods extends Members.Handler.OfExecutable<Method, MethodCriteria> {

	public final static Methods INSTANCE;

	static {
		INSTANCE = new Methods();
	}

	private Methods(){}

	public Collection<Method> findAllAndMakeThemAccessible(
		final Class<?> targetClass
	) {
		final String cacheKey = getCacheKey(targetClass, "all methods");
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		final Collection<Method> members = Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(
			targetClassClassLoader, cacheKey, new Supplier<Collection<Method>>() {
				@Override
				public Collection<Method> get() {
					return findAllAndMakeThemAccessible(
						MethodCriteria.forEntireClassHierarchy(), targetClass
					);
				}
			}
		);
		return members;
	}

	public Collection<Method> findAllByExactNameAndMakeThemAccessible(
		final Class<?> targetClass,
		final String methodName,
		final Class<?>... inputParameterTypesOrSubTypes
	) {
		return findAllByNamePredicateAndMakeThemAccessible(
			targetClass,
			"equals " + methodName,
			new ThrowingPredicate<String, Throwable>() {
				@Override
				public boolean test(final String name) throws Throwable {
					return methodName.equals(name);
				}
			},
			inputParameterTypesOrSubTypes
		);
	}

	public Collection<Method> findAllByMatchedNameAndMakeThemAccessible(
		final Class<?> targetClass,
		final String regEx,
		final Class<?>... inputParameterTypesOrSubTypes
	) {
		return findAllByNamePredicateAndMakeThemAccessible(
			targetClass, "match " + regEx,
			new ThrowingPredicate<String, Throwable>() {
				@Override
				public boolean test(final String name) throws Throwable {
					return name.matches(regEx);
				}
			},
			inputParameterTypesOrSubTypes
		);
	}

	public MethodHandle findDirectHandle(final Class<?> targetClass, final String methodName, final Class<?>... inputParameterTypesOrSubTypes) {
		return findDirectHandleBox(targetClass, methodName, inputParameterTypesOrSubTypes).getHandler();
	}

	public Method findFirstAndMakeItAccessible(final Class<?> targetClass, final String memberName, final Class<?>... inputParameterTypesOrSubTypes) {
		final Collection<Method> members = findAllByExactNameAndMakeThemAccessible(targetClass, memberName, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.iterator().next();
		} else if (members.size() > 1) {
			final Collection<Method> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (!membersThatMatch.isEmpty()) {
				return membersThatMatch.iterator().next();
			}
			return members.iterator().next();
		}
		return null;
	}


	public Method findOneAndMakeItAccessible(final Class<?> targetClass, final String memberName, final Class<?>... inputParameterTypesOrSubTypes) {
		final Collection<Method> members = findAllByExactNameAndMakeThemAccessible(targetClass, memberName, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.iterator().next();
		} else if (members.size() > 1) {
			final Collection<Method> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (membersThatMatch.size() == 1) {
				return membersThatMatch.iterator().next();
			}
			Throwables.INSTANCE.throwException(
				new IllegalArgumentException(
					Strings.INSTANCE.compile(
						"Found more than one of method named {} with argument types {} in {} hierarchy",
						memberName,
						Strings.INSTANCE.join(", ", inputParameterTypesOrSubTypes, new Function<Class<?>, String>() {
							@Override
							public String apply(final Class<?> cls) {
								return cls.getName();
							}
						}),
						targetClass.getName()
					)
				)
			);
		}
		return null;
	}

	public <T> T invoke(final Object target, final Method method, final Object... params) {
		return Facade.INSTANCE.invoke(target, method, params);
	}

	public <T> T invoke(final Object target, final String methodName, final Object... arguments) {
		return Handler.getFirst(new ThrowingSupplier<T, RuntimeException>() {
			@Override
			public T get() throws RuntimeException {
				return (T)invokeDirect(
					Classes.INSTANCE.retrieveFrom(target),
					target, methodName, new Supplier<List<Object>>() {
						@Override
						public List<Object> get() {
							final List<Object> argumentList = new ArrayList<>();
							argumentList.add(target);
							return argumentList;
						}
					},
					arguments
				);
			}
		}, new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					return invoke(
						Classes.INSTANCE.retrieveFrom(target),
						null, methodName, new ThrowingFunction<Method, T, Throwable>() {
							@Override
							public T apply(final Method method) throws Throwable {
								return invoke(
									target,
									method,
									getArgumentArray(
										method,
										buildArgumentListSupplier(),
										buildNewListSupplier()
									),
									arguments
								);
							}
						},
						arguments
					);
				}
			}
		);
	}

	public 	<T> T invokeStatic(final Class<?> targetClass, final String methodName, final Object... arguments) {
		return Handler.getFirst(
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					return (T)invokeDirect(
						targetClass,
						null,
						methodName,
						buildNewListSupplier(),
						arguments
					);
				}
			},
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					return invoke(
						targetClass, null, methodName, new ThrowingFunction<Method, T, Throwable>() {
							@Override
							public T apply(final Method method) throws Throwable {
								return (T)invoke(null,
									method,
									getArgumentArray(
										method,
										buildArgumentListSupplier(),
										buildNewListSupplier(),
										arguments
									)
								);
							}
						},
						arguments
					);
				}
			}
		);
	}

	private ThrowingTriFunction<Method, Supplier<List<Object>>, Object[], List<Object>, Throwable> buildArgumentListSupplier() {
		return new ThrowingTriFunction<Method, Supplier<List<Object>>, Object[], List<Object>, Throwable>() {
			@Override
			public List<Object> apply(final Method member, final Supplier<List<Object>> collector, final Object[] arguments) throws Throwable {
				return getArgumentListWithArrayForVarArgs(member, collector, arguments);
			}
		};
	}

	private Supplier<List<Object>> buildNewListSupplier() {
		return new Supplier<List<Object>>() {
			@Override
			public List<Object> get() {
				return new ArrayList<>();
			}
		};
	}

	String createGetterMethodNameByFieldPath(final String fieldPath) {
		final String methodName =
			"get" + Strings.INSTANCE.capitalizeFirstCharacter(fieldPath);
		return methodName;
	}

	String createSetterMethodNameByFieldPath(final String fieldPath) {
		final String methodName =
			"set" + Strings.INSTANCE.capitalizeFirstCharacter(fieldPath);
		return methodName;
	}

	@Override
	MethodHandle retrieveMethodHandle(final MethodHandles.Lookup consulter, final Method method) throws java.lang.NoSuchMethodException, IllegalAccessException {
		final Class<?> methodDeclaringClass = method.getDeclaringClass();
		return !Modifier.isStatic(method.getModifiers())?
			consulter.findSpecial(
				methodDeclaringClass, retrieveNameForCaching(method),
				MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
				methodDeclaringClass
			):
			consulter.findStatic(
				methodDeclaringClass, retrieveNameForCaching(method),
				MethodType.methodType(method.getReturnType(), method.getParameterTypes())
			);
	}

	@Override
	String retrieveNameForCaching(final Method method) {
		return method.getName();
	}

	private Collection<Method> findAllByNamePredicateAndMakeThemAccessible(
		final Class<?> targetClass,
		final String cacheKeyPrefix,
		final ThrowingPredicate<String, ? extends Throwable> namePredicate,
		final Class<?>... inputParameterTypesOrSubTypes
	) {
		final String cacheKey = getCacheKey(targetClass, cacheKeyPrefix, inputParameterTypesOrSubTypes);
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Method>>() {
			@Override
			public Collection<Method> get() {
				MethodCriteria criteria = MethodCriteria.forEntireClassHierarchy()
					.name(namePredicate)
					.and().parameterTypesAreAssignableFrom(inputParameterTypesOrSubTypes);
				if ((inputParameterTypesOrSubTypes != null) && (inputParameterTypesOrSubTypes.length == 0)) {
					criteria = criteria.or(MethodCriteria.forEntireClassHierarchy().name(namePredicate).and().parameter(
						new ThrowingTriPredicate<Class<?>[], Boolean, Integer, Throwable>() {
						@Override
						public boolean test(final Class<?>[] parameters, Boolean isVarArgs, final Integer idx) {
							return (parameters.length == 1) && isVarArgs;
						}
					}));
				}
				final MethodCriteria finalCriteria = criteria;
				return Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Method>>() {
					@Override
					public Collection<Method> get() {
						return findAllAndApply(
								finalCriteria, targetClass, new Consumer<Method>() {
									@Override
									public void accept(final Method member) {
									setAccessible(member, true);
}
								}
						);
					}
				}
				);
			}
		});
	}

	private Members.Handler.OfExecutable.Box<Method> findDirectHandleBox(final Class<?> targetClass, final String methodName, final Class<?>... inputParameterTypesOrSubTypes) {
		final String cacheKey = getCacheKey(targetClass, "equals " + methodName, inputParameterTypesOrSubTypes);
		final ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Members.Handler.OfExecutable.Box<Method> entry =
			(Box<Method>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.get(targetClassClassLoader, cacheKey);
		if (entry == null) {
			final Method method = findFirstAndMakeItAccessible(targetClass, methodName, inputParameterTypesOrSubTypes);
			if (method == null) {
				Throwables.INSTANCE.throwException(
					new NoSuchMethodException(
						Strings.INSTANCE.compile(
							"Method {} not found in {} hierarchy", methodName, targetClass.getName()
						)
					)
				);
			}
			entry = findDirectHandleBox(
				method, targetClassClassLoader, cacheKey
			);
		}
		return entry;
	}

	private <T> T invoke(final Class<?> targetClass, final Object target, final String methodName, final ThrowingFunction<Method, T, Throwable> methodInvoker, final Object... arguments) {
		return Handler.get(new ThrowingSupplier<T, Throwable>() {
			@Override
			public T get() throws Throwable {
				final Method method = findFirstAndMakeItAccessible(targetClass, methodName, Classes.INSTANCE.retrieveFrom(arguments));
				if (method == null) {
					Throwables.INSTANCE.throwException(
						new NoSuchMethodException(
							Strings.INSTANCE.compile(
								"Method {} not found in {} hierarchy", methodName, targetClass.getName()
							)
						)
					);
				}
				return methodInvoker.apply(method);
			}
		});
	}

	private <T> T invokeDirect(final Class<?> targetClass, final Object target, final String methodName, final Supplier<List<Object>> listSupplier,  final Object... arguments) {
		final Class<?>[] argsType = Classes.INSTANCE.retrieveFrom(arguments);
		final Members.Handler.OfExecutable.Box<Method> methodHandleBox = findDirectHandleBox(targetClass, methodName, argsType);
		return Handler.get(new ThrowingSupplier<T, Throwable>() {
			@Override
			public T get() throws Throwable {
					final Method method = methodHandleBox.getExecutable();
					final List<Object> argumentList = getFlatArgumentList(method, listSupplier, arguments);
					return (T)methodHandleBox.getHandler().invokeWithArguments(argumentList);
				}
		}
		);
	}

	public static class NoSuchMethodException extends RuntimeException {

		private static final long serialVersionUID = -2912826056405333039L;

		public NoSuchMethodException(final String message) {
			super(message);
		}

	}

}

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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.burningwave.Strings;
import org.burningwave.Throwables;
import org.burningwave.function.Consumer;
import org.burningwave.function.Executor;
import org.burningwave.function.Function;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingFunction;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingSupplier;

@SuppressWarnings("unchecked")
public class Methods extends Members.Handler.OfExecutable<Method, MethodCriteria> {

	public final static Methods INSTANCE;

	static {
		INSTANCE = new Methods();
	}

	private Methods(){}

	public Collection<Method> findAllAndMakeThemAccessible(
		Class<?> targetClass
	) {
		String cacheKey = getCacheKey(targetClass, "all methods");
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Collection<Method> members = Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(
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
		Class<?> targetClass,
		String methodName,
		Class<?>... inputParameterTypesOrSubTypes
	) {
		return findAllByNamePredicateAndMakeThemAccessible(targetClass, "equals " + methodName, methodName::equals, inputParameterTypesOrSubTypes);
	}

	public Collection<Method> findAllByMatchedNameAndMakeThemAccessible(
		Class<?> targetClass,
		String methodName,
		Class<?>... inputParameterTypesOrSubTypes
	) {
		return findAllByNamePredicateAndMakeThemAccessible(targetClass, "match " + methodName, methodName::matches, inputParameterTypesOrSubTypes);
	}

	public MethodHandle findDirectHandle(Class<?> targetClass, String methodName, Class<?>... inputParameterTypesOrSubTypes) {
		return findDirectHandleBox(targetClass, methodName, inputParameterTypesOrSubTypes).getHandler();
	}

	public Method findFirstAndMakeItAccessible(Class<?> targetClass, String memberName, Class<?>... inputParameterTypesOrSubTypes) {
		Collection<Method> members = findAllByExactNameAndMakeThemAccessible(targetClass, memberName, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.stream().findFirst().get();
		} else if (members.size() > 1) {
			Collection<Method> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (!membersThatMatch.isEmpty()) {
				return membersThatMatch.stream().findFirst().get();
			}
			return members.stream().findFirst().get();
		}
		return null;
	}


	public Method findOneAndMakeItAccessible(Class<?> targetClass, String memberName, Class<?>... inputParameterTypesOrSubTypes) {
		Collection<Method> members = findAllByExactNameAndMakeThemAccessible(targetClass, memberName, inputParameterTypesOrSubTypes);
		if (members.size() == 1) {
			return members.stream().findFirst().get();
		} else if (members.size() > 1) {
			Collection<Method> membersThatMatch = searchForExactMatch(members, inputParameterTypesOrSubTypes);
			if (membersThatMatch.size() == 1) {
				return membersThatMatch.stream().findFirst().get();
			}
			Throwables.INSTANCE.throwException(
				new IllegalArgumentException(
					Strings.INSTANCE.compile(
						"Found more than one of method named {} with argument types {} in {} hierarchy",
						memberName,
						Strings.INSTANCE.join(", ", inputParameterTypesOrSubTypes, new Function<Class<?>, String>() {
							@Override
							public String apply(Class<?> cls) {
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

	public <T> T invoke(Object target, Method method, Object... params) {
		return Facade.INSTANCE.invoke(target, method, params);
	}

	public <T> T invoke(Object target, String methodName, Object... arguments) {
		return Executor.getFirst(new ThrowingSupplier<T, RuntimeException>() {
			@Override
			public T get() throws RuntimeException {
				return (T)invokeDirect(
					Classes.INSTANCE.retrieveFrom(target),
					target, methodName, new Supplier<List<Object>>() {
						@Override
						public List<Object> get() {
							List<Object> argumentList = new ArrayList<>();
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
							public T apply(Method method) throws Throwable {
								return invoke(
									target, method, getArgumentArray(
									method,
									Methods.this::getArgumentListWithArrayForVarArgs,
									ArrayList::new,
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

	public 	<T> T invokeStatic(Class<?> targetClass, String methodName, Object... arguments) {
		return Executor.getFirst(
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					return (T)invokeDirect(targetClass, null, methodName, ArrayList::new, arguments);
				}
			},
			new ThrowingSupplier<T, RuntimeException>() {
				@Override
				public T get() throws RuntimeException {
					return invoke(
						targetClass, null, methodName, new ThrowingFunction<Method, T, Throwable>() {
							@Override
							public T apply(Method method) throws Throwable {
								return (T)invoke(null,
									method,
									getArgumentArray(
										method,
										Methods.this::getArgumentListWithArrayForVarArgs,
										ArrayList::new,
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


	String createGetterMethodNameByFieldPath(String fieldPath) {
		String methodName =
			"get" + Strings.INSTANCE.capitalizeFirstCharacter(fieldPath);
		return methodName;
	}

	String createSetterMethodNameByFieldPath(String fieldPath) {
		String methodName =
			"set" + Strings.INSTANCE.capitalizeFirstCharacter(fieldPath);
		return methodName;
	}

	@Override
	MethodHandle retrieveMethodHandle(MethodHandles.Lookup consulter, Method method) throws java.lang.NoSuchMethodException, IllegalAccessException {
		Class<?> methodDeclaringClass = method.getDeclaringClass();
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
	String retrieveNameForCaching(Method method) {
		return method.getName();
	}

	private Collection<Method> findAllByNamePredicateAndMakeThemAccessible(
		Class<?> targetClass,
		String cacheKeyPrefix,
		ThrowingPredicate<String, ? extends Throwable> namePredicate,
		Class<?>... inputParameterTypesOrSubTypes
	) {
		String cacheKey = getCacheKey(targetClass, cacheKeyPrefix, inputParameterTypesOrSubTypes);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Method>>() {
			@Override
			public Collection<Method> get() {
				MethodCriteria criteria = MethodCriteria.forEntireClassHierarchy()
					.name(namePredicate)
					.and().parameterTypesAreAssignableFrom(inputParameterTypesOrSubTypes);
				if ((inputParameterTypesOrSubTypes != null) && (inputParameterTypesOrSubTypes.length == 0)) {
					criteria = criteria.or(MethodCriteria.forEntireClassHierarchy().name(namePredicate).and().parameter(new ThrowingBiPredicate<Parameter[], Integer, Throwable>() {
						@Override
						public boolean test(Parameter[] parameters, Integer idx) {
							return (parameters.length == 1) && parameters[0].isVarArgs();
						}
					}));
				}
				MethodCriteria finalCriteria = criteria;
				return Cache.INSTANCE.uniqueKeyForMethods.getOrUploadIfAbsent(targetClassClassLoader, cacheKey, new Supplier<Collection<Method>>() {
					@Override
					public Collection<Method> get() {
						return findAllAndApply(
								finalCriteria, targetClass, new Consumer<Method>() {
									@Override
									public void accept(Method member) {
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

	private Members.Handler.OfExecutable.Box<Method> findDirectHandleBox(Class<?> targetClass, String methodName, Class<?>... inputParameterTypesOrSubTypes) {
		String cacheKey = getCacheKey(targetClass, "equals " + methodName, inputParameterTypesOrSubTypes);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		Members.Handler.OfExecutable.Box<Method> entry =
			(Box<Method>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.get(targetClassClassLoader, cacheKey);
		if (entry == null) {
			Method method = findFirstAndMakeItAccessible(targetClass, methodName, inputParameterTypesOrSubTypes);
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

	private <T> T invoke(Class<?> targetClass, Object target, String methodName, ThrowingFunction<Method, T, Throwable> methodInvoker, Object... arguments) {
		return Executor.get(new ThrowingSupplier<T, Throwable>() {
			@Override
			public T get() throws Throwable {
				Method method = findFirstAndMakeItAccessible(targetClass, methodName, Classes.INSTANCE.retrieveFrom(arguments));
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

	private <T> T invokeDirect(Class<?> targetClass, Object target, String methodName, Supplier<List<Object>> listSupplier,  Object... arguments) {
		Class<?>[] argsType = Classes.INSTANCE.retrieveFrom(arguments);
		Members.Handler.OfExecutable.Box<Method> methodHandleBox = findDirectHandleBox(targetClass, methodName, argsType);
		return Executor.get(new ThrowingSupplier<T, Throwable>() {
			@Override
			public T get() throws Throwable {
					Method method = methodHandleBox.getExecutable();
					List<Object> argumentList = getFlatArgumentList(method, listSupplier, arguments);
					return (T)methodHandleBox.getHandler().invokeWithArguments(argumentList);
				}
		}
		);
	}

	public static class NoSuchMethodException extends RuntimeException {

		private static final long serialVersionUID = -2912826056405333039L;

		public NoSuchMethodException(String message) {
			super(message);
		}

	}

}

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
 * Copyright (c) 2022-2023 Roberto Gentili
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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.burningwave.Throwables;
import org.burningwave.function.Consumer;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingFunction;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingTriFunction;

import io.github.toolfactory.jvm.function.template.ThrowingBiFunction;

@SuppressWarnings("unchecked")
public class Members {
	public static final Members INSTANCE;
	static final String ALL_FOR_CLASS = "all for class";

	static {
		INSTANCE = new Members();
	}

	private Members() {}

	public <M extends Member> Collection<M> findAll(final MemberCriteria<M, ?, ?> criteria, final Class<?> classFrom) {
		final Collection<M> result = findAll(
			classFrom,
			classFrom,
			criteria.getScanUpToPredicate(),
			criteria.getMembersSupplier(),
			criteria.getPredicateOrTruePredicateIfPredicateIsNull(),
			new HashSet<Class<?>>(),
			new LinkedHashSet<M>()
		);
		final ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate = criteria.getResultPredicate();
		try {
			return resultPredicate == null?
					result :
					resultPredicate.test(result)?
						result :
						new LinkedHashSet<M>();
		} catch (final Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	public <M extends Member> M findFirst(final MemberCriteria<M, ?, ?> criteria, final Class<?> classFrom) {
		final ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate = criteria.getResultPredicate();
		if (resultPredicate == null) {
			return findFirst(
				classFrom,
				classFrom,
				criteria.getScanUpToPredicate(),
				criteria.getMembersSupplier(),
				criteria.getPredicateOrTruePredicateIfPredicateIsNull(),
				new HashSet<Class<?>>()
			);
		} else {
			final Collection<M> result = findAll(
				classFrom,
				classFrom,
				criteria.getScanUpToPredicate(),
				criteria.getMembersSupplier(),
				criteria.getPredicateOrTruePredicateIfPredicateIsNull(),
				new HashSet<Class<?>>(),
				new LinkedHashSet<M>()
			);
			try {
				if (resultPredicate.test(result)) {
					return result.iterator().next();
				}
				return null;
			} catch (final Throwable exc) {
				return Throwables.INSTANCE.throwException(exc);
			}
		}
	}

	public <M extends Member> M findOne(final MemberCriteria<M, ?, ?> criteria, final Class<?> classFrom) {
		final Collection<M> members = findAll(criteria, classFrom);
		if (members.size() > 1) {
			Throwables.INSTANCE.throwException("More than one member found for class {}", classFrom.getName());
		}
		if (!members.isEmpty()) {
			return members.iterator().next();
		}
		return null;
	}

	public <M extends Member> boolean match(final MemberCriteria<M, ?, ?> criteria, final Class<?> classFrom) {
		return findFirst(criteria, classFrom) != null;
	}

	private <M extends Member> Collection<M> findAll(
		final Class<?> initialClsFrom,
		final Class<?> currentScannedClass,
		final ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> clsPredicate,
		final ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable> memberSupplier,
		final ThrowingPredicate<M, ? extends Throwable> predicate,
		final Set<Class<?>> visitedInterfaces,
		Collection<M> collection
	) {
		try {
			for (final M member : memberSupplier.apply(initialClsFrom, currentScannedClass)) {
				if (predicate.test(member)) {
					collection.add(member);
				}
			}
			for (final Class<?> interf : currentScannedClass.getInterfaces()) {
				if (!visitedInterfaces.add(interf)) {
					continue;
				}
				collection = findAll((Class<?>) initialClsFrom, interf, clsPredicate, memberSupplier, predicate, visitedInterfaces, collection);
				if (!(collection instanceof Set)) {
					return collection;
				}
				if (clsPredicate.test(initialClsFrom, currentScannedClass)) {
					return Collections.unmodifiableCollection(collection);
				}
			}
			Class<?> superClass = currentScannedClass.getSuperclass();
			if (!(collection instanceof Set) || (((superClass = currentScannedClass.getSuperclass()) == null) && currentScannedClass.isInterface())) {
				return collection;
			}
			if ((superClass == null) || clsPredicate.test(initialClsFrom, currentScannedClass)) {
				return Collections.unmodifiableCollection(collection);
			}
			return findAll(
				initialClsFrom,
				superClass,
				clsPredicate,
				memberSupplier,
				predicate,
				visitedInterfaces,
				collection
			);
		} catch (final Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	private <M extends Member> M findFirst(
		final Class<?> initialClsFrom,
		final Class<?> currentScannedClass,
		final ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> clsPredicate,
		final ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable>
		memberSupplier, final ThrowingPredicate<M, ? extends Throwable> predicate,
		final Set<Class<?>> visitedInterfaces
	) {
		try {
			for (final M member : memberSupplier.apply(initialClsFrom, currentScannedClass)) {
				if (predicate.test(member)) {
					return member;
				}
			}
			for (final Class<?> interf : currentScannedClass.getInterfaces()) {
				if (!visitedInterfaces.add(interf)) {
					continue;
				}
				final M member = findFirst(initialClsFrom, interf, clsPredicate, memberSupplier, predicate, visitedInterfaces);
				if ((member != null) || clsPredicate.test(initialClsFrom, currentScannedClass)) {
					return member;
				}
			}
			return
				(clsPredicate.test(initialClsFrom, currentScannedClass) || (currentScannedClass.getSuperclass() == null)) ?
					null :
					findFirst(initialClsFrom, currentScannedClass.getSuperclass(), clsPredicate, memberSupplier, predicate, visitedInterfaces);
		} catch (final Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	public static abstract class Handler<M extends Member, C extends MemberCriteria<M, C, ?>> {

		public static abstract class OfExecutable<E extends Member, C extends ExecutableMemberCriteria<E, C, ?>> extends Members.Handler<E, C> {

			OfExecutable() {}

			abstract Class<?>[] getParameterTypes(Member member);

			abstract boolean isVarArgs(Member member);

			abstract MethodHandle retrieveMethodHandle(MethodHandles.Lookup consulter, E member) throws NoSuchMethodException, IllegalAccessException;

			abstract String retrieveNameForCaching(E member);

			public Collection<MethodHandle> findAllDirectHandle(final C criteria, final Class<?> clsFrom) {
				final Collection<MethodHandle> methodHandles = new LinkedHashSet<>();
				for (final E member : findAll(criteria, clsFrom)) {
					methodHandles.add(findDirectHandle(member));
				}
				return methodHandles;
			}

			public MethodHandle findDirectHandle(final E executable) {
				return findDirectHandleBox(executable).getHandler();
			}

			public MethodHandle findFirstDirectHandle(final C criteria, final Class<?> clsFrom) {
				final E found = findFirst(criteria, clsFrom);
				if (found != null) {
					return findDirectHandle(found);
				}
				return null;
			}


			public MethodHandle findOneDirectHandle(final C criteria, final Class<?> clsFrom) {
				final E found = findOne(criteria, clsFrom);
				if (found != null) {
					return findDirectHandle(found);
				}
				return null;
			}

			Members.Handler.OfExecutable.Box<E> findDirectHandleBox(final E executable) {
				final Class<?> targetClass = executable.getDeclaringClass();
				final String cacheKey = getCacheKey(targetClass, "equals " + retrieveNameForCaching(executable), getParameterTypes(executable));
				return findDirectHandleBox(executable, cacheKey);
			}

			Members.Handler.OfExecutable.Box<E> checkAndGetExecutableBox(Members.Handler.OfExecutable.Box<E> executableBox) {
				if (executableBox.getHandler() != null) {
					return executableBox;
				}
				return Throwables.INSTANCE.throwException(executableBox.getException());
			}

			Members.Handler.OfExecutable.Box<E> findDirectHandleBox(final E executable, final String cacheKey) {
				return checkAndGetExecutableBox(
					(Box<E>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.getOrUploadIfAbsent(cacheKey, new Supplier<Box<?>>() {
						@Override
						public Box<?> get() {
							final Class<?> methodDeclaringClass = executable.getDeclaringClass();
							final Collection<Members.Handler.OfExecutable.Box<E>> executableBoxes = new ArrayList<>();
							try {
								return (Members.Handler.OfExecutable.Box<E>)Facade.INSTANCE.executeWithConsulter(
									methodDeclaringClass,
									new ThrowingFunction<Lookup, Box<E>, Throwable>() {
										@Override
										public Box<E> apply(final Lookup consulter) throws Throwable {
											Throwable exception = null;
											MethodHandle methodHandle = null;
											try {
												methodHandle = retrieveMethodHandle(consulter, executable);
											} catch (Throwable exc) {
												exception = exc;
											}
											Members.Handler.OfExecutable.Box<E> executableBox = new Members.Handler.OfExecutable.Box<>(consulter,
												executable,
												methodHandle,
												exception
											);
											executableBoxes.add(
												executableBox
											);
											if (exception != null) {
												throw exception;
											}
											return executableBox;
										}
									}
								).getValue();
							} catch (Throwable exception) {
								return executableBoxes.iterator().next();
							}
						}
					})
				);
			}

			Object[] getArgumentArray(
				final E member,
				final ThrowingTriFunction<E, Supplier<List<Object>>, Object[], List<Object>, ? extends Throwable> argumentListSupplier,
				final Supplier<List<Object>> listSupplier,
				final Object... arguments
			) {
				List<Object> argumentList;
				try {
					argumentList = argumentListSupplier.apply(member, listSupplier, arguments != null ? arguments : null);
					return argumentList.toArray(new Object[argumentList.size()]);
				} catch (final Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}

			List<Object> getArgumentListWithArrayForVarArgs(final E member, final Supplier<List<Object>> argumentListSupplier, final Object... arguments) {
				final Class<?>[] parameters = getParameterTypes(member);
				final List<Object> argumentList = argumentListSupplier.get();
				if (arguments != null) {
					if ((parameters.length > 0) && isVarArgs(member)) {
						for (int i = 0; (i < arguments.length) && (i < (parameters.length - 1)); i++) {
							argumentList.add(arguments[i]);
						}
						final Class<?> lastParameter = parameters[parameters.length -1];
						if (arguments.length == parameters.length) {
							final Object lastArgument = arguments[arguments.length -1];
							if ((lastArgument != null) &&
								lastArgument.getClass().isArray() &&
								lastArgument.getClass().equals(lastParameter)) {
								argumentList.add(lastArgument);
							} else {
								final Object array = Array.newInstance(lastParameter.getComponentType(), 1);
								Array.set(array, 0, lastArgument);
								argumentList.add(array);
							}
						} else if (arguments.length > parameters.length) {
							final Object array = Array.newInstance(lastParameter.getComponentType(), arguments.length - (parameters.length - 1));
							for (int i = parameters.length - 1, j = 0; i < arguments.length; i++, j++) {
								Array.set(array, j, arguments[i]);
							}
							argumentList.add(array);
						} else if (arguments.length < parameters.length) {
							argumentList.add(Array.newInstance(lastParameter.getComponentType(),0));
						}
					} else if (arguments.length > 0) {
						for (final Object argument : arguments) {
							argumentList.add(argument);
						}
					}
				} else {
					argumentList.add(null);
				}
				return argumentList;
			}

			List<Object> getFlatArgumentList(final E member, final Supplier<List<Object>> argumentListSupplier, final Object... arguments) {
				final Class<?>[] parameters = getParameterTypes(member);
				final List<Object> argumentList = argumentListSupplier.get();
				if (arguments != null) {
					if ((parameters.length > 0) && isVarArgs(member)) {
						for (int i = 0; (i < arguments.length) && (i < (parameters.length - 1)); i++) {
							argumentList.add(arguments[i]);
						}
						if (arguments.length == parameters.length) {
							final Class<?> lastParameter = parameters[parameters.length -1];
							final Object lastArgument = arguments[arguments.length -1];
							if ((lastArgument != null) &&
								lastArgument.getClass().isArray() &&
								lastArgument.getClass().equals(lastParameter)) {
								for (int i = 0; i < Array.getLength(lastArgument); i++) {
									argumentList.add(Array.get(lastArgument, i));
								}
							} else {
								argumentList.add(lastArgument);
							}
						} else if (arguments.length > parameters.length) {
							for (int i = parameters.length - 1; i < arguments.length; i++) {
								argumentList.add(arguments[i]);
							}
						} else if (arguments.length < parameters.length) {
							argumentList.add(null);
						}
					} else if (arguments.length > 0) {
						for (final Object argument : arguments) {
							argumentList.add(argument);
						}
					}
				} else {
					argumentList.add(null);
				}
				return argumentList;
			}

			static Class<?>[] retrieveParameterTypes(
				final Member member,
				final List<Class<?>> argumentsClassesAsList,
				Class<?>[] parameterTypes,
				boolean isVarArgs
			) {
				final Class<?>[] memberParameter = parameterTypes;
				Class<?>[] memberParameterTypes = parameterTypes.clone();
				if ((memberParameter.length > 0) && isVarArgs) {
					final Class<?> varArgsType =
						(argumentsClassesAsList.size() > 0) &&
						(argumentsClassesAsList.get(argumentsClassesAsList.size()-1) != null) &&
						argumentsClassesAsList.get(argumentsClassesAsList.size()-1).isArray()?
						memberParameter[memberParameter.length - 1]:
						memberParameter[memberParameter.length - 1].getComponentType();
					if (memberParameter.length == 1) {
						memberParameterTypes = new Class<?>[argumentsClassesAsList.size()];
						for (int j = 0; j < memberParameterTypes.length; j++) {
							memberParameterTypes[j] = varArgsType;
						}
					} else if ((memberParameter.length - 1) <= argumentsClassesAsList.size()) {
						memberParameterTypes = new Class<?>[argumentsClassesAsList.size()];
						for (int j = 0; j < memberParameterTypes.length; j++) {
							if (j < (memberParameter.length - 1)) {
								memberParameterTypes[j] = memberParameter[j];
							} else {
								memberParameterTypes[j] = varArgsType;
							}
						}
					}
				}
				return memberParameterTypes;
			}

			Collection<E> searchForExactMatch(final Collection<E> members, final Class<?>... arguments) {
				final List<Class<?>> argumentsClassesAsList = Arrays.asList(arguments);
				//Collection<E> membersThatMatch = new LinkedHashSet<>();
				final Collection<E> membersThatMatch = new TreeSet<>(new Comparator<E>() {
					@Override
					public int compare(final E executableOne, final E executableTwo) {
						final Class<?>[] executableOneParameters = getParameterTypes(executableOne);
						final Class<?>[] executableTwoParameters = getParameterTypes(executableTwo);
						if (executableOneParameters.length == argumentsClassesAsList.size()) {
							if (executableTwoParameters.length == argumentsClassesAsList.size()) {
								if ((executableOneParameters.length > 0) && isVarArgs(executableOne)) {
									if ((executableTwoParameters.length > 0) && isVarArgs(executableTwo)) {
										return 0;
									}
									return 1;
								} else if ((executableTwoParameters.length > 0) && isVarArgs(executableTwo)) {
									return -1;
								} else {
									return 0;
								}
							}
							return -1;
						} else if (executableTwoParameters.length == argumentsClassesAsList.size()) {
							return 1;
						}
						return 0;
					}

				});

				for (final E executable : members) {
					final Class<?>[] parameterTypes = retrieveParameterTypes(
						executable,
						argumentsClassesAsList,
						getParameterTypes(executable),
						isVarArgs(executable)
					);
					boolean exactMatch = true;
					for (int i = 0; i < parameterTypes.length; i++) {
						if ((argumentsClassesAsList.get(i) != null) &&
							!Classes.INSTANCE.getClassOrWrapper(argumentsClassesAsList.get(i)).equals(Classes.INSTANCE.getClassOrWrapper(parameterTypes[i]))
						) {
							exactMatch = false;
						}
					}
					if (exactMatch) {
						membersThatMatch.add(executable);
					}
				}
				return membersThatMatch;
			}

			public static class Box<E extends Member> {
				MethodHandles.Lookup consulter;
				E executable;
				MethodHandle handler;
				Throwable exception;

				Box(final MethodHandles.Lookup consulter, final E executable, final MethodHandle handler, final Throwable exception) {
					super();
					this.consulter = consulter;
					this.executable = executable;
					this.handler = handler;
					this.exception = exception;
				}

				public MethodHandles.Lookup getConsulter() {
					return consulter;
				}

				public E getExecutable() {
					return executable;
				}

				public MethodHandle getHandler() {
					return handler;
				}

				public Throwable getException() {
					return exception;
				}

			}

		}

		public Collection<M> findAll(final C criteria, final Class<?> classFrom) {
			return Members.INSTANCE.findAll(criteria, classFrom);
		}

		public Collection<M> findAllAndMakeThemAccessible(
			final C criteria,
			final Class<?> targetClass
		) {
			return findAllAndApply(
				criteria, targetClass, new Consumer<M>() {
					@Override
					public void accept(final M member) {
						setAccessible(member, true);
					}
				}
			);
		}

		public M findFirst(final C criteria, final Class<?> classFrom) {
			return Members.INSTANCE.findFirst(criteria, classFrom);
		}

		public M findOne(final C criteria, final Class<?> classFrom) {
			return Members.INSTANCE.findOne(criteria, classFrom);
		}

		public boolean match(final C criteria, final Class<?> classFrom) {
			return Members.INSTANCE.match(criteria, classFrom);
		}

		public void setAccessible(final M member, final boolean flag) {
			Facade.INSTANCE.setAccessible(((AccessibleObject)member), flag);
		}

		Collection<M> findAllAndApply(final C criteria, final Class<?> targetClass, final Consumer<M>... consumers) {
			final Collection<M> members = findAll(criteria, targetClass);
			if (consumers != null) {
				for (final M member : members) {
					for (final Consumer<M> consumer : consumers) {
						consumer.accept(member);
					}
				}
			}
			return members;
		}

		M findOneAndApply(final C criteria, final Class<?> targetClass, final Consumer<M>... consumers) {
			final M member = findOne(criteria, targetClass);
			if (member != null) {
				for (final Consumer<M> consumer : consumers) {
					consumer.accept(member);
				}
			}
			return member;
		}

		String getCacheKey(final Class<?> targetClass, final String groupName, Class<?>... arguments) {
			if (arguments == null) {
				arguments = new Class<?>[] {null};
			}
			String argumentsKey = "";
			if ((arguments != null) && (arguments.length > 0)) {
				final StringBuffer argumentsKeyStringBuffer = new StringBuffer();
				for (final Class<?> clazz : arguments) {
					argumentsKeyStringBuffer.append("/" +
						(clazz != null ? clazz.getName() : "null")
					);
				}
				argumentsKey = argumentsKeyStringBuffer.toString();
			}
			final String cacheKey = "/" + targetClass.getName() + "@" + targetClass.hashCode() +
				"/" + groupName +
				argumentsKey;
			return cacheKey;
		}
	}

}

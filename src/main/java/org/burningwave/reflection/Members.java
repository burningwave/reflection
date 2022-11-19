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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
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

	static {
		INSTANCE = new Members();
	}

	private Members() {}

	public <M extends Member> Collection<M> findAll(MemberCriteria<M, ?, ?> criteria, Class<?> classFrom) {
		ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> clsPredicate = criteria.getScanUpToPredicate();
		ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable> memberSupplier = criteria.getMembersSupplier();
		ThrowingPredicate<M, ? extends Throwable> predicate = criteria.getPredicateOrTruePredicateIfPredicateIsNull();
		Collection<M> result = findAll(
			classFrom,
			classFrom,
			clsPredicate,
			memberSupplier,
			predicate,
			new HashSet<>(),
			new LinkedHashSet<>()
		);
		ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate = criteria.getResultPredicate();
		try {
			return resultPredicate == null?
					result :
					resultPredicate.test(result)?
						result :
						new LinkedHashSet<>();
		} catch (Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	public <M extends Member> M findFirst(MemberCriteria<M, ?, ?> criteria, Class<?> classFrom) {
		ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate = criteria.getResultPredicate();
		if (resultPredicate == null) {
			return findFirst(
				classFrom,
				classFrom,
				criteria.getScanUpToPredicate(),
				criteria.getMembersSupplier(),
				criteria.getPredicateOrTruePredicateIfPredicateIsNull(),
				new HashSet<>()
			);
		} else {
			Collection<M> result = findAll(
				classFrom,
				classFrom,
				criteria.getScanUpToPredicate(),
				criteria.getMembersSupplier(),
				criteria.getPredicateOrTruePredicateIfPredicateIsNull(),
				new HashSet<>(),
				new LinkedHashSet<>()
			);
			try {
				if (resultPredicate.test(result)) {
					return result.iterator().next();
				}
				return null;
			} catch (Throwable exc) {
				return Throwables.INSTANCE.throwException(exc);
			}
		}
	}

	public <M extends Member> M findOne(MemberCriteria<M, ?, ?> criteria, Class<?> classFrom) {
		Collection<M> members = findAll(criteria, classFrom);
		if (members.size() > 1) {
			Throwables.INSTANCE.throwException("More than one member found for class {}", classFrom.getName());
		}
		if (!members.isEmpty()) {
			return members.iterator().next();
		}
		return null;
	}

	public <M extends Member> boolean match(MemberCriteria<M, ?, ?> criteria, Class<?> classFrom) {
		return findFirst(criteria, classFrom) != null;
	}

	private <M extends Member> Collection<M> findAll(
		Class<?> initialClsFrom,
		Class<?> currentScannedClass,
		ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> clsPredicate,
		ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable> memberSupplier,
		ThrowingPredicate<M, ? extends Throwable> predicate,
		Set<Class<?>> visitedInterfaces,
		Collection<M> collection
	) {
		try {
			for (M member : memberSupplier.apply(initialClsFrom, currentScannedClass)) {
				if (predicate.test(member)) {
					collection.add(member);
				}
			}
			for (Class<?> interf : currentScannedClass.getInterfaces()) {
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
		} catch (Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	private <M extends Member> M findFirst(
		Class<?> initialClsFrom,
		Class<?> currentScannedClass,
		ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> clsPredicate,
		ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable>
		memberSupplier, ThrowingPredicate<M, ? extends Throwable> predicate,
		Set<Class<?>> visitedInterfaces
	) {
		try {
			for (M member : memberSupplier.apply(initialClsFrom, currentScannedClass)) {
				if (predicate.test(member)) {
					return member;
				}
			}
			for (Class<?> interf : currentScannedClass.getInterfaces()) {
				if (!visitedInterfaces.add(interf)) {
					continue;
				}
				M member = findFirst(initialClsFrom, interf, clsPredicate, memberSupplier, predicate, visitedInterfaces);
				if ((member != null) || clsPredicate.test(initialClsFrom, currentScannedClass)) {
					return member;
				}
			}
			return
				(clsPredicate.test(initialClsFrom, currentScannedClass) || (currentScannedClass.getSuperclass() == null)) ?
					null :
					findFirst(initialClsFrom, currentScannedClass.getSuperclass(), clsPredicate, memberSupplier, predicate, visitedInterfaces);
		} catch (Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	public static abstract class Handler<M extends Member, C extends MemberCriteria<M, C, ?>> {

		public static abstract class OfExecutable<E extends Executable, C extends ExecutableMemberCriteria<E, C, ?>> extends Members.Handler<E, C> {

			OfExecutable() {}

			public Collection<MethodHandle> findAllDirectHandle(C criteria, Class<?> clsFrom) {
				Collection<MethodHandle> methodHandles = new LinkedHashSet<>();
				for (E member : findAll(criteria, clsFrom)) {
					methodHandles.add(findDirectHandle(member));
				}
				return methodHandles;
			}

			public MethodHandle findDirectHandle(E executable) {
				return findDirectHandleBox(executable).getHandler();
			}

			public MethodHandle findFirstDirectHandle(C criteria, Class<?> clsFrom) {
				E found = findFirst(criteria, clsFrom);
				if (found != null) {
					return findDirectHandle(found);
				}
				return null;
			}


			public MethodHandle findOneDirectHandle(C criteria, Class<?> clsFrom) {
				E found = findOne(criteria, clsFrom);
				if (found != null) {
					return findDirectHandle(found);
				}
				return null;
			}

			Members.Handler.OfExecutable.Box<E> findDirectHandleBox(E executable) {
				Class<?> targetClass = executable.getDeclaringClass();
				ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
				String cacheKey = getCacheKey(targetClass, "equals " + retrieveNameForCaching(executable), executable.getParameterTypes());
				return findDirectHandleBox(executable, targetClassClassLoader, cacheKey);
			}

			Members.Handler.OfExecutable.Box<E> findDirectHandleBox(E executable, ClassLoader classLoader, String cacheKey) {
				return (Box<E>)Cache.INSTANCE.uniqueKeyForExecutableAndMethodHandle.getOrUploadIfAbsent(classLoader, cacheKey, new Supplier<Box<?>>() {
					@Override
					public Box<?> get() {
						Class<?> methodDeclaringClass = executable.getDeclaringClass();
						return (Members.Handler.OfExecutable.Box<E>)Facade.INSTANCE.executeWithConsulter(
							methodDeclaringClass,
							new ThrowingFunction<Lookup, Box<E>, Throwable>() {
								@Override
								public Box<E> apply(Lookup consulter) throws Throwable {
									return new Members.Handler.OfExecutable.Box<>(consulter,
										executable,
										retrieveMethodHandle(consulter, executable)
									);
								}
							}
						).getValue();
					}
				});
			}

			Object[] getArgumentArray(
				E member,
				ThrowingTriFunction<E, Supplier<List<Object>>, Object[], List<Object>, ? extends Throwable> argumentListSupplier,
				Supplier<List<Object>> listSupplier,
				Object... arguments
			) {
				List<Object> argumentList;
				try {
					argumentList = argumentListSupplier.apply(member, listSupplier, arguments);
					return argumentList.toArray(new Object[argumentList.size()]);
				} catch (Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}

			List<Object> getArgumentListWithArrayForVarArgs(E member, Supplier<List<Object>> argumentListSupplier, Object... arguments) {
				Parameter[] parameters = member.getParameters();
				List<Object> argumentList = argumentListSupplier.get();
				if (arguments != null) {
					if ((parameters.length > 0) && parameters[parameters.length - 1].isVarArgs()) {
						for (int i = 0; (i < arguments.length) && (i < (parameters.length - 1)); i++) {
							argumentList.add(arguments[i]);
						}
						Parameter lastParameter = parameters[parameters.length -1];
						if (arguments.length == parameters.length) {
							Object lastArgument = arguments[arguments.length -1];
							if ((lastArgument != null) &&
								lastArgument.getClass().isArray() &&
								lastArgument.getClass().equals(lastParameter.getType())) {
								argumentList.add(lastArgument);
							} else {
								Object array = Array.newInstance(lastParameter.getType().getComponentType(), 1);
								Array.set(array, 0, lastArgument);
								argumentList.add(array);
							}
						} else if (arguments.length > parameters.length) {
							Object array = Array.newInstance(lastParameter.getType().getComponentType(), arguments.length - (parameters.length - 1));
							for (int i = parameters.length - 1, j = 0; i < arguments.length; i++, j++) {
								Array.set(array, j, arguments[i]);
							}
							argumentList.add(array);
						} else if (arguments.length < parameters.length) {
							argumentList.add(Array.newInstance(lastParameter.getType().getComponentType(),0));
						}
					} else if (arguments.length > 0) {
						for (Object argument : arguments) {
							argumentList.add(argument);
						}
					}
				} else {
					argumentList.add(null);
				}
				return argumentList;
			}

			List<Object> getFlatArgumentList(E member, Supplier<List<Object>> argumentListSupplier, Object... arguments) {
				Parameter[] parameters = member.getParameters();
				List<Object> argumentList = argumentListSupplier.get();
				if (arguments != null) {
					if ((parameters.length > 0) && parameters[parameters.length - 1].isVarArgs()) {
						for (int i = 0; (i < arguments.length) && (i < (parameters.length - 1)); i++) {
							argumentList.add(arguments[i]);
						}
						if (arguments.length == parameters.length) {
							Parameter lastParameter = parameters[parameters.length -1];
							Object lastArgument = arguments[arguments.length -1];
							if ((lastArgument != null) &&
								lastArgument.getClass().isArray() &&
								lastArgument.getClass().equals(lastParameter.getType())) {
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
						for (Object argument : arguments) {
							argumentList.add(argument);
						}
					}
				} else {
					argumentList.add(null);
				}
				return argumentList;
			}

			abstract MethodHandle retrieveMethodHandle(MethodHandles.Lookup consulter, E executable) throws NoSuchMethodException, IllegalAccessException;

			abstract String retrieveNameForCaching(E executable);

			Class<?>[] retrieveParameterTypes(Executable member, List<Class<?>> argumentsClassesAsList) {
				Parameter[] memberParameter = member.getParameters();
				Class<?>[] memberParameterTypes = member.getParameterTypes();
				if ((memberParameter.length > 0) && memberParameter[memberParameter.length - 1].isVarArgs()) {
					Class<?> varArgsType =
						(argumentsClassesAsList.size() > 0) &&
						(argumentsClassesAsList.get(argumentsClassesAsList.size()-1) != null) &&
						argumentsClassesAsList.get(argumentsClassesAsList.size()-1).isArray()?
						memberParameter[memberParameter.length - 1].getType():
						memberParameter[memberParameter.length - 1].getType().getComponentType();
					if (memberParameter.length == 1) {
						memberParameterTypes = new Class<?>[argumentsClassesAsList.size()];
						for (int j = 0; j < memberParameterTypes.length; j++) {
							memberParameterTypes[j] = varArgsType;
						}
					} else if ((memberParameter.length - 1) <= argumentsClassesAsList.size()) {
						memberParameterTypes = new Class<?>[argumentsClassesAsList.size()];
						for (int j = 0; j < memberParameterTypes.length; j++) {
							if (j < (memberParameter.length - 1)) {
								memberParameterTypes[j] = memberParameter[j].getType();
							} else {
								memberParameterTypes[j] = varArgsType;
							}
						}
					}
				}
				return memberParameterTypes;
			}

			Collection<E> searchForExactMatch(Collection<E> members, Class<?>... arguments) {
				List<Class<?>> argumentsClassesAsList = Arrays.asList(arguments);
				//Collection<E> membersThatMatch = new LinkedHashSet<>();
				Collection<E> membersThatMatch = new TreeSet<>(new Comparator<E>() {
					@Override
					public int compare(E executableOne, E executableTwo) {
						Parameter[] executableOneParameters = executableOne.getParameters();
						Parameter[] executableTwoParameters = executableTwo.getParameters();
						if (executableOneParameters.length == argumentsClassesAsList.size()) {
							if (executableTwoParameters.length == argumentsClassesAsList.size()) {
								if ((executableOneParameters.length > 0) && executableOneParameters[executableOneParameters.length - 1].isVarArgs()) {
									if ((executableTwoParameters.length > 0) && executableTwoParameters[executableTwoParameters.length - 1].isVarArgs()) {
										return 0;
									}
									return 1;
								} else if ((executableTwoParameters.length > 0) && executableTwoParameters[executableTwoParameters.length - 1].isVarArgs()) {
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

				for (E executable : members) {
					Class<?>[] parameterTypes = retrieveParameterTypes(executable, argumentsClassesAsList);
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

			public static class Box<E extends Executable> {
				MethodHandles.Lookup consulter;
				E executable;
				MethodHandle handler;

				Box(MethodHandles.Lookup consulter, E executable, MethodHandle handler) {
					super();
					this.consulter = consulter;
					this.executable = executable;
					this.handler = handler;
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

			}

		}

		public Collection<M> findAll(C criteria, Class<?> classFrom) {
			return Members.INSTANCE.findAll(criteria, classFrom);
		}

		public Collection<M> findAllAndMakeThemAccessible(
			C criteria,
			Class<?> targetClass
		) {
			return findAllAndApply(
				criteria, targetClass, new Consumer<M>() {
					@Override
					public void accept(M member) {
						setAccessible(member, true);
					}
				}
			);
		}

		public M findFirst(C criteria, Class<?> classFrom) {
			return Members.INSTANCE.findFirst(criteria, classFrom);
		}

		public M findOne(C criteria, Class<?> classFrom) {
			return Members.INSTANCE.findOne(criteria, classFrom);
		}

		public boolean match(C criteria, Class<?> classFrom) {
			return Members.INSTANCE.match(criteria, classFrom);
		}

		public void setAccessible(M member, boolean flag) {
			Facade.INSTANCE.setAccessible(((AccessibleObject)member), flag);
		}

		Collection<M> findAllAndApply(C criteria, Class<?> targetClass, Consumer<M>... consumers) {
			Collection<M> members = findAll(criteria, targetClass);
			if (consumers != null) {
				for (M member : members) {
					for (Consumer<M> consumer : consumers) {
						consumer.accept(member);
					}
				}
			}
			return members;
		}

		M findOneAndApply(C criteria, Class<?> targetClass, Consumer<M>... consumers) {
			M member = findOne(criteria, targetClass);
			if (member != null) {
				for (Consumer<M> consumer : consumers) {
					consumer.accept(member);
				}
			}
			return member;
		}

		String getCacheKey(Class<?> targetClass, String groupName, Class<?>... arguments) {
			if (arguments == null) {
				arguments = new Class<?>[] {null};
			}
			String argumentsKey = "";
			if ((arguments != null) && (arguments.length > 0)) {
				StringBuffer argumentsKeyStringBuffer = new StringBuffer();
				for (Class<?> clazz : arguments) {
					argumentsKeyStringBuffer.append("/" +
						(clazz != null ? clazz.getName() : "null")
					);
				}
				argumentsKey = argumentsKeyStringBuffer.toString();
			}
			String cacheKey = "/" + targetClass.getName() + "@" + targetClass.hashCode() +
				"/" + groupName +
				argumentsKey;
			return cacheKey;
		}
	}

}

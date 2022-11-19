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

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.burningwave.Criteria;
import org.burningwave.Throwables;
import org.burningwave.function.ThrowingBiFunction;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingTriPredicate;

@SuppressWarnings("unchecked")
public abstract class ExecutableMemberCriteria<
	E extends Executable,
	C extends ExecutableMemberCriteria<E, C, T>,
	T extends Criteria.TestContext<E, C>
> extends MemberCriteria<E, C, T> {

	public C parameter(final ThrowingBiPredicate<Parameter[], Integer, ? extends Throwable> predicate) {
		this.predicate = concat(
			this.predicate,
			getPredicateWrapper(
				new ThrowingBiFunction<T, E, Parameter[], Throwable>() {
					@Override
					public Parameter[] apply(T criteria, E member) {
						return member.getParameters();
					}
				},
				new ThrowingTriPredicate<T, Parameter[], Integer, Throwable>() {
					@Override
					public boolean test(T criteria, Parameter[] array, Integer index) throws Throwable {
						return predicate.test(array, index);
					}
				}
			)
		);
		return (C)this;
	}

	public C parameterType(final ThrowingBiPredicate<Class<?>[], Integer, ? extends Throwable> predicate) {
		this.predicate = concat(
			this.predicate,
			getPredicateWrapper(
				new ThrowingBiFunction<T, E, Class<?>[], Throwable>() {
					@Override
					public Class<?>[] apply(T criteria, E member) {
						return member.getParameterTypes();
					}
				},
				new ThrowingTriPredicate<T, Class<?>[], Integer, Throwable>() {
					@Override
					public boolean test(T criteria, Class<?>[] array, Integer index) throws Throwable {
						return predicate.test(array, index);
					}
				}
			)
		);
		return (C)this;
	}


	public C parameterTypes(final ThrowingPredicate<Class<?>[], ? extends Throwable> predicate) {
		this.predicate = concat(
			this.predicate,
			new ThrowingBiPredicate<T, E, Throwable>() {
				@Override
				public boolean test(T context, E member) throws Throwable {
					return predicate.test(member.getParameterTypes());
				}
			}
		);
		return (C)this;
	}

	public C parameterTypesAreAssignableFrom(Class<?>... argumentsClasses) {
		return parameterTypesMatch(
			new ThrowingTriPredicate<List<Class<?>>, Class<?>[], Integer, Throwable>() {
				@Override
				public boolean test(List<Class<?>> argClasses, Class<?>[] paramTypes, Integer innerIdx) {
					return ((argClasses.get(innerIdx) == null) || Classes.INSTANCE.isAssignableFrom(paramTypes[innerIdx], argClasses.get(innerIdx)));
				}
			},
			argumentsClasses
		);
	}

	public C parameterTypesAreAssignableFromTypesOf(Object... arguments) {
		return parameterTypesAreAssignableFrom(Classes.INSTANCE.retrieveFrom(arguments));
	}


	public C parameterTypesExactlyMatch(Class<?>... argumentsClasses) {
		return parameterTypesMatch(
			new ThrowingTriPredicate<List<Class<?>>, Class<?>[], Integer, Throwable>() {
				@Override
				public boolean test(List<Class<?>> argClasses, Class<?>[] paramTypes, Integer innerIdx) {
					return ((argClasses.get(innerIdx) == null) || Classes.INSTANCE.getClassOrWrapper(paramTypes[innerIdx]).equals(Classes.INSTANCE.getClassOrWrapper(argClasses.get(innerIdx))));
				}
			},
			argumentsClasses
		);
	}


	public C parameterTypesExactlyMatchTypesOf(Object... arguments) {
		return parameterTypesAreAssignableFrom(Classes.INSTANCE.retrieveFrom(arguments));
	}

	private C parameterTypesMatch(
		final ThrowingTriPredicate<List<Class<?>>, Class<?>[], Integer, ? extends Throwable> predicate,
		Class<?>... arguments
	) {
		if (arguments == null) {
			arguments = new Class<?>[]{null};
		}
		Class<?>[] argumentsClasses = arguments;
		if ((argumentsClasses != null) && (argumentsClasses.length > 0)) {
			final List<Class<?>> argumentsClassesAsList = Arrays.asList(argumentsClasses);
			for (int i = 0; i < argumentsClasses.length; i++) {
				final int index = i;
				this.predicate = concat(
					this.predicate,
					new ThrowingBiPredicate<T, E, Throwable>() {
						@Override
						public boolean test(T context, E member) {
							Parameter[] memberParameter = member.getParameters();
							if ((memberParameter.length > 1) &&
								memberParameter[memberParameter.length - 1].isVarArgs() &&
								((memberParameter.length - 1) > argumentsClassesAsList.size())
							) {
								return false;
							}
							Class<?>[] memberParameterTypes = Methods.INSTANCE.retrieveParameterTypes(member, argumentsClassesAsList);
							if (argumentsClassesAsList.size() == memberParameterTypes.length) {
								try {
									return predicate.test(argumentsClassesAsList, memberParameterTypes, index);
								} catch (Throwable exc) {
									return Throwables.INSTANCE.throwException(exc);
								}
							} else {
								return false;
							}
						}
					}
				);
				if (index < (arguments.length - 1)) {
					and();
				}
			}
		} else {
			parameterTypes(
				new ThrowingPredicate<Class<?>[], Throwable>() {
					@Override
					public boolean test(Class<?>[] parameters) {
						return parameters.length == 0;
					}
				}
			);
		}
		return (C)this;
	}
}

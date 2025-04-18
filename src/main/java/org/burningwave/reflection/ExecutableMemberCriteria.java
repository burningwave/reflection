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
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.burningwave.Classes;
import org.burningwave.TriPredicate;
import org.burningwave.reflection.Members.Handler;

@SuppressWarnings("unchecked")
public abstract class ExecutableMemberCriteria<
	E extends Executable,
	C extends ExecutableMemberCriteria<E, C, T>,
	T extends Criteria.TestContext<E, C>
> extends MemberCriteria<E, C, T> {

	public C parameter(final BiPredicate<Parameter[], Integer> predicate) {
		this.predicate = concat(
			this.predicate,
			getPredicateWrapper(
				(testContext, member) -> member.getParameters(),
				(testContext, array, index) -> predicate.test(array, index)
			)
		);
		return (C)this;
	}

	public C parameterType(final BiPredicate<Class<?>[], Integer> predicate) {
		this.predicate = concat(
			this.predicate,
			getPredicateWrapper(
				(testContext, member) -> member.getParameterTypes(),
				(testContext, array, index) -> predicate.test(array, index)
			)
		);
		return (C)this;
	}


	public C parameterTypes(final Predicate<Class<?>[]> predicate) {
		this.predicate = concat(
			this.predicate,
			(context, member) -> predicate.test(member.getParameterTypes())
		);
		return (C)this;
	}

	public C parameterTypesAreAssignableFrom(Class<?>... argumentsClasses) {
		return parameterTypesMatch(
			(argClasses, paramTypes, innerIdx) ->
				(argClasses.get(innerIdx) == null || Classes.INSTANCE.isAssignableFrom(paramTypes[innerIdx], argClasses.get(innerIdx))),
			argumentsClasses
		);
	}

	public C parameterTypesAreAssignableFromTypesOf(Object... arguments) {
		return parameterTypesAreAssignableFrom(Classes.INSTANCE.retrieveFrom(arguments));
	}


	public C parameterTypesExactlyMatch(Class<?>... argumentsClasses) {
		return parameterTypesMatch(
			(argClasses, paramTypes, innerIdx) ->
				(argClasses.get(innerIdx) == null || Classes.INSTANCE.getClassOrWrapper(paramTypes[innerIdx]).equals(Classes.INSTANCE.getClassOrWrapper(argClasses.get(innerIdx)))),
			argumentsClasses
		);
	}


	public C parameterTypesExactlyMatchTypesOf(Object... arguments) {
		return parameterTypesAreAssignableFrom(Classes.INSTANCE.retrieveFrom(arguments));
	}

	private C parameterTypesMatch(TriPredicate<List<Class<?>>, Class<?>[], Integer> predicate, Class<?>... arguments) {
		if (arguments == null) {
			arguments = new Class<?>[]{null};
		}
		Class<?>[] argumentsClasses = arguments;
		if (argumentsClasses != null && argumentsClasses.length > 0) {
			List<Class<?>> argumentsClassesAsList = Arrays.asList(argumentsClasses);
			for (int i = 0; i < argumentsClasses.length; i++) {
				final int index = i;
				this.predicate = concat(
					this.predicate,
					(context, member) -> {
						Parameter[] memberParameter = member.getParameters();
						if (memberParameter.length > 1 &&
							memberParameter[memberParameter.length - 1].isVarArgs() &&
							(memberParameter.length - 1) > argumentsClassesAsList.size()
						) {
							return false;
						}
						Class<?>[] memberParameterTypes = Handler.OfExecutable.retrieveParameterTypes(member, argumentsClassesAsList);
						if (argumentsClassesAsList.size() == memberParameterTypes.length) {
							return predicate.test(argumentsClassesAsList, memberParameterTypes, index);
						} else {
							return false;
						}
					}
				);
				if (index < arguments.length - 1) {
					and();
				}
			}
		} else {
			parameterTypes(
				parameters ->
				parameters.length == 0
			);
		}
		return (C)this;
	}
}

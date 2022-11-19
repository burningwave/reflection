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

import java.lang.reflect.Constructor;

import org.burningwave.Criteria;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingFunction;

public class ConstructorCriteria extends ExecutableMemberCriteria<
	Constructor<?>, ConstructorCriteria, Criteria.TestContext<Constructor<?>, ConstructorCriteria>
> {
	private ConstructorCriteria() {
		super();
	}

	public static ConstructorCriteria byScanUpTo(ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> predicate) {
		return new ConstructorCriteria().scanUpTo(predicate);
	}

	public static ConstructorCriteria forEntireClassHierarchy() {
		return new ConstructorCriteria();
	}

	public static ConstructorCriteria withoutConsideringParentClasses() {
		return byScanUpTo(new ThrowingBiPredicate<Class<?>, Class<?>, Throwable>() {
			@Override
			public boolean test(Class<?> lastClassInHierarchy, Class<?> currentScannedClass) {
			    return lastClassInHierarchy.equals(currentScannedClass);
			}
		});
	}

	@Override
	ThrowingFunction<Class<?>, Constructor<?>[], ? extends Throwable> getMembersSupplierFunction() {
		return new ThrowingFunction<>() {
			@Override
			public Constructor<?>[] apply(Class<?> clazz) throws Throwable {
				return Facade.INSTANCE.getDeclaredConstructors(clazz);
			}
		};
	}

}
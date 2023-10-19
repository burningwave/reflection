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

import java.lang.reflect.Member;
import java.util.Collection;

import org.burningwave.Criteria;
import org.burningwave.function.Function;
import org.burningwave.function.Handler;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingFunction;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingTriPredicate;

import io.github.toolfactory.jvm.function.template.ThrowingBiFunction;


@SuppressWarnings("unchecked")
public abstract class MemberCriteria<M extends Member, C extends MemberCriteria<M, C, T>, T extends Criteria.TestContext<M, C>> extends Criteria<M, C, T> {
	private static Member[] EMPTY_MEMBERS_ARRAY = {};
	ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate;
	ThrowingTriPredicate<C, Class<?>, Class<?>, ? extends Throwable> scanUpToPredicate;
	ThrowingTriPredicate<C, Class<?>, Class<?>, ? extends Throwable> skipClassPredicate;


	@Override
	public C createCopy() {
		C copy = super.createCopy();
		copy.scanUpToPredicate = this.scanUpToPredicate;
		copy.skipClassPredicate = this.skipClassPredicate;
		copy.resultPredicate = this.resultPredicate;
		return copy;
	}

	public C name(final ThrowingPredicate<String, ? extends Throwable> predicate) {
		this.predicate = concat(
			this.predicate,
			new ThrowingBiPredicate<T, M, Throwable>() {
				@Override
				public boolean test(T context, M member) throws Throwable {
					return predicate.test(member.getName());
				}
			}
		);
		return (C)this;
	}

	public C result(ThrowingPredicate<Collection<M>, ? extends Throwable> resultPredicate) {
		this.resultPredicate = resultPredicate;
		return (C)this;
	}


	public C skip(final ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> predicate) {
		if (skipClassPredicate != null) {
			skipClassPredicate = Handler.or(
				skipClassPredicate,
				(ThrowingTriPredicate)new ThrowingTriPredicate<C, Class<?>, Class<?>, Throwable>() {
					@Override
					public boolean test(C criteria, Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
						return predicate.test(initialClassFrom, currentClass);
					}
				}
			);
		} else {
			skipClassPredicate = new ThrowingTriPredicate<C, Class<?>, Class<?>, Throwable>() {
				@Override
				public boolean test(C criteria, Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
					return predicate.test(initialClassFrom, currentClass);
				}
			};
		}
		return (C)this;
	}

	@Override
	protected C logicOperation(C leftCriteria, C rightCriteria,
		Function<ThrowingBiPredicate<T, M, ? extends Throwable>, Function<ThrowingBiPredicate<? super T, ? super M, ? extends Throwable>, ThrowingBiPredicate<T, M, ? extends Throwable>>> binaryOperator,
		C targetCriteria
	) {
		C newCriteria = super.logicOperation(leftCriteria, rightCriteria, binaryOperator, targetCriteria);
		newCriteria.scanUpToPredicate =
			leftCriteria.scanUpToPredicate != null?
				rightCriteria.scanUpToPredicate != null?
					Handler.or(leftCriteria.scanUpToPredicate, (ThrowingTriPredicate)rightCriteria.scanUpToPredicate) :
					null :
				null;
		newCriteria.skipClassPredicate =
			leftCriteria.skipClassPredicate != null?
				rightCriteria.skipClassPredicate != null?
					Handler.or(leftCriteria.skipClassPredicate, (ThrowingTriPredicate)rightCriteria.skipClassPredicate) :
					leftCriteria.skipClassPredicate :
				rightCriteria.skipClassPredicate;
//		newCriteria.resultPredicate =
//			leftCriteria.resultPredicate != null?
//				rightCriteria.resultPredicate != null?
//					leftCriteria.resultPredicate.or(rightCriteria.resultPredicate) :
//					leftCriteria.resultPredicate :
//				rightCriteria.resultPredicate;
		return newCriteria;
	}

	ThrowingBiFunction<Class<?>, Class<?>, M[], ? extends Throwable> getMembersSupplier() {
		return new ThrowingBiFunction<Class<?>, Class<?>, M[], Throwable>() {
			@Override
			public M[] apply(Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
				return !((skipClassPredicate != null) && skipClassPredicate.test((C)MemberCriteria.this, initialClassFrom, currentClass)) ?
					getMembersSupplierFunction().apply(currentClass) :
					(M[])EMPTY_MEMBERS_ARRAY;
			}
		};
	}

	abstract ThrowingFunction<Class<?>, M[], ? extends Throwable> getMembersSupplierFunction();

	ThrowingPredicate<Collection<M>, ? extends Throwable> getResultPredicate() {
		return this.resultPredicate;
	}

	ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> getScanUpToPredicate() {
		return scanUpToPredicate != null?
			new ThrowingBiPredicate<Class<?>, Class<?>, Throwable>() {
				@Override
				public boolean test(Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
					return MemberCriteria.this.scanUpToPredicate.test((C)MemberCriteria.this, initialClassFrom, currentClass);
				}
			}:
			new ThrowingBiPredicate<Class<?>, Class<?>, Throwable>() {
				@Override
				public boolean test(Class<?> initialClassFrom, Class<?> currentClass) {
					return currentClass.getName().equals(Object.class.getName());
				}
			};
	}

	C scanUpTo(final ThrowingBiPredicate<Class<?>, Class<?>, ? extends Throwable> predicate) {
		this.scanUpToPredicate = new ThrowingTriPredicate<C, Class<?>, Class<?>, Throwable>() {
			@Override
			public boolean test(C criteria, Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
				return predicate.test(initialClassFrom, currentClass);
			}
		};
		return (C)this;
	}

	C scanUpTo(final ThrowingPredicate<Class<?>, ? extends Throwable> predicate) {
		this.scanUpToPredicate = new ThrowingTriPredicate<C, Class<?>, Class<?>, Throwable>() {
			@Override
			public boolean test(C criteria, Class<?> initialClassFrom, Class<?> currentClass) throws Throwable {
				return predicate.test(currentClass);
			}
		};
		return (C)this;
	}

}

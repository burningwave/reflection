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


import java.lang.reflect.Member;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.burningwave.TriPredicate;


@SuppressWarnings("unchecked")
public abstract class MemberCriteria<M extends Member, C extends MemberCriteria<M, C, T>, T extends Criteria.TestContext<M, C>> extends Criteria<M, C, T> {
	private static Member[] EMPTY_MEMBERS_ARRAY = {};
	Predicate<Collection<M>> resultPredicate;
	TriPredicate<C, Class<?>, Class<?>> scanUpToPredicate;
	TriPredicate<C, Class<?>, Class<?>> skipClassPredicate;


	@Override
	public C createCopy() {
		C copy = super.createCopy();
		copy.scanUpToPredicate = this.scanUpToPredicate;
		copy.skipClassPredicate = this.skipClassPredicate;
		copy.resultPredicate = this.resultPredicate;
		return copy;
	}

	public C name(final Predicate<String> predicate) {
		this.predicate = concat(
			this.predicate,
			(context, member) ->
				predicate.test(member.getName())
		);
		return (C)this;
	}

	public C result(Predicate<Collection<M>> resultPredicate) {
		this.resultPredicate = resultPredicate;
		return (C)this;
	}


	public C skip(BiPredicate<Class<?>, Class<?>> predicate) {
		if (skipClassPredicate != null) {
			skipClassPredicate = skipClassPredicate.or((criteria, initialClassFrom, currentClass) ->
				predicate.test(initialClassFrom, currentClass)
			);
		} else {
			skipClassPredicate = (criteria, initialClassFrom, currentClass) ->
				predicate.test(initialClassFrom, currentClass);
		}
		return (C)this;
	}

	@Override
	protected C logicOperation(C leftCriteria, C rightCriteria,
			Function<BiPredicate<T, M>, Function<BiPredicate<? super T, ? super M>, BiPredicate<T, M>>> binaryOperator,
			C targetCriteria) {
		C newCriteria = super.logicOperation(leftCriteria, rightCriteria, binaryOperator, targetCriteria);
		newCriteria.scanUpToPredicate =
			leftCriteria.scanUpToPredicate != null?
				rightCriteria.scanUpToPredicate != null?
					leftCriteria.scanUpToPredicate.or(rightCriteria.scanUpToPredicate) :
					null :
				null;
		newCriteria.skipClassPredicate =
			leftCriteria.skipClassPredicate != null?
				rightCriteria.skipClassPredicate != null?
					leftCriteria.skipClassPredicate.or(rightCriteria.skipClassPredicate) :
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

	BiFunction<Class<?>, Class<?>, M[]> getMembersSupplier() {
		return (initialClassFrom, currentClass) ->
			!(skipClassPredicate != null && skipClassPredicate.test((C)this, initialClassFrom, currentClass)) ?
				getMembersSupplierFunction().apply(currentClass) :
				(M[]) EMPTY_MEMBERS_ARRAY;
	}

	abstract Function<Class<?>, M[]> getMembersSupplierFunction();

	Predicate<Collection<M>> getResultPredicate() {
		return this.resultPredicate;
	}

	BiPredicate<Class<?>, Class<?>> getScanUpToPredicate() {
		return scanUpToPredicate != null?
			(initialClassFrom, currentClass) -> this.scanUpToPredicate.test((C)this, initialClassFrom, currentClass):
			(initialClassFrom, currentClass) -> currentClass.getName().equals(Object.class.getName()
		);
	}

	C scanUpTo(BiPredicate<Class<?>, Class<?>> predicate) {
		this.scanUpToPredicate = (criteria, initialClassFrom, currentClass) -> predicate.test(initialClassFrom, currentClass);
		return (C)this;
	}

	C scanUpTo(Predicate<Class<?>> predicate) {
		this.scanUpToPredicate = (criteria, initialClassFrom, currentClass) -> predicate.test(currentClass);
		return (C)this;
	}

}

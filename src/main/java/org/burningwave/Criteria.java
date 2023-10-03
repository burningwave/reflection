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
package org.burningwave;


import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.burningwave.function.TriPredicate;
import org.burningwave.reflection.Constructors;

@SuppressWarnings("unchecked")
public class Criteria<E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> {

	protected Function<BiPredicate<T, E>, BiPredicate<T, E>> logicalOperator;

	protected BiPredicate<T, E> predicate;

	public final static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> Criteria<E, C, T> of(final BiPredicate<T, E> predicate) {
		return new Criteria<E, C, T>().allThoseThatMatch(predicate);
	}

	public final static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> Criteria<E, C, T> of(final Predicate<E> predicate) {
		return new Criteria<E, C, T>().allThoseThatMatch(predicate);
	}

	public C allThoseThatMatch(final BiPredicate<T, E> predicate) {
		this.predicate = concat(
			this.predicate,
			(context, entity) -> predicate.test(context, entity)
		);
		return (C)this;
	}

	public C allThoseThatMatch(final Predicate<E> predicate) {
		return allThoseThatMatch((context, entity) -> predicate.test(entity)) ;
	}

	public C and(){
		logicalOperator = (predicate) -> this.predicate.and(predicate);
		return (C)this;
	}

	public C and(C criteria) {
		return logicOperation(this.createCopy(), criteria.createCopy(), (predicate) -> predicate::and, newInstance());
	}

	public C or(){
		logicalOperator = (predicate) -> this.predicate.or(predicate);
		return (C)this;
	}

	public C or(C criteria) {
		return logicOperation(this.createCopy(), criteria.createCopy(), (predicate) -> predicate::or, newInstance());
	}

	public C negate() {
		predicate = predicate.negate();
		return (C)this;
	}

	public C createCopy() {
		C copy = newInstance();
		copy.predicate = this.predicate;
		copy.logicalOperator = this.logicalOperator;
		return copy;
	}

	public Predicate<E> getPredicateOrFalsePredicateIfPredicateIsNull() {
		return getPredicate(createTestContext(), false);
	}

	public Predicate<E> getPredicateOrTruePredicateIfPredicateIsNull() {
		return getPredicate(createTestContext(), true);
	}

	public boolean hasNoPredicate() {
		return this.predicate == null;
	}

	public T testWithFalseResultForNullEntityOrFalseResultForNullPredicate(E entity) {
		T context = createTestContext();
		testWithFalseResultForNullEntityOrFalseResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithFalseResultForNullEntityOrTrueResultForNullPredicate(E entity) {
		T context = createTestContext();
		testWithFalseResultForNullEntityOrTrueResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithTrueResultForNullEntityOrFalseResultForNullPredicate(E entity) {
		T context = createTestContext();
		testWithTrueResultForNullEntityOrFalseResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithTrueResultForNullEntityOrTrueResultForNullPredicate(E entity) {
		T context = createTestContext();
		testWithTrueResultForNullEntityOrTrueResultForNullPredicate(context, entity);
		return context;
	}

	protected BiPredicate<T, E> concat(
		BiPredicate<T, E> mainPredicate,
		BiPredicate<T, E> otherPredicate
	) {
		BiPredicate<T, E> predicate = concat(mainPredicate, this.logicalOperator, otherPredicate);
		this.logicalOperator = null;
		return predicate;
	}

	@SuppressWarnings("hiding")
	protected <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> BiPredicate<T, E> concat(
		BiPredicate<T, E> mainPredicate,
		Function<BiPredicate<T, E>, BiPredicate<T, E>> logicalOperator,
		BiPredicate<T, E> otherPredicate
	) {
		return Optional.ofNullable(otherPredicate).map(othPred ->
			Optional.ofNullable(mainPredicate).map(mainPred ->
				consumeLogicalOperator(othPred, logicalOperator)
			).orElse(othPred)
		).orElse(mainPredicate);
	}

	protected T createTestContext() {
		return (T)TestContext.<E, C, T>create((C)this);
	}

	protected T getContextWithFalsePredicateForNullPredicate() {
		T context = createTestContext();
		getPredicate(context, false);
		return context;
	}

	protected T getContextWithTruePredicateForNullPredicate() {
		T context = createTestContext();
		getPredicate(context, true);
		return context;
	}

	protected <V> BiPredicate<T, E> getPredicateWrapper(
		final BiFunction<T, E, V[]> valueSupplier,
		final TriPredicate<T, V[], Integer> predicate
	) {
		return getPredicateWrapper((criteria, entity) -> {
			V[] array = valueSupplier.apply(criteria, entity);
			boolean result = false;
			for (int i = 0; i < array.length; i++) {
				if (result = predicate.test(criteria, array, i)) {
					break;
				}
			}
			//logDebug("test for {} return {}", entity, result);
			return result;
		});
	}

	protected C logicOperation(C leftCriteria, C rightCriteria,
		Function<BiPredicate<T, E>, Function<BiPredicate<? super T, ? super E>, BiPredicate<T, E>>> binaryOperator,
		C targetCriteria
	) {
		targetCriteria.predicate =
			leftCriteria.predicate != null?
				(rightCriteria.predicate != null?
					binaryOperator.apply(leftCriteria.predicate).apply(rightCriteria.predicate) :
					leftCriteria.predicate):
				rightCriteria.predicate;
		return targetCriteria;
	}

	protected C newInstance() {
		return (C)Constructors.INSTANCE.newInstanceOf(this.getClass());
	}

	@SuppressWarnings("hiding")
	<E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> BiPredicate<T,E> consumeLogicalOperator(
		BiPredicate<T, E> input,
		Function<BiPredicate<T, E>,
		BiPredicate<T, E>> logicalOperator
	) {
		return Optional.ofNullable(logicalOperator).map(logOp -> {
			return logicalOperator.apply(input);
		}).orElseGet(() ->
			Throwables.INSTANCE.throwException(
				"A call to and/or method is necessary before calling {} at {}",
				Thread.currentThread().getStackTrace()[10].getMethodName(),
				Thread.currentThread().getStackTrace()[11]
			)
		);
	}

	BiPredicate<T, E> getPredicateWrapper(
		BiPredicate<T, E> function
	) {
		return Optional.ofNullable(function).map(innPredWrap ->
			(BiPredicate<T, E>) (criteria, entity) -> innPredWrap.test(criteria, entity)
		).orElse(null);
	}

	private Predicate<E> getPredicate(T context, boolean defaultResult) {
		return context.setPredicate(
			this.predicate != null?
				(entity) -> {
				return context.setEntity(entity).setResult(this.predicate.test(
					context,
					entity
				)).getResult();
			} :
			(entity) -> {
				return context.setEntity(entity).setResult(defaultResult).getResult();
			}
		).getPredicate();
	}


	private boolean testWithFalseResultForNullEntityOrFalseResultForNullPredicate(T context, E entity) {
		return Optional.ofNullable(entity).map(ent -> getPredicate(context, false).test(ent)).orElseGet(() ->
			context.setEntity(entity).setResult(false).getResult()
		);
	}


	private boolean testWithFalseResultForNullEntityOrTrueResultForNullPredicate(T context, E entity) {
		return Optional.ofNullable(entity).map(ent -> getPredicate(context, true).test(ent)).orElseGet(() ->
			context.setEntity(entity).setResult(false).getResult()
		);
	}


	private boolean testWithTrueResultForNullEntityOrFalseResultForNullPredicate(T context, E entity) {
		return Optional.ofNullable(entity).map(ent -> getPredicate(context, false).test(ent)).orElseGet(() ->
			context.setEntity(entity).setResult(true).getResult()
		);
	}

	private boolean testWithTrueResultForNullEntityOrTrueResultForNullPredicate(T context, E entity) {
		return Optional.ofNullable(entity).map(ent -> getPredicate(context, true).test(ent)).orElseGet(() ->
			context.setEntity(entity).setResult(true).getResult()
		);
	}

	public static class Simple<E, C extends Simple<E, C>> {

		protected Function<Predicate<E>, Predicate<E>> logicalOperator;
		protected Predicate<E> predicate;

		public C allThoseThatMatch(final Predicate<E> predicate) {
			this.predicate = concat(
				this.predicate,
				(entity) -> predicate.test(entity)
			);
			return (C)this;
		}

		public C and(){
			logicalOperator = (predicate) -> this.predicate.and(predicate);
			return (C)this;
		}

		public C and(C criteria) {
			return logicOperation(this.createCopy(), criteria.createCopy(), (predicate) -> predicate::and, newInstance());
		}

		public C or(){
			logicalOperator = (predicate) -> this.predicate.or(predicate);
			return (C)this;
		}

		public C or(C criteria) {
			return logicOperation(this.createCopy(), criteria.createCopy(), (predicate) -> predicate::or, newInstance());
		}

		public C negate() {
			predicate = predicate.negate();
			return (C)this;
		}

		public C createCopy() {
			C copy = newInstance();
			copy.predicate = this.predicate;
			copy.logicalOperator = this.logicalOperator;
			return copy;
		}

		public Predicate<E> getPredicateOrFalsePredicateIfPredicateIsNull() {
			return getPredicate(false);
		}


		public Predicate<E> getPredicateOrTruePredicateIfPredicateIsNull() {
			return getPredicate(true);
		}

		public boolean hasNoPredicate() {
			return this.predicate == null;
		}

		public boolean testWithFalseResultForNullEntityOrFalseResultForNullPredicate(E entity) {
			return Optional.ofNullable(entity).map(ent -> getPredicateOrFalsePredicateIfPredicateIsNull().test(ent)).orElseGet(() ->
				false
			);
		}


		public boolean testWithFalseResultForNullEntityOrTrueResultForNullPredicate(E entity) {
			return Optional.ofNullable(entity).map(ent -> getPredicateOrTruePredicateIfPredicateIsNull().test(ent)).orElseGet(() ->
				false
			);
		}

		public boolean testWithFalseResultForNullPredicate(E entity) {
			return getPredicateOrFalsePredicateIfPredicateIsNull().test(entity);
		}

		public boolean testWithTrueResultForNullEntityOrFalseResultForNullPredicate(E entity) {
			return Optional.ofNullable(entity).map(ent -> getPredicateOrFalsePredicateIfPredicateIsNull().test(ent)).orElseGet(() ->
				true
			);
		}

		public boolean testWithTrueResultForNullEntityOrTrueResultForNullPredicate(E entity) {
			return Optional.ofNullable(entity).map(ent -> getPredicateOrTruePredicateIfPredicateIsNull().test(ent)).orElseGet(() ->
				true
			);
		}

		public boolean testWithTrueResultForNullPredicate(E entity) {
			return getPredicateOrTruePredicateIfPredicateIsNull().test(entity);
		}

		@SuppressWarnings("hiding")
		protected <E, C extends Simple<E, C>> Predicate<E> concat(
			Predicate<E> mainPredicate,
			Function<Predicate<E>, Predicate<E>> logicalOperator,
			Predicate<E> otherPredicate
		) {
			return Optional.ofNullable(otherPredicate).map(othPred ->
				Optional.ofNullable(mainPredicate).map(mainPred ->
					consumeLogicalOperator(othPred, logicalOperator)
				).orElse(othPred)
			).orElse(mainPredicate);
		}

		protected Predicate<E> concat(
			Predicate<E> mainPredicate,
			Predicate<E> otherPredicate
		) {
			Predicate<E> predicate = concat(mainPredicate, this.logicalOperator, otherPredicate);
			this.logicalOperator = null;
			return predicate;
		}

		protected <V> Predicate<E> getPredicateWrapper(
			final Function<E, V[]> valueSupplier,
			final BiPredicate<V[], Integer> predicate
		) {
			return getPredicateWrapper((entity) -> {
				V[] array = valueSupplier.apply(entity);
				boolean result = false;
				for (int i = 0; i < array.length; i++) {
					if (result = predicate.test(array, i)) {
						break;
					}
				}
				//logDebug("test for {} return {}", entity, result);
				return result;
			});
		}

		protected C logicOperation(C leftCriteria, C rightCriteria,
			Function<Predicate<E>, Function<Predicate< ? super E>, Predicate<E>>> binaryOperator,
			C targetCriteria
		) {
			targetCriteria.predicate =
				leftCriteria.predicate != null?
					(rightCriteria.predicate != null?
						binaryOperator.apply(leftCriteria.predicate).apply(rightCriteria.predicate) :
						leftCriteria.predicate):
					rightCriteria.predicate;
			return targetCriteria;
		}

		protected C newInstance() {
			return (C)Constructors.INSTANCE.newInstanceOf(this.getClass());
		}

		@SuppressWarnings("hiding")
		<E, C extends Simple<E, C>> Predicate<E> consumeLogicalOperator (
			Predicate<E> input,
			Function<Predicate<E>,
			Predicate<E>> logicalOperator
		) {
			return Optional.ofNullable(logicalOperator).map(logOp -> {
				return logicalOperator.apply(input);
			}).orElseGet(() ->
				Throwables.INSTANCE.throwException(
					"A call to and/or method is necessary before calling {} at {}",
					Thread.currentThread().getStackTrace()[10].getMethodName(),
					Thread.currentThread().getStackTrace()[11]
				)
			);
		}

		Predicate<E> getPredicateWrapper(
			Predicate<E> function
		) {
			return Optional.ofNullable(function).map(innPredWrap ->
				(Predicate<E>) (entity) -> innPredWrap.test(entity)
			).orElse(null);
		}

		private Predicate<E> getPredicate(boolean defaultResult) {
			return this.predicate != null?
					(entity) -> {
						return this.predicate.test(entity);
				} :
				(entity) -> {
					return defaultResult;
				};
		}
	}

	public static class TestContext<E, C extends Criteria<E, C, ?>> extends Context {
		private enum Elements {
			ENTITY,
			PREDICATE,
			TEST_RESULT,
			THIS_CRITERIA
		}

		protected TestContext(C criteria) {
			super();
			put(Elements.THIS_CRITERIA, criteria);
		}

		public static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> TestContext<E, C> create(C criteria) {
			return new TestContext<>(criteria);
		}

		public C getCriteria() {
			return get(Elements.THIS_CRITERIA);
		}

		public E getEntity() {
			return get(Elements.ENTITY);
		}

		public Predicate<E> getPredicate() {
			return get(Elements.PREDICATE);
		}

		public Boolean getResult() {
			return super.get(Elements.TEST_RESULT);
		}

		<T extends Criteria.TestContext<E, C>> T setEntity(E entity) {
			put(Elements.ENTITY, entity);
			return (T) this;
		}

		<T extends Criteria.TestContext<E, C>> T setPredicate(Predicate<E> predicate) {
			put(Elements.PREDICATE, predicate);
			return (T)this;
		}

		<T extends Criteria.TestContext<E, C>> T setResult(Boolean result) {
			put(Elements.TEST_RESULT, result);
			return (T) this;
		}
	}

}

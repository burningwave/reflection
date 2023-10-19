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
package org.burningwave;

import org.burningwave.function.Function;
import org.burningwave.function.Handler;
import org.burningwave.function.ThrowingBiFunction;
import org.burningwave.function.ThrowingBiPredicate;
import org.burningwave.function.ThrowingPredicate;
import org.burningwave.function.ThrowingTriPredicate;
import org.burningwave.reflection.Constructors;

@SuppressWarnings("unchecked")
public class Criteria<E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> {

	protected Function<ThrowingBiPredicate<T, E, ? extends Throwable>, ThrowingBiPredicate<T, E, ? extends Throwable>> logicalOperator;

	protected ThrowingBiPredicate<T, E, ? extends Throwable> predicate;

	public final static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> Criteria<E, C, T> of(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
		return new Criteria<E, C, T>().allThoseThatMatch(predicate);
	}

	public final static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> Criteria<E, C, T> of(final ThrowingPredicate<E, ? extends Throwable> predicate) {
		return new Criteria<E, C, T>().allThoseThatMatch(predicate);
	}

	public C allThoseThatMatch(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
		this.predicate = concat(
			this.predicate,
			new ThrowingBiPredicate<T, E, Throwable>() {
				@Override
				public boolean test(final T context, final E entity) throws Throwable {
					return predicate.test(context, entity);
				}
			}
		);
		return (C)this;
	}

	public C allThoseThatMatch(final ThrowingPredicate<E, ? extends Throwable> predicate) {
		return allThoseThatMatch(
			new ThrowingBiPredicate<T, E, Throwable>() {
				@Override
				public boolean test(final T context, final E entity) throws Throwable {
					return predicate.test(entity);
				}
			}
		) ;
	}

	public C and(){
		logicalOperator = new Function<ThrowingBiPredicate<T, E, ? extends Throwable>, ThrowingBiPredicate<T, E, ? extends Throwable>>() {
			@Override
			public ThrowingBiPredicate<T, E, ? extends Throwable> apply(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
				return Handler.and(Criteria.this.predicate, (ThrowingBiPredicate)predicate);
			}
		};
		return (C)this;
	}

	public C and(final C criteria) {
		return logicOperation(
			this.createCopy(),
			criteria.createCopy(),
			new Function<
				ThrowingBiPredicate<T, E, ? extends Throwable>,
				Function<
					ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
					ThrowingBiPredicate<T, E, ? extends Throwable>
				>
			>() {
				@Override
				public Function<
					ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
					ThrowingBiPredicate<T, E, ? extends Throwable>
				> apply(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
					return new Function<
						ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
						ThrowingBiPredicate<T, E, ? extends Throwable>
					>() {
						@Override
						public ThrowingBiPredicate<T, E, ? extends Throwable> apply (
							final ThrowingBiPredicate<? super T, ? super E, ? extends Throwable> otherPredicate
						) {
							return Handler.and(predicate, (ThrowingBiPredicate)otherPredicate);
						}
					};
				}
			},
			newInstance()
		);
	}

	public C or(){
		logicalOperator = new Function<ThrowingBiPredicate<T, E, ? extends Throwable>, ThrowingBiPredicate<T, E, ? extends Throwable>>() {
			@Override
			public ThrowingBiPredicate<T, E, ? extends Throwable> apply(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
				return Handler.or(Criteria.this.predicate, (ThrowingBiPredicate) predicate);
			}
		};
		return (C)this;
	}

	public C or(final C criteria) {
		return logicOperation(
			this.createCopy(),
			criteria.createCopy(),
			new Function<
				ThrowingBiPredicate<T, E, ? extends Throwable>,
				Function<
					ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
					ThrowingBiPredicate<T, E, ? extends Throwable>
				>
			>() {
				@Override
				public Function<
					ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
					ThrowingBiPredicate<T, E, ? extends Throwable>
				> apply(final ThrowingBiPredicate<T, E, ? extends Throwable> predicate) {
					return new Function<
						ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
						ThrowingBiPredicate<T, E, ? extends Throwable>
					>() {
						@Override
						public ThrowingBiPredicate<T, E, ? extends Throwable> apply (
							final ThrowingBiPredicate<? super T, ? super E, ? extends Throwable> otherPredicate
						) {
							return Handler.or(predicate, (ThrowingBiPredicate)otherPredicate);
						}
					};
				}
			},
			newInstance()
		);
	}

	public C negate() {
		final ThrowingBiPredicate<T, E, ? extends Throwable> predicate = this.predicate;
		this.predicate = new ThrowingBiPredicate<T, E, Throwable>() {
			@Override
			public boolean test(final T context, final E entity) throws Throwable {
				return !predicate.test(context, entity);
			}
		};
		return (C)this;
	}

	public C createCopy() {
		final C copy = newInstance();
		copy.predicate = this.predicate;
		copy.logicalOperator = this.logicalOperator;
		return copy;
	}


	public ThrowingPredicate<E, ? extends Throwable> getPredicateOrFalsePredicateIfPredicateIsNull() {
		return getPredicate(createTestContext(), false);
	}

	public ThrowingPredicate<E, ? extends Throwable> getPredicateOrTruePredicateIfPredicateIsNull() {
		return getPredicate(createTestContext(), true);
	}

	public boolean hasNoPredicate() {
		return this.predicate == null;
	}

	public T testWithFalseResultForNullEntityOrFalseResultForNullPredicate(final E entity) {
		final T context = createTestContext();
		testWithFalseResultForNullEntityOrFalseResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithFalseResultForNullEntityOrTrueResultForNullPredicate(final E entity) {
		final T context = createTestContext();
		testWithFalseResultForNullEntityOrTrueResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithTrueResultForNullEntityOrFalseResultForNullPredicate(final E entity) {
		final T context = createTestContext();
		testWithTrueResultForNullEntityOrFalseResultForNullPredicate(context, entity);
		return context;
	}

	public T testWithTrueResultForNullEntityOrTrueResultForNullPredicate(final E entity) {
		final T context = createTestContext();
		testWithTrueResultForNullEntityOrTrueResultForNullPredicate(context, entity);
		return context;
	}

	protected ThrowingBiPredicate<T, E, ? extends Throwable> concat(
		final ThrowingBiPredicate<T, E, ? extends Throwable> mainPredicate,
		final ThrowingBiPredicate<T, E, ? extends Throwable> otherPredicate
	) {
		final ThrowingBiPredicate<T, E, ? extends Throwable> predicate = concat(mainPredicate, this.logicalOperator, otherPredicate);
		this.logicalOperator = null;
		return predicate;
	}

	protected <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> ThrowingBiPredicate<T, E, ? extends Throwable> concat(
		final ThrowingBiPredicate<T, E, ? extends Throwable> mainPredicate,
		final Function<ThrowingBiPredicate<T, E, ? extends Throwable>, ThrowingBiPredicate<T, E, ? extends Throwable>> logicalOperator,
		final ThrowingBiPredicate<T, E, ? extends Throwable> otherPredicate
	) {
		if (otherPredicate != null) {
			if (mainPredicate != null) {
				return consumeLogicalOperator(otherPredicate, logicalOperator);
			}
			return otherPredicate;
		}
		return mainPredicate;
	}


	protected T createTestContext() {
		return (T)TestContext.<E, C, T>create((C)this);
	}

	protected T getContextWithFalsePredicateForNullPredicate() {
		final T context = createTestContext();
		getPredicate(context, false);
		return context;
	}

	protected T getContextWithTruePredicateForNullPredicate() {
		final T context = createTestContext();
		getPredicate(context, true);
		return context;
	}

	protected <V> ThrowingBiPredicate<T, E, ? extends Throwable> getPredicateWrapper(
		final ThrowingBiFunction<T, E, V[], ? extends Throwable> valueSupplier,
		final ThrowingTriPredicate<T, V[], Integer, ? extends Throwable> predicate
	) {
		return getPredicateWrapper(
			new ThrowingBiPredicate<T, E, Throwable>() {
				@Override
				public boolean test(final T criteria, final E entity) throws Throwable {
					final V[] array = valueSupplier.apply(criteria, entity);
					boolean result = false;
					for (int i = 0; i < array.length; i++) {
						if (result = predicate.test(criteria, array, i)) {
							break;
						}
					}
					//logDebug("test for {} return {}", entity, result);
					return result;
				}
			}
		);
	}

	protected C logicOperation(final C leftCriteria, final C rightCriteria,
		final Function<
			ThrowingBiPredicate<T, E, ? extends Throwable>,
			Function<
				ThrowingBiPredicate<? super T, ? super E, ? extends Throwable>,
				ThrowingBiPredicate<T, E, ? extends Throwable>
			>
		> binaryOperator,
		final C targetCriteria
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
	<E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> ThrowingBiPredicate<T,E, ? extends Throwable> consumeLogicalOperator(
		final ThrowingBiPredicate<T, E, ? extends Throwable> input,
		final Function<
			ThrowingBiPredicate<T, E, ? extends Throwable>,
			ThrowingBiPredicate<T, E, ? extends Throwable>
		> logicalOperator
	) {
		if (logicalOperator != null) {
			return logicalOperator.apply(input);
		}
		return Throwables.INSTANCE.throwException(
			"A call to and/or method is necessary before calling {} at {}",
			Thread.currentThread().getStackTrace()[10].getMethodName(),
			Thread.currentThread().getStackTrace()[11]
		);
	}

	ThrowingBiPredicate<T, E, ? extends Throwable> getPredicateWrapper(
		final ThrowingBiPredicate<T, E, ? extends Throwable> function
	) {
		if (function != null) {
			return new ThrowingBiPredicate<T, E, Throwable>() {
				@Override
				public boolean test(final T criteria, final E entity) throws Throwable {
					return function.test(criteria, entity);
				}
			};
		}
		return null;
	}

	private ThrowingPredicate<E, ? extends Throwable> getPredicate(final T context, final boolean defaultResult) {
		return context.setPredicate(
			this.predicate != null?
				new ThrowingPredicate<E, Throwable>() {
					@Override
					public boolean test(final E entity) throws Throwable {
						return context.setEntity(entity).setResult(Criteria.this.predicate.test(
							context,
							entity
						)).getResult();
					}
				} :
			new ThrowingPredicate<E, Throwable>() {
				@Override
				public boolean test(final E entity) {
					return context.setEntity(entity).setResult(defaultResult).getResult();
				}
			}
		).getPredicate();
	}


	private boolean testWithFalseResultForNullEntityOrFalseResultForNullPredicate(final T context, final E entity) {
		if (entity != null) {
			try {
				return getPredicate(context, false).test(entity);
			} catch (final Throwable exc) {
				Throwables.INSTANCE.throwException(exc);
			}
		}
		return context.setEntity(entity).setResult(false).getResult();
	}


	private boolean testWithFalseResultForNullEntityOrTrueResultForNullPredicate(final T context, final E entity) {
		if (entity != null) {
			try {
				return getPredicate(context, true).test(entity);
			} catch (final Throwable exc) {
				Throwables.INSTANCE.throwException(exc);
			}
		}
		return context.setEntity(entity).setResult(false).getResult();
	}


	private boolean testWithTrueResultForNullEntityOrFalseResultForNullPredicate(final T context, final E entity) {
		if (entity != null) {
			try {
				return getPredicate(context, false).test(entity);
			} catch (final Throwable exc) {
				Throwables.INSTANCE.throwException(exc);
			}
		}
		return context.setEntity(entity).setResult(true).getResult();
	}

	private boolean testWithTrueResultForNullEntityOrTrueResultForNullPredicate(final T context, final E entity) {
		if (entity != null) {
			try {
				return getPredicate(context, true).test(entity);
			} catch (final Throwable exc) {
				Throwables.INSTANCE.throwException(exc);
			}
		}
		return context.setEntity(entity).setResult(true).getResult();
	}

	public static class Simple<E, C extends Simple<E, C>> {

		protected Function<ThrowingPredicate<E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>> logicalOperator;
		protected ThrowingPredicate<E, ? extends Throwable> predicate;

		public C allThoseThatMatch(final ThrowingPredicate<E, ? extends Throwable> predicate) {
			this.predicate = concat(
				this.predicate,
				new ThrowingPredicate<E,Throwable>() {
					@Override
					public boolean test(final E entity) throws Throwable {
						return predicate.test(entity);
					}
				}
			);
			return (C)this;
		}

		public C and(){
			logicalOperator = new Function<ThrowingPredicate<E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>>() {
				@Override
				public ThrowingPredicate<E, ? extends Throwable> apply(final ThrowingPredicate<E, ? extends Throwable> predicate) {
					return Handler.and(Simple.this.predicate, (ThrowingPredicate)predicate);
				}
			};
			return (C)this;
		}

		public C and(final C criteria) {
			return logicOperation(this.createCopy(), criteria.createCopy(),
					new Function<ThrowingPredicate<E, ? extends Throwable>,
					Function<ThrowingPredicate<? super E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>>>() {
				@Override
				public Function<ThrowingPredicate<? super E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>> apply(final ThrowingPredicate<E, ? extends Throwable> predicate) {
					return new Function<
						ThrowingPredicate<? super E, ? extends Throwable>,
						ThrowingPredicate<E, ? extends Throwable>
					>() {
						@Override
						public ThrowingPredicate<E, ? extends Throwable> apply(
							final ThrowingPredicate<? super E, ? extends Throwable> otherPredicate
						) {
							return Handler.and(predicate, (ThrowingPredicate) otherPredicate);
						}

					};
				}
			}, newInstance());
		}

		public C or(){
			logicalOperator = new Function<ThrowingPredicate<E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>>() {
				@Override
				public ThrowingPredicate<E, ? extends Throwable> apply(final ThrowingPredicate<E, ? extends Throwable> predicate) {
					return Handler.or(Simple.this.predicate, (ThrowingPredicate)predicate);
				}
			};
			return (C)this;
		}

		public C or(final C criteria) {
			return logicOperation(
				this.createCopy(), criteria.createCopy(),
				new Function<ThrowingPredicate<E, ? extends Throwable>, Function<ThrowingPredicate<? super E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>>>() {
					@Override
					public Function<ThrowingPredicate<? super E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>> apply(final ThrowingPredicate<E, ? extends Throwable> predicate) {
						return new Function<
							ThrowingPredicate<? super E, ? extends Throwable>,
							ThrowingPredicate<E, ? extends Throwable>
						>() {
							@Override
							public ThrowingPredicate<E, ? extends Throwable> apply(
								final ThrowingPredicate<? super E, ? extends Throwable> otherPredicate
							) {
								return Handler.or(predicate, (ThrowingPredicate)otherPredicate);
							}
						};
					}
				},
				newInstance()
			);
		}

		public C negate() {
			final ThrowingPredicate<E, ? extends Throwable> predicate = this.predicate;
			this.predicate = new ThrowingPredicate<E, Throwable>() {

				@Override
				public boolean test(E entity) throws Throwable {
					return !predicate.test(entity);
				}

			};
			return (C)this;
		}

		public C createCopy() {
			final C copy = newInstance();
			copy.predicate = this.predicate;
			copy.logicalOperator = this.logicalOperator;
			return copy;
		}

		public ThrowingPredicate<E, ? extends Throwable> getPredicateOrFalsePredicateIfPredicateIsNull() {
			return getPredicate(false);
		}

		public ThrowingPredicate<E, ? extends Throwable> getPredicateOrTruePredicateIfPredicateIsNull() {
			return getPredicate(true);
		}

		public boolean hasNoPredicate() {
			return this.predicate == null;
		}

		public boolean testWithFalseResultForNullEntityOrFalseResultForNullPredicate(final E entity) {
			if (entity != null) {
				try {
					return getPredicateOrFalsePredicateIfPredicateIsNull().test(entity);
				} catch (final Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}
			return false;
		}

		public boolean testWithFalseResultForNullEntityOrTrueResultForNullPredicate(final E entity) {
			if (entity != null) {
				try {
					return getPredicateOrTruePredicateIfPredicateIsNull().test(entity);
				} catch (final Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}
			return false;
		}

		public boolean testWithFalseResultForNullPredicate(final E entity) {
			try {
				return getPredicateOrFalsePredicateIfPredicateIsNull().test(entity);
			} catch (final Throwable exc) {
				return Throwables.INSTANCE.throwException(exc);
			}
		}

		public boolean testWithTrueResultForNullEntityOrFalseResultForNullPredicate(final E entity) {
			if (entity != null) {
				try {
					return getPredicateOrFalsePredicateIfPredicateIsNull().test(entity);
				} catch (final Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}
			return true;
		}

		public boolean testWithTrueResultForNullEntityOrTrueResultForNullPredicate(final E entity) {
			if (entity != null) {
				try {
					return getPredicateOrTruePredicateIfPredicateIsNull().test(entity);
				} catch (final Throwable exc) {
					return Throwables.INSTANCE.throwException(exc);
				}
			}
			return true;
		}

		public boolean testWithTrueResultForNullPredicate(final E entity) {
			try {
				return getPredicateOrTruePredicateIfPredicateIsNull().test(entity);
			} catch (final Throwable exc) {
				return Throwables.INSTANCE.throwException(exc);
			}
		}


		protected <E, C extends Simple<E, C>> ThrowingPredicate<E, ? extends Throwable> concat(
			final ThrowingPredicate<E, ? extends Throwable> mainPredicate,
			final Function<ThrowingPredicate<E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>> logicalOperator,
			final ThrowingPredicate<E, ? extends Throwable> otherPredicate
		) {
			if (otherPredicate != null) {
				if (mainPredicate != null) {
					return consumeLogicalOperator(otherPredicate, logicalOperator);
				}
				return otherPredicate;
			}
			return mainPredicate;
		}

		protected ThrowingPredicate<E, ? extends Throwable> concat(
			final ThrowingPredicate<E, ? extends Throwable> mainPredicate,
			final ThrowingPredicate<E, ? extends Throwable> otherPredicate
		) {
			final ThrowingPredicate<E, ? extends Throwable> predicate = concat(mainPredicate, this.logicalOperator, otherPredicate);
			this.logicalOperator = null;
			return predicate;
		}

		protected <V> ThrowingPredicate<E, ? extends Throwable> getPredicateWrapper(
			final Function<E, V[]> valueSupplier,
			final ThrowingBiPredicate<V[], Integer, ? extends Throwable> predicate
		) {
			return getPredicateWrapper(new ThrowingPredicate<E, Throwable>() {
				@Override
				public boolean test(final E entity) throws Throwable {
					final V[] array = valueSupplier.apply(entity);
					boolean result = false;
					for (int i = 0; i < array.length; i++) {
						if (result = predicate.test(array, i)) {
							break;
						}
					}
					//logDebug("test for {} return {}", entity, result);
					return result;
				}
			});
		}

		protected C logicOperation(final C leftCriteria, final C rightCriteria,
			final Function<ThrowingPredicate<E, ? extends Throwable>, Function<ThrowingPredicate< ? super E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>>> binaryOperator,
			final C targetCriteria
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
		<E, C extends Simple<E, C>> ThrowingPredicate<E, ? extends Throwable> consumeLogicalOperator (
			final ThrowingPredicate<E, ? extends Throwable> input,
			final Function<ThrowingPredicate<E, ? extends Throwable>, ThrowingPredicate<E, ? extends Throwable>> logicalOperator
		) {
			if (logicalOperator != null) {
				return logicalOperator.apply(input);
			}
			return Throwables.INSTANCE.throwException(
				"A call to and/or method is necessary before calling {} at {}",
				Thread.currentThread().getStackTrace()[10].getMethodName(),
				Thread.currentThread().getStackTrace()[11]
			);
		}

		ThrowingPredicate<E, ? extends Throwable> getPredicateWrapper(
			final ThrowingPredicate<E, ? extends Throwable> function
		) {
			if (function != null) {
				return new ThrowingPredicate<E, Throwable>() {
					@Override
					public boolean test(final E entity) throws Throwable {
						return function.test(entity);
					}
				};
			}
			return null;
		}

		private ThrowingPredicate<E, ? extends Throwable> getPredicate(final boolean defaultResult) {
			return this.predicate != null?
				new ThrowingPredicate<E, Throwable>() {
					@Override
					public boolean test(final E entity) throws Throwable {
						return Simple.this.predicate.test(entity);
					}
				} :
				new ThrowingPredicate<E, Throwable>() {
					@Override
					public boolean test(final E entity) {
						return defaultResult;
					}
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

		protected TestContext(final C criteria) {
			super();
			put(Elements.THIS_CRITERIA, criteria);
		}

		public static <E, C extends Criteria<E, C, T>, T extends Criteria.TestContext<E, C>> TestContext<E, C> create(final C criteria) {
			return new TestContext<>(criteria);
		}

		public C getCriteria() {
			return get(Elements.THIS_CRITERIA);
		}

		public E getEntity() {
			return get(Elements.ENTITY);
		}

		public ThrowingPredicate<E, ? extends Throwable> getPredicate() {
			return get(Elements.PREDICATE);
		}

		public Boolean getResult() {
			return super.get(Elements.TEST_RESULT);
		}

		<T extends Criteria.TestContext<E, C>> T setEntity(final E entity) {
			put(Elements.ENTITY, entity);
			return (T) this;
		}

		<T extends Criteria.TestContext<E, C>> T setPredicate(final ThrowingPredicate<E, ? extends Throwable> predicate) {
			put(Elements.PREDICATE, predicate);
			return (T)this;
		}

		<T extends Criteria.TestContext<E, C>> T setResult(final Boolean result) {
			put(Elements.TEST_RESULT, result);
			return (T) this;
		}
	}

}

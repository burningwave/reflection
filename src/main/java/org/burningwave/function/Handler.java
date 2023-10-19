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
package org.burningwave.function;

import org.burningwave.Throwables;

@SuppressWarnings("unchecked")
public abstract class Handler {

	public static <T, E extends Throwable> T get(final ThrowingSupplier<T, ? extends E> supplier) {
		try {
			return supplier.get();
		} catch (final Throwable exc) {
			return Throwables.INSTANCE.throwException(exc);
		}
	}

	public static <T, E extends Throwable> T getFirst(final ThrowingSupplier<T, ? extends E>... suppliers) {
		Throwable exception = null;
		for (final ThrowingSupplier<T, ? extends E> supplier : suppliers) {
			try {
				return supplier.get();
			} catch (final Throwable exc) {
				exception = exc;
			}
		}
		return Throwables.INSTANCE.throwException(exception);
	}

    public static <P0, P1, P2, E extends Throwable> ThrowingTriPredicate<P0, P1, P2, E> and(
		final ThrowingTriPredicate<P0, P1, P2, E> left, final ThrowingTriPredicate<? super P0, ? super P1, ? super P2, ? extends E> right
	) {
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(final P0 p0, final P1 p1, final P2 p2) throws E {
				return left.test(p0, p1, p2) && right.test(p0, p1, p2);
			}
		};
    }

    public static <P0, P1, P2, E extends Throwable> ThrowingTriPredicate<P0, P1, P2, E> negate(final ThrowingTriPredicate<P0, P1, P2, E> predicate) {
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(final P0 p0, final P1 p1, final P2 p2) throws E {
				return !predicate.test(p0, p1, p2);
			}
		};
    }

    public static <P0, P1, P2, E extends Throwable> ThrowingTriPredicate<P0, P1, P2, E> or(
		final ThrowingTriPredicate<P0, P1, P2, E> left, final ThrowingTriPredicate<? super P0, ? super P1, ? super P2, ? extends E> right
	) {
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(final P0 p0, final P1 p1, final P2 p2) throws E {
				return left.test(p0, p1, p2) || right.test(p0, p1, p2);
			}
		};
    }

    public static <T, E extends Throwable> ThrowingPredicate<T, E> and(
    	final ThrowingPredicate<T, E> left,
		final ThrowingPredicate<? super T, ? extends E> right
	) {
        return new ThrowingPredicate<T, E>() {
			@Override
			public boolean test(final T t) throws E {
				return left.test(t) && right.test(t);
			}
		};
    }

    public static <T, E extends Throwable> ThrowingPredicate<T, E> negate(final ThrowingPredicate<T, E> predicate) {
        return new ThrowingPredicate<T, E>() {
			@Override
			public boolean test(final T t) throws E {
				return !predicate.test(t);
			}
		};
    }

    public static <T, E extends Throwable> ThrowingPredicate<T, E> or(
    	final ThrowingPredicate<T, E> left,
		final ThrowingPredicate<? super T, ? extends E> right
	) {
        return new ThrowingPredicate<T, E>() {
			@Override
			public boolean test(final T t) throws E {
				return left.test(t) || right.test(t);
			}
		};
    }

    public static <T, U, E extends Throwable> ThrowingBiPredicate<T, U, E> and(
		final ThrowingBiPredicate<T, U, E> left,
		final ThrowingBiPredicate<? super T, ? super U, ? extends E> right
	) {
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(final T t, final U u) throws E {
				return left.test(t, u) && right.test(t, u);
			}
		};
    }

    public static <T, U, E extends Throwable>  ThrowingBiPredicate<T, U, E> negate(
		final ThrowingBiPredicate<T, U, E> predicate
	) {
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(final T t, final U u) throws E {
				return !predicate.test(t, u);
			}
		};
    }

    public static <T, U, E extends Throwable> ThrowingBiPredicate<T, U, E> or(
		final ThrowingBiPredicate<T, U, E> left,
		final ThrowingBiPredicate<? super T, ? super U, ? extends E> right
	) {
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(final T t, final U u) throws E {
				return left.test(t, u) || right.test(t, u);
			}
		};
    }

}
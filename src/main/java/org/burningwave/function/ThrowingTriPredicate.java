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
package org.burningwave.function;


import java.util.Objects;


@FunctionalInterface
public interface ThrowingTriPredicate<P0, P1, P2, E extends Throwable> {

    boolean test(P0 p0, P1 p1, P2 p2) throws Throwable;

    default ThrowingTriPredicate<P0, P1, P2, E> and(ThrowingTriPredicate<? super P0, ? super P1, ? super P2, ? extends E> other) {
        Objects.requireNonNull(other);
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(P0 p0, P1 p1, P2 p2) throws Throwable {
				return ThrowingTriPredicate.this.test(p0, p1, p2) && other.test(p0, p1, p2);
			}
		};
    }

    default ThrowingTriPredicate<P0, P1, P2, E> negate() {
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(P0 p0, P1 p1, P2 p2) throws Throwable {
				return !ThrowingTriPredicate.this.test(p0, p1, p2);
			}
		};
    }

    default ThrowingTriPredicate<P0, P1, P2, E> or(ThrowingTriPredicate<? super P0, ? super P1, ? super P2, ? extends E> other) {
        Objects.requireNonNull(other);
        return new ThrowingTriPredicate<P0, P1, P2, E>() {
			@Override
			public boolean test(P0 p0, P1 p1, P2 p2) throws Throwable {
				return ThrowingTriPredicate.this.test(p0, p1, p2) || other.test(p0, p1, p2);
			}
		};
    }

}

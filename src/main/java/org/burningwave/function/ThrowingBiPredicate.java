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


public interface ThrowingBiPredicate<T, U, E extends Throwable> {


    boolean test(T t, U u) throws E;

    default ThrowingBiPredicate<T, U, E> and(ThrowingBiPredicate<? super T, ? super U, ? extends E> other) {
        Objects.requireNonNull(other);
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(T t, U u) throws E {
				return ThrowingBiPredicate.this.test(t, u) && other.test(t, u);
			}
		};
    }

    default ThrowingBiPredicate<T, U, E> negate() {
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(T t, U u) throws E {
				return !ThrowingBiPredicate.this.test(t, u);
			}
		};
    }

    default ThrowingBiPredicate<T, U, E> or(ThrowingBiPredicate<? super T, ? super U, ? extends E> other) {
        Objects.requireNonNull(other);
        return new ThrowingBiPredicate<T, U, E>() {
			@Override
			public boolean test(T t, U u) throws E {
				return ThrowingBiPredicate.this.test(t, u) || other.test(t, u);
			}
		};
    }
}
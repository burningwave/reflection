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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class Throwables {
	public static final Throwables INSTANCE;
	private final Thrower thrower;
	static {
		try {
			INSTANCE  = new Throwables();
		} catch (Throwable exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
	}

	private Throwables() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Constructor<?> constructor =
			Class.forName(Thrower.class.getName() + "Impl").getDeclaredConstructor();
		this.thrower = (Thrower)constructor.newInstance();
	}

	public <T> T throwException(final String exc, final Object... placeHolderReplacements) {
        return throwException(
    		new Exception(Strings.INSTANCE.compile(exc, placeHolderReplacements))
		);
    }

	public <T> T throwException(final Throwable exc) {
        return thrower.throwException(exc);
    }

    static abstract class Thrower {

    	Thrower(){}

    	abstract <T> T throwException(final Throwable exc);

    }

    /*static class ThrowerImpl extends Thrower {
    	ThrowerImpl(){}

    	@Override
		<T> T throwException(final Throwable exc) {
    		sneakyThrow(exc);
            return null;
        }

        private <E extends Throwable> void sneakyThrow(final Throwable exc) throws E {
            throw (E)exc;
        }
    }*/
}
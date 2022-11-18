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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class Synchronizer {
	public class Mutex implements java.io.Closeable {
		int clientsCount = 1;
		String id;
		Mutex(String id) {
			this.id = id;
		}

		@Override
		public void close() {
			if (--clientsCount < 1) {
				Synchronizer.this.mutexes.remove(id);
			}
		}
	}

	public final static Synchronizer INSTANCE;

	static {
		INSTANCE = new Synchronizer();
	}


	Map<String, Mutex> mutexes;


	private Synchronizer() {
		mutexes = new ConcurrentHashMap<>();
	}

	public void execute(String id, Runnable executable) {
		try (Mutex mutex = getMutex(id);) {
			synchronized (mutex) {
				executable.run();
			}
		}
	}

	public <T> T execute(String id, Supplier<T> executable) {
		try (Mutex mutex = getMutex(id);) {
			synchronized (mutex) {
				return executable.get();
			}
		}
	}

	public Mutex getMutex(String id) {
		Mutex newMutex = this.new Mutex(id);
		while (true) {
			Mutex oldMutex = mutexes.putIfAbsent(id, newMutex);
	        if (oldMutex == null) {
		        return newMutex;
	        }
	        if (++oldMutex.clientsCount > 1 && mutexes.get(id) == oldMutex) {
	        	return oldMutex;
        	}
        	//logWarn("Unvalid mutex with id \"{}\": a new mutex will be created", id);
        	continue;
		}
    }

}

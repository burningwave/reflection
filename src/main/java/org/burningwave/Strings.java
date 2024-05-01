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


import java.util.Arrays;
import java.util.Collection;

import org.burningwave.function.Function;

public class Strings {
	public final static Strings INSTANCE;

	static {
		INSTANCE = new Strings();
	}

	private Strings() {}

	public String capitalizeFirstCharacter(String value) {
		return Character.toString(value.charAt(0)).toUpperCase()
		+ value.substring(1, value.length());
	}

	public String compile(String message, Object... arguments) {
		for (Object obj : arguments) {
			message = message.replaceFirst("\\{\\}", obj == null ? "null" : clear(obj.toString()));
		}
		return message;
	}

	public <T> String join(String separator, Objects... objects) {
		return join(separator, Arrays.asList(objects), null);
	}

	public <T> String join(String separator, String... objects) {
		return join(separator, Arrays.asList(objects), null);
	}

	public <T> String join(String separator, Collection<T> objects) {
		return join(separator, objects, null);
	}

	public <T> String join(String separator, Collection<T> objects, Function<T, String> objectProcessor) {
		StringBuffer joiner = new StringBuffer();
		for (T object : objects) {
			if (object != null) {
				if (objectProcessor != null) {
					joiner.append(objectProcessor.apply(object));
				} else {
					joiner.append(object);
				}
			} else {
				joiner.append("null");
			}
			joiner.append(separator);
		}
		String joined = joiner.toString();
		return separator.length() > 0 && joined.length() > 0 ?
			joined.substring(0, joined.length() - separator.length()):
			joined;
	}

	public <T> String join(String separator, T[] argumentTypes, Function<Class<?>, String> function) {
		return join(separator, Arrays.asList(argumentTypes));
	}

	private String clear(String text) {
		return text
		.replace("\\", "\\\\\\")
		.replace("{", "\\{")
		.replace("}", "\\}")
		.replace("(", "\\(")
		.replace(")", "\\)")
		.replace(".", "\\.")
		.replace("$", "\\$");
	}

}

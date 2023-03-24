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


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.burningwave.Throwables;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingFunction;

import io.github.toolfactory.jvm.function.template.ThrowingBiFunction;

@SuppressWarnings("unchecked")
public abstract class FieldAccessor {

	public final static FieldAccessor INSTANCE;

	private final static String REG_EXP_FOR_INDEXES_OF_INDEXED_FIELDS = "\\[(.*?)\\]";
	private final static String REG_EXP_FOR_SIMPLE_FIELDS = "([a-zA-Z\\$\\_\\-0-9]*)(\\[*.*)";

	static {
		INSTANCE = new ByFieldOrByMethod();
	}
	private List<ThrowingBiFunction<Object, String, Object, Throwable>> fieldRetrievers;
	private List<ThrowingFunction<Object[], Boolean, Throwable>> fieldSetters;
	private Pattern indexesSearcherForIndexedField;

	private Pattern simpleFieldSearcher;

	FieldAccessor() {
		this.fieldRetrievers = getFieldRetrievers();
		this.fieldSetters= getFieldSetters();
		this.simpleFieldSearcher = Pattern.compile(REG_EXP_FOR_SIMPLE_FIELDS);
		this.indexesSearcherForIndexedField = Pattern.compile(REG_EXP_FOR_INDEXES_OF_INDEXED_FIELDS);
	}

	public <T> T get(final Object obj, final String path) {
		if (path == null) {
			throw new IllegalArgumentException("Field path cannot be null");
		}
		final String[] pathSegments = path.split("\\.");
		Object objToReturn = obj;
		for (int j = 0; j < pathSegments.length; j++) {
			objToReturn = getField(j != 0 ? objToReturn : obj, pathSegments[j]);
		}
		return (T)objToReturn;
	}

	public void set(final Object obj, final String path, final Object value) {
		if (path == null) {
			throw new IllegalArgumentException("Field path cannot be null");
		}
		if (path.trim().isEmpty()) {
			return;
		}
		final Object target =
				path.contains(".")?
						get(obj, path.substring(0, path.lastIndexOf("."))) :
							obj;
		final String targetPathSegment =
				path.contains(".")?
						path.substring(path.lastIndexOf(".") + 1, path.length()) :
							path;
		setField(target, targetPathSegment, value);
	}

	abstract List<ThrowingBiFunction<Object, String, Object, Throwable>> getFieldRetrievers();

	abstract List<ThrowingFunction<Object[], Boolean, Throwable>> getFieldSetters();

	Object retrieveFieldByDirectAccess(final Object target, final String pathSegment) throws IllegalAccessException {
		if (pathSegment.trim().isEmpty()) {
			return target;
		}
		return Fields.INSTANCE.get(target, pathSegment);
	}

	Boolean setFieldByDirectAccess(final Object target, final String pathSegment, final Object value) throws IllegalAccessException {
		final Matcher matcher = simpleFieldSearcher.matcher(pathSegment);
		matcher.find();
		if (matcher.group(2).isEmpty()) {
			final Field field = Fields.INSTANCE.findOneAndMakeItAccessible(target.getClass(), matcher.group(1));
			Fields.INSTANCE.set(target, field, value);
		} else {
			if (target.getClass().isArray() || (target instanceof Map) || (target instanceof Collection)) {
				setInIndexedField(target, matcher.group(2), value);
			} else {
				final Field field = Fields.INSTANCE.findOneAndMakeItAccessible(target.getClass(), matcher.group(1));
				setInIndexedField(field.get(target), matcher.group(2), value);
			}
		}
		return Boolean.TRUE;
	}


	private <T> int convertAndCheckIndex(final Collection<T> collection, final String indexAsString) {
		final int index = Integer.valueOf(indexAsString);
		if (collection.size() < index) {
			throw new IndexOutOfBoundsException(("Illegal index "+ indexAsString +", collection size " + collection.size()));
		}
		return index;
	}

	private Object getField(final Object obj, final String pathSegment) {
		Object objToReturn = null;
		final Matcher matcher = simpleFieldSearcher.matcher(pathSegment);
		matcher.find();
		final List<Throwable> exceptions = new ArrayList<>();
		for (final ThrowingBiFunction<Object, String, Object, Throwable> retriever : fieldRetrievers) {
			try {
				if ((objToReturn = retriever.apply(obj, matcher.group(1))) != null) {
					break;
				}
			} catch (final Throwable exc) {
				exceptions.add(exc);
			}
		}
		manageGetFieldExceptions(exceptions);
		if (!matcher.group(2).isEmpty()) {
			try {
				objToReturn = retrieveFromIndexedField(objToReturn, matcher.group(2));
			} catch (final Throwable exc) {
				exceptions.add(exc);
			}
		}
		return objToReturn;
	}

	private void manageGetFieldExceptions(final List<Throwable> exceptions) {
		if (exceptions.size() == fieldRetrievers.size()) {
			Throwables.INSTANCE.throwException(exceptions.iterator().next());
		}
	}

	private <T> Object retrieveFromIndexedField(final Object fieldValue, final String indexes) {
		final Matcher matcher = indexesSearcherForIndexedField.matcher(indexes);
		if (matcher.find()) {
			final String index = matcher.group(1);
			Supplier<Object> propertyRetriever = null;
			if (fieldValue.getClass().isArray()) {
				propertyRetriever = new Supplier<Object>() {
					@Override
					public Object get() {
						return Array.get(fieldValue, Integer.valueOf(index));
					}
				};
			} else if (fieldValue instanceof List) {
				propertyRetriever = new Supplier<Object>() {
					@Override
					public Object get() {
						return ((List<?>)fieldValue).get(Integer.valueOf(index));
					}
				};
			} else if (fieldValue instanceof Map) {
				propertyRetriever = new Supplier<Object>() {
					@Override
					public Object get() {
						return ((Map<?, ?>)fieldValue).get(index);
					}
				};
			} else if (fieldValue instanceof Collection) {
				propertyRetriever = new Supplier<Object>() {
					@Override
					public Object get() {
						final Collection<T> collection = (Collection<T>)fieldValue;
						final int indexAsInt = convertAndCheckIndex(collection, index);
						final Iterator<T> itr = collection.iterator();
						int currentIterationIndex = 0;
						while (itr.hasNext()) {
							final Object currentIteartedObject = itr.next();
							if (currentIterationIndex++ == indexAsInt) {
								return currentIteartedObject;
							}
						}
						return null;
					}
				};
			} else {
				return Throwables.INSTANCE.throwException("indexed property {} of type {} is not supporterd", fieldValue, fieldValue.getClass());
			}
			return retrieveFromIndexedField(
				propertyRetriever.get(),
				indexes.substring(matcher.end(), indexes.length())
			);
		}
		return fieldValue;
	}

	private void setField(final Object target, final String pathSegment, final Object value) {
		final List<Throwable> exceptions = new ArrayList<>();
		for (final ThrowingFunction<Object[], Boolean, Throwable> setter : fieldSetters) {
			try {
				setter.apply(new Object[] {target, pathSegment, value});
				break;
			} catch (final Throwable exc) {
				exceptions.add(exc);
			}
		}
		manageGetFieldExceptions(exceptions);
	}

	private <T> void setIndexedValue(final Collection<T> collection, final String index, final Object value) {
		final int indexAsInt = convertAndCheckIndex(collection, index);
		final List<T> tempList = new ArrayList<>();
		Iterator<T> itr = collection.iterator();
		while (itr.hasNext()) {
			tempList.add(itr.next());
		}
		int iterationIndex = 0;
		collection.clear();
		itr = tempList.iterator();
		while (itr.hasNext()) {
			final T origVal = itr.next();
			if (iterationIndex++ != indexAsInt) {
				collection.add(origVal);
			} else {
				collection.add((T)value);
			}
		}
	}

	private <T> void setInIndexedField(final Object fieldValue, final String indexes, final Object value) {
		final Matcher matcher = indexesSearcherForIndexedField.matcher(indexes);
		int lastIndexOf = 0;
		String index = null;
		while (matcher.find()) {
			index = matcher.group(1);
			lastIndexOf = matcher.start();
		}
		final Object targetObject = retrieveFromIndexedField(fieldValue, indexes.substring(0, lastIndexOf));
		if (targetObject.getClass().isArray()) {
			Array.set(targetObject, Integer.valueOf(index), value);
		} else if (targetObject instanceof List) {
			((List<T>)targetObject).set(Integer.valueOf(index), (T)value);
		} else if (targetObject instanceof Map) {
			((Map<String, T>)targetObject).put(index, (T)value);
		} else if (targetObject instanceof Collection) {
			setIndexedValue((Collection<T>) targetObject, index, value);
		} else {
			Throwables.INSTANCE.throwException("indexed property {} of type {} is not supporterd", fieldValue, fieldValue.getClass());
		}
	}

	private static class ByFieldOrByMethod extends FieldAccessor {

		private ByFieldOrByMethod() {
			super();
		}

		@Override
		List<ThrowingBiFunction<Object, String, Object, Throwable>> getFieldRetrievers() {
			final List<ThrowingBiFunction<Object, String, Object, Throwable>> retrievers = new ArrayList<>();
			retrievers.add(new ThrowingBiFunction<Object, String, Object, Throwable>() {
				@Override
				public Object apply(final Object object, final String pathSegment) throws Throwable {
					return retrieveFieldByDirectAccess(object, pathSegment);
				}
			});
			return retrievers;
		}

		@Override
		List<ThrowingFunction<Object[], Boolean, Throwable>> getFieldSetters() {
			final List<ThrowingFunction<Object[], Boolean, Throwable>> setters  = new ArrayList<>();
			setters.add(new ThrowingFunction<Object[], Boolean, Throwable>() {
				@Override
				public Boolean apply(final Object[] objects) throws Throwable {
					return setFieldByDirectAccess(objects[0], (String)objects[1], objects[2]);
				}
			});
			return setters;
		}
	}

}
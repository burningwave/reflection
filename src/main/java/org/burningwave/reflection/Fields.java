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


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.burningwave.Throwables;

import io.github.toolfactory.jvm.util.Strings;


public class Fields extends Members.Handler<Field, FieldCriteria> {

	public static final Fields INSTANCE;

	static {
		INSTANCE = new Fields();
	}

	private Fields() {}

	public Collection<Field> findAllAndMakeThemAccessible(
		Class<?> targetClass
	) {
		String cacheKey = getCacheKey(targetClass, "all fields", (Class<?>[])null);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForFields.getOrUploadIfAbsent(
			targetClassClassLoader,
			cacheKey,
			() ->
				findAllAndMakeThemAccessible(
					FieldCriteria.forEntireClassHierarchy(), targetClass
				)
		);
	}

	public Collection<Field> findAllByExactNameAndMakeThemAccessible(
		Class<?> targetClass,
		String fieldName
	) {
		return findAllByExactNameAndMakeThemAccessible(targetClass, fieldName, null);
	}

	public Collection<Field> findAllByExactNameAndMakeThemAccessible(
		Class<?> targetClass,
		String fieldName,
		Class<?> valueType
	) {
		String cacheKey = getCacheKey(targetClass, "equals " + fieldName, valueType);
		ClassLoader targetClassClassLoader = Classes.INSTANCE.getClassLoader(targetClass);
		return Cache.INSTANCE.uniqueKeyForFields.getOrUploadIfAbsent(
			targetClassClassLoader,
			cacheKey,
			() ->
				findAllAndMakeThemAccessible(
					FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(field -> {
						if (valueType == null) {
							return field.getName().equals(fieldName);
						} else {
							return field.getName().equals(fieldName) && Classes.INSTANCE.isAssignableFrom(field.getType(), valueType);
						}
					}), targetClass
				)
		);
	}

	public Field findFirstAndMakeItAccessible(Class<?> targetClass, String fieldName) {
		return findFirstAndMakeItAccessible(targetClass, fieldName, null);
	}

	public Field findFirstAndMakeItAccessible(Class<?> targetClass, String fieldName, Class<?> fieldTypeOrSubType) {
		Collection<Field> members = findAllByExactNameAndMakeThemAccessible(targetClass, fieldName, fieldTypeOrSubType);
		if (members.size() < 1) {
			Throwables.INSTANCE.throwException(
				new NoSuchFieldException(
					Strings.compile("Field {} not found in {} hierarchy", fieldName, targetClass.getName())
				)
			);
		}
		return members.stream().findFirst().get();
	}

	public Field findOneAndMakeItAccessible(Class<?> targetClass, String memberName) {
		Collection<Field> members = findAllByExactNameAndMakeThemAccessible(targetClass, memberName, null);
		if (members.size() != 1) {
			Throwables.INSTANCE.throwException(
				new NoSuchFieldException(
					Strings.compile("Field {} not found or found more than one field in {} hierarchy", memberName, targetClass.getName())
				)
			);
		}
		return members.stream().findFirst().get();
	}

	public <T> T get(Object target, Field field) {
		return Facade.INSTANCE.getFieldValue(target, field);
	}

	public <T> T get(Object target, String fieldName) {
		return get(target, findFirstAndMakeItAccessible(Classes.INSTANCE.retrieveFrom(target), fieldName, null));
	}

	public Map<Field, ?> getAll(FieldCriteria criteria, Object target) {
		return getAll(() -> findAllAndMakeThemAccessible(criteria, Classes.INSTANCE.retrieveFrom(target)), target);
	}

	public Map<Field, ?> getAll(Object target) {
		return getAll(() -> findAllAndMakeThemAccessible(Classes.INSTANCE.retrieveFrom(target)), target);
	}

	public Map<Field, ?> getAllDirect(FieldCriteria criteria, Object target) {
		return getAllDirect(() -> findAllAndMakeThemAccessible(criteria, Classes.INSTANCE.retrieveFrom(target)), target);
	}


	public Map<Field, ?> getAllDirect(Object target) {
		return getAllDirect(() -> findAllAndMakeThemAccessible(Classes.INSTANCE.retrieveFrom(target)), target);
	}

	public Map<Field, ?> getAllStatic(Class<?> targetClass) {
		return getAll(() -> findAllAndMakeThemAccessible(targetClass), null);
	}

	public Map<Field, ?> getAllStaticDirect(Class<?> targetClass) {
		return getAllDirect(() -> findAllAndMakeThemAccessible(targetClass), null);
	}

	public <T> T getStatic(Class<?> targetClass, String fieldName) {
		return getStatic(findFirstAndMakeItAccessible(targetClass, fieldName, null));
	}

	public <T> T getStatic(Field field) {
		return get(null, field);
	}

	public void set(Object target, Field field, Object value) {
		Facade.INSTANCE.setFieldValue(target, field, value);
	}

	public void set(Object target, String fieldName, Object value) {
		set(Classes.INSTANCE.retrieveFrom(target), target, fieldName, value);
	}

	public void setStatic(Class<?> targetClass, String fieldName, Object value) {
		set(targetClass, null, fieldName, value);
	}

	public void setStatic(Field field, Object value) {
		set(null, field, value);
	}

	private Map<Field, Object> getAll(Supplier<Collection<Field>> fieldsSupplier, Object target) {
		Map<Field, Object> fieldValues = new HashMap<>();
		for (Field field : fieldsSupplier.get()) {
			if (target != null) {
				fieldValues.put(
					field,
					get(
						Modifier.isStatic(field.getModifiers()) ? null : target, field
					)
				);
			} else if (Modifier.isStatic(field.getModifiers())) {
				fieldValues.put(
					field,
					get(null, field)
				);
			}
		}
		return fieldValues;
	}

	private Map<Field, ?> getAllDirect(Supplier<Collection<Field>> fieldsSupplier, Object target) {
		Map<Field, ?> fieldValues = new HashMap<>();
		for (Field field : fieldsSupplier.get()) {
			fieldValues.put(
				field,
				Facade.INSTANCE.getFieldValue(target, field)
			);
		}
		return fieldValues;
	}

	private void set(Class<?> targetClass, Object target, String fieldName, Object value) {
		set(target, findFirstAndMakeItAccessible(targetClass, fieldName, Classes.INSTANCE.retrieveFrom(value)), value);
	}

	public static class NoSuchFieldException extends RuntimeException {

		private static final long serialVersionUID = 3656790511956737635L;

		public NoSuchFieldException(String message) {
			super(message);
		}

	}

}

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

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.burningwave.Criteria;

@SuppressWarnings("unchecked")
abstract class CriteriaWithClassElementsSupplyingSupport<
	E,
	C extends CriteriaWithClassElementsSupplyingSupport<E, C, T>,
	T extends Criteria.TestContext<E, C>
> extends Criteria<E, C, T>  {
	Map<Class<?>[], List<ByteBuffer>> byteCodeForClasses;
	Function<Class<?>, ByteBuffer> byteCodeSupplier;
	List<Class<?>> classesToBeUploaded;
	Function<Class<?>, Class<?>> classSupplier;

	Map<Class<?>, byte[]> loadedBytecode;
	Map<Class<?>, Class<?>> uploadedClasses;
	Map<Class<?>[], List<Class<?>>> uploadedClassesMap;

	CriteriaWithClassElementsSupplyingSupport() {
		byteCodeForClasses = new ConcurrentHashMap<>();
	}

	@Override
	public C createCopy() {
		C copy = super.createCopy();
		if (this.classesToBeUploaded != null) {
			copy.useClasses(this.classesToBeUploaded);
		}
		return copy;
	}

	public C useClasses(Class<?>... classes) {
		if (classesToBeUploaded == null) {
			classesToBeUploaded = new CopyOnWriteArrayList<>();
		}
		for (Class<?> cls : classes) {
			classesToBeUploaded.add(cls);
		}
		return (C)this;
	}

	public C useClasses(Collection<Class<?>> classes) {
		if (classesToBeUploaded == null) {
			classesToBeUploaded = new CopyOnWriteArrayList<>();
		}
		classesToBeUploaded.addAll(classes);
		return (C)this;
	}

	@Override
	protected C logicOperation(C leftCriteria, C rightCriteria,
		Function<BiPredicate<T, E>, Function<BiPredicate<? super T, ? super E>, BiPredicate<T, E>>> binaryOperator,
		C targetCriteria
	) {
		C newCriteria = super.logicOperation(leftCriteria, rightCriteria, binaryOperator, targetCriteria);
		if (leftCriteria.classesToBeUploaded != null) {
			newCriteria.useClasses(leftCriteria.classesToBeUploaded);
		}
		if (rightCriteria.classesToBeUploaded != null) {
			newCriteria.useClasses(rightCriteria.classesToBeUploaded);
		}
		return newCriteria;
	}

	Function<Class<?>, ByteBuffer> getByteCodeSupplier() {
		return byteCodeSupplier;
	}

	List<Class<?>> getClassesToBeUploaded() {
		return classesToBeUploaded;
	}

	Function<Class<?>, Class<?>> getClassSupplier() {
		return classSupplier;
	}

	Map<Class<?>, Class<?>> getUploadedClasses() {
		if (uploadedClasses == null) {
			synchronized (this) {
				if (uploadedClasses == null) {
					Map<Class<?>, Class<?>> uploadedClasses = new HashMap<>();
					for (Class<?> cls : classesToBeUploaded) {
						uploadedClasses.put(cls, classSupplier.apply(cls));
					}
					this.uploadedClasses = uploadedClasses;
				}
			}
		}
		return uploadedClasses;
	}

	C init(Function<Class<?>, Class<?>> classSupplier, Function<Class<?>, ByteBuffer> byteCodeSupplier) {
		if (classSupplier != null) {
			this.classSupplier = classSupplier;
			this.uploadedClassesMap = new HashMap<>();
		}
		if (byteCodeSupplier != null) {
			this.byteCodeSupplier = byteCodeSupplier;
			this.byteCodeForClasses = new HashMap<>();
		}
		return (C)this;
	}

	List<ByteBuffer> retrieveByteCode(C criteria, Class<?>[] classes) {
		List<ByteBuffer> byteCode = criteria.byteCodeForClasses.get(classes);
		if (byteCode == null) {
			synchronized (criteria.byteCodeForClasses) {
				byteCode = criteria.byteCodeForClasses.get(classes);
				if (byteCode == null) {
					byteCode = new CopyOnWriteArrayList<>();
					for (Class<?> cls : classes) {
						byteCode.add(byteCodeSupplier.apply(cls));
					}
					criteria.byteCodeForClasses.put(classes, byteCode);
				}
			}
		}
		return byteCode;
	}

	Class<?> retrieveClass(Class<?> cls) {
		if (classSupplier != null) {
			return classSupplier.apply(cls);
		}
		return cls;
	}

	List<Class<?>> retrieveUploadedClasses(Class<?>... classes) {
		List<Class<?>> uploadedClasses = uploadedClassesMap.get(classes);
		if (uploadedClasses == null) {
			synchronized(uploadedClassesMap) {
				if ((uploadedClasses = uploadedClassesMap.get(classes)) == null) {
					uploadedClasses = new CopyOnWriteArrayList<>();
					for (Class<?> element : classes) {
						uploadedClasses.add(element.isPrimitive()? element :classSupplier.apply(element));
					}
					uploadedClassesMap.put(
						classes, uploadedClasses
					);
				}
			}
		}
		return uploadedClasses;
	}

}
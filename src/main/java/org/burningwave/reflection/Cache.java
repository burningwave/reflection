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
package org.burningwave.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.burningwave.Synchronizer;
import org.burningwave.Throwables;
import org.burningwave.function.Function;
import org.burningwave.function.Supplier;
import org.burningwave.function.ThrowingSupplier;

import io.github.toolfactory.jvm.function.template.ThrowingBiConsumer;


class Cache {

	public final static Cache INSTANCE;

	static {
		INSTANCE = new Cache();
	}

	final PathForResources<Constructor<?>[]> uniqueKeyForConstructorsArray;
	final PathForResources<Field[]> uniqueKeyForFieldsArray;
	final PathForResources<Method[]> uniqueKeyForMethodsArray;
	final PathForResources<Collection<Constructor<?>>> uniqueKeyForConstructors;
	final PathForResources<Members.Handler.OfExecutable.Box<?>> uniqueKeyForExecutableAndMethodHandle;
	final PathForResources<Collection<Field>> uniqueKeyForAllFields;
	final PathForResources<Collection<Method>> uniqueKeyForAllMethods;

	private Cache() {
		uniqueKeyForConstructorsArray = new PathForResources<>();
		uniqueKeyForFieldsArray = new PathForResources<>();
		uniqueKeyForMethodsArray = new PathForResources<>();
		uniqueKeyForConstructors = new PathForResources<>();
		uniqueKeyForExecutableAndMethodHandle = new PathForResources<>();
		uniqueKeyForAllFields = new PathForResources<>();
		uniqueKeyForAllMethods = new PathForResources<>();
	}

	public void clear(final boolean destroyItems, final Object... excluded) {
		final Set<Object> toBeExcluded = (excluded != null) && (excluded.length > 0) ?
			new HashSet<>(Arrays.asList(excluded)) :
			null;
		final Set<Runnable> deepCleaners = new HashSet<>();
		addCleaningTask(deepCleaners, clear(uniqueKeyForConstructorsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForFieldsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForMethodsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForConstructors, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForExecutableAndMethodHandle, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForAllFields, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForAllMethods, toBeExcluded, destroyItems));
		new Thread() {
			@Override
			public void run() {
				for (Runnable task : deepCleaners) {
					task.run();
				}
			};
		}.start();
	}

	private boolean addCleaningTask(Set<Runnable> tasks, Runnable task) {
		if (task != null) {
			return tasks.add(task);
		}
		return false;
	}

	private Runnable clear(Object cache, Set<Object> excluded, boolean destroyItems) {
		if ((excluded == null) || !excluded.contains(cache)) {
			if (cache instanceof PathForResources) {
				return ((PathForResources<?>)cache).clear(destroyItems);
			}
		}
		return null;
	}

	static class PathForResources<R> {
		String instanceId;
		ThrowingBiConsumer<String, R, ? extends Throwable> itemDestroyer;
		Long partitionStartLevel;
		Map<Long, Map<String, Map<String, R>>> resources;
		Function<R, R> sharer;

		private PathForResources() {
			this(1L, new Function<R, R>() {
				@Override
				public R apply(final R item) {
					return item;
				}
			}, null);
		}

		private PathForResources(final ThrowingBiConsumer<String, R, ? extends Throwable> itemDestroyer) {
			this(1L, new Function<R, R>() {
				@Override
				public R apply(final R item) {
					return item;
				}
			}, itemDestroyer);
		}

		private PathForResources(final Function<R, R> sharer) {
			this(1L, sharer, null);
		}

		private PathForResources(final Function<R, R> sharer, final ThrowingBiConsumer<String, R, ? extends Throwable> itemDestroyer) {
			this(1L, new Function<R, R>() {
				@Override
				public R apply(final R item) {
					return item;
				}
			}, itemDestroyer);
		}

		private PathForResources(final Long partitionStartLevel) {
			this(partitionStartLevel, new Function<R, R>() {
				@Override
				public R apply(final R item) {
					return item;
				}
			}, null);
		}

		private PathForResources(final Long partitionStartLevel, final ThrowingBiConsumer<String, R, ? extends Throwable> itemDestroyer) {
			this(partitionStartLevel, new Function<R, R>() {
				@Override
				public R apply(final R item) {
					return item;
				}
			}, itemDestroyer);
		}

		private PathForResources(final Long partitionStartLevel, final Function<R, R> sharer) {
			this(partitionStartLevel, sharer, null);
		}

		private PathForResources(final Long partitionStartLevel, final Function<R, R> sharer, final ThrowingBiConsumer<String, R, ? extends Throwable> itemDestroyer) {
			this.partitionStartLevel = partitionStartLevel;
			this.sharer = sharer;
			this.resources = new ConcurrentHashMap<>();
			this.itemDestroyer = itemDestroyer;
			this.instanceId = this.toString();
		}

		<K, V, E extends Throwable> void deepClear(final Map<K,V> map, final ThrowingBiConsumer<K, V, E> itemDestroyer) throws E {
			final java.util.Iterator<Entry<K, V>> itr = map.entrySet().iterator();
			while (itr.hasNext()) {
				final Entry<K, V> entry = itr.next();
				try {
					itr.remove();
					itemDestroyer.accept(entry.getKey(), entry.getValue());
				} catch (final Throwable exc) {

				}
			}
		}

		R get(final String path) {
			return getOrUploadIfAbsent(path, null);
		}

		int getLoadedResourcesCount() {
			return getLoadedResourcesCount(resources);
		}

		R getOrUploadIfAbsent(final String path, final Supplier<R> resourceSupplier) {
			Long occurences = getSeparatorCount(path);
			final Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			final Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			final Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			return getOrUploadIfAbsent(nestedPartition, path, resourceSupplier);
		}

		Long getSeparatorCount(final String path) {
			Long occurences = 0L;
			for (int i = 0; i < path.length(); i++) {
	            if (path.charAt(i) == '/') {
	            	occurences++;
	            }
	        }
			return occurences;
		}

		R remove(final String path, final boolean destroy) {
			final Long occurences = getSeparatorCount(path);
			final Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			final Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			final Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			final R item =  Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, new ThrowingSupplier<R, Throwable>() {
				@Override
				public R get() throws Throwable {
					return nestedPartition.remove(path);
				}
			});
			if ((itemDestroyer != null) && destroy && (item != null)) {
				final String finalPath = path;
				try {
					itemDestroyer.accept(finalPath, item);
				} catch (Throwable exc) {
					Throwables.INSTANCE.throwException(exc);
				}
			}
			return item;
		}

		R upload(final Map<String, R> loadedResources, final String path, final Supplier<R> resourceSupplier, final boolean destroy) {
			final R oldResource = remove(path, destroy);
			 Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, new Runnable() {
				@Override
				public void run() {
					R resourceTemp = resourceSupplier.get();
					if (resourceTemp != null) {
						loadedResources.put(path, resourceTemp = sharer.apply(resourceTemp));
					}
				}
			});
			return oldResource;
		}

		R upload(final String path, final Supplier<R> resourceSupplier, final boolean destroy) {
			final Long occurences = getSeparatorCount(path);
			final Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			final Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			final Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			return upload(nestedPartition, path, resourceSupplier, destroy);
		}

		void clearResources(final Map<Long, Map<String, Map<String, R>>> partitions, final boolean destroyItems) {
			for (final Entry<Long, Map<String, Map<String, R>>> partition : partitions.entrySet()) {
				for (final Entry<String, Map<String, R>> nestedPartition : partition.getValue().entrySet()) {
					if ((itemDestroyer != null) && destroyItems) {
						deepClear(nestedPartition.getValue(), new ThrowingBiConsumer<String, R, RuntimeException>() {
							@Override
							public void accept(final String path, final R resource) throws RuntimeException {
								try {
									PathForResources.this.itemDestroyer.accept(path, resource);
								} catch (Throwable exc) {
									Throwables.INSTANCE.throwException(exc);
								}
							}
						});
					} else {
						nestedPartition.getValue().clear();
					}
				}
				partition.getValue().clear();
			}
			partitions.clear();
		}

		R getOrUploadIfAbsent(final Map<String, R> loadedResources, final String path, final Supplier<R> resourceSupplier) {
			R resource = loadedResources.get(path);
			if (resource == null) {
				resource =  Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, new ThrowingSupplier<R, Throwable>() {
					@Override
					public R get() throws Throwable {
						R resourceTemp = loadedResources.get(path);
						if ((resourceTemp == null) && (resourceSupplier != null)) {
							resourceTemp = resourceSupplier.get();
							if (resourceTemp != null) {
								loadedResources.put(path, resourceTemp = sharer.apply(resourceTemp));
							}
						}
						return resourceTemp;
					}
				});
			}
			return resource != null?
				sharer.apply(resource) :
				resource;
		}

		Map<String, Map<String, R>> retrievePartition(final Map<Long, Map<String, Map<String, R>>> partitionedResources, final Long partitionIndex) {
			Map<String, Map<String, R>> resources = partitionedResources.get(partitionIndex);
			if (resources == null) {
				resources = Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForPartitionedResources_" + partitionIndex.toString(), new ThrowingSupplier<Map<String, Map<String, R>>, Throwable>() {
					@Override
					public Map<String, Map<String, R>> get() throws Throwable {
						Map<String, Map<String, R>> resourcesTemp = partitionedResources.get(partitionIndex);
						if (resourcesTemp == null) {
							partitionedResources.put(partitionIndex, resourcesTemp = new ConcurrentHashMap<>());
						}
						return resourcesTemp;
					}
				});
			}
			return resources;
		}

		Map<String, R> retrievePartition(final Map<String, Map<String, R>> partion, final Long partitionIndex, final String path) {
			String partitionKey = "/";
			if (partitionIndex > 1) {
				partitionKey = path.substring(0, path.lastIndexOf("/"));
				partitionKey = partitionKey.substring(partitionKey.lastIndexOf("/") + 1);
			}
			Map<String, R> innerPartion = partion.get(partitionKey);
			if (innerPartion == null) {
				final String finalPartitionKey = partitionKey;
				innerPartion = Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForPartitions_" + finalPartitionKey, new ThrowingSupplier<Map<String, R>, Throwable>() {
					@Override
					public Map<String, R> get() throws Throwable {
						Map<String, R> innerPartionTemp = partion.get(finalPartitionKey);
						if (innerPartionTemp == null) {
							partion.put(finalPartitionKey, innerPartionTemp = new ConcurrentHashMap<>());
						}
						return innerPartionTemp;
					}
				});
			}
			return innerPartion;
		}

		private Runnable clear(final boolean destroyItems) {
			final Map<Long, Map<String, Map<String, R>>> partitions;
			synchronized (this.resources) {
				partitions = this.resources;
				this.resources = new ConcurrentHashMap<>();
			}
			return new Runnable() {
				@Override
				public void run() {
					clearResources(partitions, destroyItems);
				}
			};
		}

		private int getLoadedResourcesCount(final Map<Long, Map<String, Map<String, R>>> resources) {
			int count = 0;
			for (final Map.Entry<Long, Map<String, Map<String, R>>> partition : resources.entrySet()) {
				for (final Map.Entry<String, Map<String, R>> innerPartition : partition.getValue().entrySet()) {
					count += innerPartition.getValue().size();
				}
			}
			return count;
		}
	}

}

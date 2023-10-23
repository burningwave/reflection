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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.burningwave.Synchronizer;
import org.burningwave.ThrowingBiConsumer;




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

	void clear(boolean destroyItems, Object... excluded) {
		Set<Object> toBeExcluded = (excluded != null) && (excluded.length > 0) ?
			new HashSet<>(Arrays.asList(excluded)) :
			null;
		Set<Runnable> deepCleaners = new HashSet<>();
		addCleaningTask(deepCleaners, clear(uniqueKeyForConstructorsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForFieldsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForMethodsArray, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForConstructors, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForExecutableAndMethodHandle, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForAllFields, toBeExcluded, destroyItems));
		addCleaningTask(deepCleaners, clear(uniqueKeyForAllMethods, toBeExcluded, destroyItems));
		new Thread(() -> {
			for (Runnable task : deepCleaners) {
				task.run();
			}
		}).start();

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
		BiConsumer<String, R> itemDestroyer;
		Long partitionStartLevel;
		Map<Long, Map<String, Map<String, R>>> resources;
		Function<R, R> sharer;

		private PathForResources() {
			this(1L, item -> item, null);
		}

		private PathForResources(BiConsumer<String, R> itemDestroyer) {
			this(1L, item -> item, itemDestroyer);
		}

		private PathForResources(Function<R, R> sharer) {
			this(1L, sharer, null);
		}

		private PathForResources(Function<R, R> sharer, BiConsumer<String, R> itemDestroyer) {
			this(1L, item -> item, itemDestroyer);
		}

		private PathForResources(Long partitionStartLevel) {
			this(partitionStartLevel, item -> item, null);
		}

		private PathForResources(Long partitionStartLevel, BiConsumer<String, R> itemDestroyer) {
			this(partitionStartLevel, item -> item, itemDestroyer);
		}

		private PathForResources(Long partitionStartLevel, Function<R, R> sharer) {
			this(partitionStartLevel, sharer, null);
		}

		private PathForResources(Long partitionStartLevel, Function<R, R> sharer, BiConsumer<String, R> itemDestroyer) {
			this.partitionStartLevel = partitionStartLevel;
			this.sharer = sharer;
			this.resources = new ConcurrentHashMap<>();
			this.itemDestroyer = itemDestroyer;
			this.instanceId = this.toString();
		}

		<K, V, E extends Throwable> void deepClear(Map<K,V> map, ThrowingBiConsumer<K, V, E> itemDestroyer) throws E {
			java.util.Iterator<Entry<K, V>> itr = map.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<K, V> entry = itr.next();
				try {
					itr.remove();
					itemDestroyer.accept(entry.getKey(), entry.getValue());
				} catch (Throwable exc) {

				}
			}
		}

		R get(String path) {
			return getOrUploadIfAbsent(path, null);
		}

		int getLoadedResourcesCount() {
			return getLoadedResourcesCount(resources);
		}

		R getOrUploadIfAbsent(String path, Supplier<R> resourceSupplier) {
			Long occurences = path.chars().filter(ch -> ch == '/').count();
			Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			return getOrUploadIfAbsent(nestedPartition, path, resourceSupplier);
		}

		R remove(String path, boolean destroy) {
			Long occurences = path.chars().filter(ch -> ch == '/').count();
			Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			R item =  Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, () -> {
				return nestedPartition.remove(path);
			});
			if ((itemDestroyer != null) && destroy && (item != null)) {
				String finalPath = path;
				itemDestroyer.accept(finalPath, item);
			}
			return item;
		}

		R upload(Map<String, R> loadedResources, String path, Supplier<R> resourceSupplier, boolean destroy) {
			R oldResource = remove(path, destroy);
			 Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, () -> {
				R resourceTemp = resourceSupplier.get();
				if (resourceTemp != null) {
					loadedResources.put(path, resourceTemp = sharer.apply(resourceTemp));
				}
			});
			return oldResource;
		}

		R upload(String path, Supplier<R> resourceSupplier, boolean destroy) {
			Long occurences = path.chars().filter(ch -> ch == '/').count();
			Long partitionIndex = occurences > partitionStartLevel? occurences : partitionStartLevel;
			Map<String, Map<String, R>> partion = retrievePartition(resources, partitionIndex);
			Map<String, R> nestedPartition = retrievePartition(partion, partitionIndex, path);
			return upload(nestedPartition, path, resourceSupplier, destroy);
		}

		void clearResources(Map<Long, Map<String, Map<String, R>>> partitions, boolean destroyItems) {
			for (Entry<Long, Map<String, Map<String, R>>> partition : partitions.entrySet()) {
				for (Entry<String, Map<String, R>> nestedPartition : partition.getValue().entrySet()) {
					if ((itemDestroyer != null) && destroyItems) {
						deepClear(nestedPartition.getValue(), (path, resource) -> {
							this.itemDestroyer.accept(path, resource);
						});
					} else {
						nestedPartition.getValue().clear();
					}
				}
				partition.getValue().clear();
			}
			partitions.clear();
		}

		R getOrUploadIfAbsent(Map<String, R> loadedResources, String path, Supplier<R> resourceSupplier) {
			R resource = loadedResources.get(path);
			if (resource == null) {
				resource =  Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForLoadedResources_" + path, () -> {
					R resourceTemp = loadedResources.get(path);
					if ((resourceTemp == null) && (resourceSupplier != null)) {
						resourceTemp = resourceSupplier.get();
						if (resourceTemp != null) {
							loadedResources.put(path, resourceTemp = sharer.apply(resourceTemp));
						}
					}
					return resourceTemp;
				});
			}
			return resource != null?
				sharer.apply(resource) :
				resource;
		}

		Map<String, Map<String, R>> retrievePartition(Map<Long, Map<String, Map<String, R>>> partitionedResources, Long partitionIndex) {
			Map<String, Map<String, R>> resources = partitionedResources.get(partitionIndex);
			if (resources == null) {
				resources = Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForPartitionedResources_" + partitionIndex.toString(), () -> {
					Map<String, Map<String, R>> resourcesTemp = partitionedResources.get(partitionIndex);
					if (resourcesTemp == null) {
						partitionedResources.put(partitionIndex, resourcesTemp = new ConcurrentHashMap<>());
					}
					return resourcesTemp;
				});
			}
			return resources;
		}

		Map<String, R> retrievePartition(Map<String, Map<String, R>> partion, Long partitionIndex, String path) {
			String partitionKey = "/";
			if (partitionIndex > 1) {
				partitionKey = path.substring(0, path.lastIndexOf("/"));
				partitionKey = partitionKey.substring(partitionKey.lastIndexOf("/") + 1);
			}
			Map<String, R> innerPartion = partion.get(partitionKey);
			if (innerPartion == null) {
				String finalPartitionKey = partitionKey;
				innerPartion = Synchronizer.INSTANCE.execute(instanceId + "_mutexManagerForPartitions_" + finalPartitionKey, () -> {
					Map<String, R> innerPartionTemp = partion.get(finalPartitionKey);
					if (innerPartionTemp == null) {
						partion.put(finalPartitionKey, innerPartionTemp = new ConcurrentHashMap<>());
					}
					return innerPartionTemp;
				});
			}
			return innerPartion;
		}

		private Runnable clear(boolean destroyItems) {
			Map<Long, Map<String, Map<String, R>>> partitions;
			synchronized (this.resources) {
				partitions = this.resources;
				this.resources = new ConcurrentHashMap<>();
			}
			return () ->
				clearResources(partitions, destroyItems);
		}

		private int getLoadedResourcesCount(Map<Long, Map<String, Map<String, R>>> resources) {
			int count = 0;
			for (Map.Entry<Long, Map<String, Map<String, R>>> partition : resources.entrySet()) {
				for (Map.Entry<String, Map<String, R>> innerPartition : partition.getValue().entrySet()) {
					count += innerPartition.getValue().size();
				}
			}
			return count;
		}
	}

}

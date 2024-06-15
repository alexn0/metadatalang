/*
 * Copyright (c) 2024 opt2code.com aka alexn0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opt2code.core.utils;


import org.apache.lucene.util.WeakIdentityMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class WeakReferenceMap<S, T> implements Map<S, T> {
    private final WeakIdentityMap<S, T> map;

    public WeakReferenceMap() {
        this(true);
    }

    public WeakReferenceMap(@NotNull Boolean concurrentSafe) {
        if (concurrentSafe) {
            map = WeakIdentityMap.newConcurrentHashMap();
        } else {
            map = WeakIdentityMap.newHashMap();
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Iterator<T> i = map.valueIterator();
        if (value==null) {
            while (i.hasNext()) {
                T w = i.next();
                if (w != null)
                    return true;
            }
        } else {
            while (i.hasNext()) {
                T w = i.next();
                if (value.equals(w))
                    return true;
            }
        }
        return false;
    }


    @Override
    public T get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public T put(S key, T value) {
        return map.put(key, value);
    }

    @Override
    public T remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends S, ? extends T> m) {
        for (Map.Entry<? extends S, ? extends T> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<S> keySet() {
        Iterator<S> i = map.keyIterator();
        IdentityHashMap<S, S> r = new IdentityHashMap<>();
        while (i.hasNext()) {
            S s = i.next();
            r.put(s, s);
        }
        return r.keySet();
    }

    @NotNull
    @Override
    public Collection<T> values() {
        Iterator<T> i = map.valueIterator();
        ArrayList<T> r = new ArrayList<>();
        while (i.hasNext()) {
            T w = i.next();
            if (w != null) {
                r.add(w);
            }
        }
        return r;
    }

    @NotNull
    @Override
    public Set<Entry<S, T>> entrySet() {
        Iterator<S> i = map.keyIterator();
        IdentityHashMap<S, T> m = new IdentityHashMap<>();
        while (i.hasNext()) {
            S key = i.next();
            T value = map.get(key);
            if (map.containsKey(key)) {
                m.put(key, value);
            }
        }
        return m.entrySet();
    }

}
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.schema2beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final class BindingsList {

    private final ArrayList<DOMBinding> bindings;
    private final Map<Integer, Integer> idToIndexMap;

    BindingsList(int initialCapacity) {
        bindings = new ArrayList<>(initialCapacity);
        idToIndexMap = new HashMap<>(initialCapacity);
    }

    DOMBinding get(int index) {
        return bindings.get(index);
    }

    DOMBinding getById(int id) {
        Integer idx = idToIndexMap.get(id);
        if (idx == null) {
            return null;
        }
        return bindings.get(idx);
    }

    int size() {
        return bindings.size();
    }

    /**
     * Convert a unique DOMBinding Id into a relative index value
     * This method may return -1 if we cannot figure out the index.
     */
    int idToIndex(int id) {
        Integer idx = idToIndexMap.get(id);
        return idx == null ? -1 : idx;
    }

    void add(DOMBinding b) {
        bindings.add(b);
        if (b != null) {
            idToIndexMap.put(b.id, bindings.size() - 1);
        }
    }

    void set(int index, DOMBinding b) {
        DOMBinding old = this.bindings.set(index, b);
        // Remove from the id -> idx map
        if (old != null) {
            idToIndexMap.remove(old.id);
        }
        // Add new one to the map
        if (b != null) {
            idToIndexMap.put(b.id, index);
        }
    }

    void remove(int index) {
        DOMBinding old = bindings.remove(index);
        if (old != null) {
            idToIndexMap.remove(old.id);
        }
        // shift the rest!
        for (int i = index; i < bindings.size(); i++) {
            DOMBinding b = bindings.get(i);
            if (b != null) {
                idToIndexMap.put(b.id, i);
            }
        }
    }

    void ensureCapacity(int capacity) {
        bindings.ensureCapacity(capacity);
    }
}

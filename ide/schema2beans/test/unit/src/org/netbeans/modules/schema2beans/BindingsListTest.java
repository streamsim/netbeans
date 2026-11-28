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

import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

public class BindingsListTest extends NbTestCase {

    public BindingsListTest(String name) {
        super(name);
    }

    public void testBasics() {
        BindingsList list = new BindingsList(100);

        assertEquals(0, list.size());
        assertEquals(-1, list.idToIndex(100));
        assertEquals(-1, list.idToIndex(0));

        assertNull(list.getById(1));
        assertNull(list.getById(100));
    }

    public void testNullElements() {
        BindingsList list = new BindingsList(100);

        assertEquals(0, list.size());

        list.add(null);
        list.add(null);
        assertEquals(2, list.size());
        assertNull(list.get(0));
        assertNull(list.get(1));
        assertNull(list.getById(100));
    }

    public void testAdd() {
        BindingsList list = new BindingsList(100);
        assertEquals(0, list.size());

        DOMBinding b = new DOMBinding();
        list.add(b);

        assertEquals(1, list.size());

        assertSame(b, list.getById(b.id));
        assertEquals(0, list.idToIndex(b.id));
    }

    public void testSet() {
        BindingsList list = new BindingsList(100);
        list.add(null);
        assertEquals(1, list.size());

        DOMBinding b = new DOMBinding();
        assertNull(list.getById(b.id));

        list.set(0, b);
        assertEquals(1, list.size());

        assertSame(b, list.getById(b.id));
        assertEquals(0, list.idToIndex(b.id));

        // Now replace old value with new
        DOMBinding b2 = new DOMBinding();
        assertNotEquals(b.id, b2.id);

        list.set(0, b2);
        assertEquals(1, list.size());
        assertSame(b2, list.getById(b2.id));
        assertEquals(0, list.idToIndex(b2.id));

        // b should be removed!
        assertNull(list.getById(b.id));
        assertEquals(-1, list.idToIndex(b.id));
    }

    public void testRemove() {
        BindingsList list = new BindingsList(100);
        list.add(null);
        DOMBinding b = new DOMBinding();
        list.add(b);
        DOMBinding b2 = new DOMBinding();
        list.add(b2);

        assertEquals(3, list.size());
        assertEquals(1, list.idToIndex(b.id));
        assertEquals(2, list.idToIndex(b2.id));

        list.remove(0);
        assertEquals(2, list.size());
        assertEquals(0, list.idToIndex(b.id));
        assertEquals(1, list.idToIndex(b2.id));

        list.remove(0);
        assertEquals(1, list.size());
        assertEquals(-1, list.idToIndex(b.id));
        assertEquals(0, list.idToIndex(b2.id));

        list.remove(0);
        assertEquals(0, list.size());
        assertEquals(-1, list.idToIndex(b.id));
        assertEquals(-1, list.idToIndex(b2.id));
    }
}
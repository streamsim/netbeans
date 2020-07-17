package org.openide.explorer.view;

import org.netbeans.junit.NbTestCase;
import org.junit.Assert;

public final class OutlineViewDropSupportTest extends NbTestCase {

    public OutlineViewDropSupportTest(String name) {
        super(name);
    }

    public void testWeirdCases() {
        // move whole block before itself
        int[] perm = OutlineViewDropSupport.calculateReorder(0, -1, new int[]{2, 1, 0}, 3);
        Assert.assertArrayEquals(new int[]{0, 1, 2}, perm);

        // move whole block after itself
        perm = OutlineViewDropSupport.calculateReorder(3, 2, new int[]{2, 1, 0}, 3);
        Assert.assertArrayEquals(new int[]{0, 1, 2}, perm);
    }

    public void testReorderSimple() {
        // Reorder {0,1,2} -> drag 2 between 0 and 1
        int[] perm = OutlineViewDropSupport.calculateReorder(1, 0, new int[]{2}, 3);
        Assert.assertArrayEquals(new int[]{0, 2, 1}, perm);
    }

    public void testReorderOutOfBounds() {
        // Indices contain out of bounds
        int[] perm = OutlineViewDropSupport.calculateReorder(1, 0, new int[]{5, 2, -1}, 3);
        Assert.assertArrayEquals(new int[]{0, 2, 1}, perm);
    }

    public void testReorderMultiple2() {
        // Reorder {0,1,2,3} -> drag 0,1 between 2 and 3
        int[] perm = OutlineViewDropSupport.calculateReorder(3, 2, new int[]{1, 0}, 4);
        Assert.assertArrayEquals(new int[]{2, 0, 1, 3}, perm);
    }

    public void testReorderMultiple() {
        // Reorder {0,1,2,3} -> drag 2,3 between 0 and 1
        int[] perm = OutlineViewDropSupport.calculateReorder(1, 0, new int[]{2, 3}, 4);
        Assert.assertArrayEquals(new int[]{0, 2, 3, 1}, perm);

        // Reorder {0,1,2,3,4,5} -> drag 0,1,4 between 2 and 3
        perm = OutlineViewDropSupport.calculateReorder(3, 2, new int[]{1, 0, 4}, 6);
        Assert.assertArrayEquals(new int[]{2, 0, 1, 4, 3, 5}, perm);
    }

    public void testReorderEdges() {
        int[] perm = OutlineViewDropSupport.calculateReorder(0, -1, new int[]{2}, 3);
        Assert.assertArrayEquals(new int[]{2, 0, 1}, perm);

        perm = OutlineViewDropSupport.calculateReorder(3, 2, new int[]{0}, 3);
        Assert.assertArrayEquals(new int[]{1, 2, 0}, perm);
    }

    public void testInvalidCases() {
        Assert.assertNull(OutlineViewDropSupport.calculateReorder(-1, 2, new int[]{0}, 3));
        Assert.assertNull(OutlineViewDropSupport.calculateReorder(2, 3, new int[]{0}, 3));
        Assert.assertNull(OutlineViewDropSupport.calculateReorder(1, 1, new int[]{0}, 3));
        Assert.assertNull(OutlineViewDropSupport.calculateReorder(1, 0, new int[]{3,4,5}, 3));
    }

}
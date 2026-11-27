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
package org.openide.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import junit.framework.TestCase;

/**
 * Tests HiDPI scale detection in {@link CachedHiDPIIcon}.
 */
public class CachedHiDPIIconTest extends TestCase {

    public CachedHiDPIIconTest(String name) {
        super(name);
    }

    private static double paintAndCaptureScale(AffineTransform transform) {
        final AtomicReference<Double> capturedScale = new AtomicReference<>();
        CachedHiDPIIcon icon = new CachedHiDPIIcon(10, 10) {
            @Override
            protected Image createAndPaintImage(
                    Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
            {
                capturedScale.set(scale);
                BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
                Graphics2D g = img.createGraphics();
                try {
                    g.setColor(Color.RED);
                    g.fillRect(0, 0, deviceWidth, deviceHeight);
                } finally {
                    g.dispose();
                }
                return img;
            }
        };
        BufferedImage surface = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = surface.createGraphics();
        try {
            g.setTransform(transform);
            icon.paintIcon(new JLabel(), g, 0, 0);
        } finally {
            g.dispose();
        }
        assertNotNull("createAndPaintImage should have been called", capturedScale.get());
        return capturedScale.get();
    }

    public void testUniformScale() {
        assertEquals(2.0, paintAndCaptureScale(AffineTransform.getScaleInstance(2.0, 2.0)), 1e-9);
    }

    public void testUniformScaleWithTranslation() {
        AffineTransform tx = AffineTransform.getTranslateInstance(3.5, 4.5);
        tx.scale(1.5, 1.5);
        assertEquals(1.5, paintAndCaptureScale(tx), 1e-9);
    }

    public void testNearUniformScaleUsesAverage() {
        // Simulate splashW/srcW vs splashH/srcH float/ceil mismatch.
        double scale = paintAndCaptureScale(AffineTransform.getScaleInstance(2.0, 2.002));
        assertEquals(2.001, scale, 1e-9);
        assertTrue("near-uniform must not fall back to 1.0", scale > 1.5);
    }

    public void testNonUniformScaleUsesMax() {
        double scale = paintAndCaptureScale(AffineTransform.getScaleInstance(2.0, 3.0));
        assertEquals(3.0, scale, 1e-9);
    }

    public void testIdentityIsScaleOne() {
        assertEquals(1.0, paintAndCaptureScale(new AffineTransform()), 1e-9);
    }

    public void testRotationFallsBackToOne() {
        AffineTransform tx = AffineTransform.getScaleInstance(2.0, 2.0);
        tx.rotate(Math.PI / 4);
        assertEquals(1.0, paintAndCaptureScale(tx), 1e-9);
    }
}

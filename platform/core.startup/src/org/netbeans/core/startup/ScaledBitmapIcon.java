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
package org.netbeans.core.startup;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.swing.Icon;
import org.openide.util.CachedHiDPIIcon;
import org.openide.util.Parameters;

/**
 * An Icon implementation that scales a source image to the specified dimensions. Can be used to
 * produce sharp images on HiDPI displays, without relying on MultiResolutionImage, which exists
 * only since JDK 9. This also sidesteps https://bugs.openjdk.java.net/browse/JDK-8212226 on
 * Windows. It's recommended to use a source image with dimensions that are exactly twice those of
 * the icon's logical dimensions. A double-resolution source image will automatically be scaled
 * down to 1x, 1.5x, or other HiDPI scaling factors as needed.
 *
 * <p>When the source is itself a {@link CachedHiDPIIcon} (e.g. an SVG icon), painting goes through
 * a uniform screen-scale transform first so nested HiDPI rasterization stays at device resolution;
 * the result is then bicubic-scaled to the splash size if needed.
 */
final class ScaledBitmapIcon extends CachedHiDPIIcon {
    private final Icon sourceIcon;
    private final int sourceWidth;
    private final int sourceHeight;

    public ScaledBitmapIcon(Icon sourceIcon, int width, int height) {
        super(width, height);
        Parameters.notNull("sourceIcon", sourceIcon);
        this.sourceIcon = sourceIcon;
        sourceWidth = sourceIcon.getIconWidth();
        sourceHeight = sourceIcon.getIconHeight();
    }

    private static void configureQualityHints(Graphics2D g) {
        /* Despite these hints, downscaling by more than 2x tends to give low-quality
        results compared to image resizing in, say, Photoshop or IrfanView. This is a known
        quality/performance trade-off in Java2D; see
        https://stackoverflow.com/questions/24745147/java-resize-image-without-losing-quality .
        Our Javadoc recommendation to use a scaling of exactly 2x should avoid these
        problems. */
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    @Override
    protected Image createAndPaintImage(
        Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
    {
        final BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
        if (sourceWidth > 0 && sourceHeight > 0) {
            final int srcDeviceWidth = (int) Math.ceil(sourceWidth * scale);
            final int srcDeviceHeight = (int) Math.ceil(sourceHeight * scale);
            final Graphics2D g = img.createGraphics();
            try {
                configureQualityHints(g);
                /* Paint the source with a uniform screen scale only. Nested CachedHiDPIIcon
                implementations (e.g. SVGIcon) read that scale and rasterize at device pixels;
                a non-uniform splashW/srcW vs splashH/srcH transform would previously cause them
                to fall back to scale 1.0. */
                if (srcDeviceWidth == deviceWidth && srcDeviceHeight == deviceHeight) {
                    g.setTransform(AffineTransform.getScaleInstance(scale, scale));
                    sourceIcon.paintIcon(null, g, 0, 0);
                } else {
                    final BufferedImage srcImg =
                            createBufferedImage(colorModel, srcDeviceWidth, srcDeviceHeight);
                    final Graphics2D gSrc = srcImg.createGraphics();
                    try {
                        configureQualityHints(gSrc);
                        gSrc.setTransform(AffineTransform.getScaleInstance(scale, scale));
                        sourceIcon.paintIcon(null, gSrc, 0, 0);
                    } finally {
                        gSrc.dispose();
                    }
                    g.drawImage(srcImg, 0, 0, deviceWidth, deviceHeight, null);
                }
            } finally {
                g.dispose();
            }
        }
        return img;
    }
}

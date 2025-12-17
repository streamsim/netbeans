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
package org.openide.util.svg;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
public class SVGLoaderImplTest extends NbTestCase {

    public SVGLoaderImplTest(String name) {
        super(name);
    }
    
    public void testLoadInnocentImage() throws Exception {
        Image im = ImageUtilities.loadImage("org/openide/util/svg/innocent.svg", false); // NOI18N
        assertNotNull("Image must not load", im);
    }

    public void testLoadImageWithNbreslocImageHrefXLink() throws Exception {
        Image im = ImageUtilities.loadImage("org/openide/util/svg/nbreslocImageHrefXLink.svg", false); // NOI18N
        assertNotNull("Image must not load", im);
//        preview(im);
    }

    private static void preview(Image im) throws InterruptedException, HeadlessException {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(new JLabel(new ImageIcon(im)), BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        CountDownLatch latch = new CountDownLatch(1);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                latch.countDown();
            }
        });
        frame.setVisible(true);
        latch.await();
    }

    public void testLoadImageWithExternalUseHrefXlink() throws Exception {
      testLoadImageWithExternalHref("org/openide/util/svg/externalHref.svg");
    }

    public void testLoadImageWithExternalImageHrefXLink() throws Exception {
      testLoadImageWithExternalHref("org/openide/util/svg/externalImageHrefXLink.svg");
    }

    public void testLoadImageWithExternalImageHref() throws Exception {
      testLoadImageWithExternalHref("org/openide/util/svg/externalImageHref.svg");
    }

    public void testLoadImageWithExternalHref(String image) throws Exception {
        class H extends Handler {
            List<LogRecord> recorded = new ArrayList<>();

            @Override
            public void publish(LogRecord lr) {
                recorded.add(lr);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H h = new H();
        Logger.getLogger(ImageUtilities.class.getName()).addHandler(h);
        try {
            Image im = ImageUtilities.loadImage(image, false); // NOI18N
            assertNull("Image must not load", im);
        } finally {
            Logger.getLogger(ImageUtilities.class.getName()).removeHandler(h);
        }
        Optional<LogRecord> report = h.recorded.stream().filter(lr ->
                    lr.getLevel().intValue() >= Level.INFO.intValue() &&
                    lr.getSourceMethodName().equals("getIcon")).
                findFirst();
        assertTrue("getIcon should have reported a message", report.isPresent());
        assertTrue("should report failed load", report.get().getMessage().startsWith("Failed to load SVG"));
    }
}

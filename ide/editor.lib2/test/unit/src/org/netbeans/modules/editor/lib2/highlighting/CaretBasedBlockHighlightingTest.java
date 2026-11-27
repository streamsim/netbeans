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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.lib2.highlighting.CaretBasedBlockHighlighting.CaretRowHighlighting;
import org.netbeans.modules.editor.lib2.highlighting.CaretBasedBlockHighlighting.TextSelectionHighlighting;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * studioSL#2008: caret-row / selection highlighting must tolerate invalid offsets.
 */
public class CaretBasedBlockHighlightingTest extends NbTestCase {

    public CaretBasedBlockHighlightingTest(String name) {
        super(name);
    }

    public void testCaretRowSkipsInvalidOffsetWithoutWarning() throws Exception {
        final boolean[] hadWarning = { false };
        SwingUtilities.invokeAndWait(() -> {
            Logger log = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
            Handler handler = warningHandler(hadWarning);
            log.addHandler(handler);
            Level prev = log.getLevel();
            try {
                log.setLevel(Level.ALL);
                JEditorPane pane = new JEditorPane("text/plain", "");
                Document doc = pane.getDocument();
                doc.insertString(0, "hello", null);
                pane.setCaret(new FixedOffsetCaret(5000));

                CaretRowHighlighting highlighting = new CaretRowHighlighting(pane);
                HighlightsSequence hs = highlighting.getHighlights(0, doc.getLength());
                assertFalse("No caret-row highlights for invalid caret offset", hs.moveNext());
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            } finally {
                log.removeHandler(handler);
                log.setLevel(prev);
            }
        });
        assertFalse("Invalid caret offset must not log WARNING", hadWarning[0]);
    }

    public void testTextSelectionSkipsInvalidOffsetsWithoutWarning() throws Exception {
        final boolean[] hadWarning = { false };
        SwingUtilities.invokeAndWait(() -> {
            Logger log = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
            Handler handler = warningHandler(hadWarning);
            log.addHandler(handler);
            Level prev = log.getLevel();
            try {
                log.setLevel(Level.ALL);
                JEditorPane pane = new JEditorPane("text/plain", "");
                Document doc = pane.getDocument();
                doc.insertString(0, "hello", null);
                pane.setCaret(new FixedOffsetCaret(100, 200));

                TextSelectionHighlighting highlighting = new TextSelectionHighlighting(pane);
                HighlightsSequence hs = highlighting.getHighlights(0, doc.getLength());
                assertFalse("No selection highlights for invalid caret offsets", hs.moveNext());
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            } finally {
                log.removeHandler(handler);
                log.setLevel(prev);
            }
        });
        assertFalse("Invalid selection offsets must not log WARNING", hadWarning[0]);
    }

    public void testDocumentPropertyChangeWithInvalidCaretDoesNotWarn() throws Exception {
        final boolean[] hadWarning = { false };
        SwingUtilities.invokeAndWait(() -> {
            Logger log = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
            Handler handler = warningHandler(hadWarning);
            log.addHandler(handler);
            Level prev = log.getLevel();
            try {
                log.setLevel(Level.ALL);
                JEditorPane pane = new JEditorPane("text/plain", "");
                Document longDoc = pane.getDocument();
                longDoc.insertString(0, "abcdefghijklmnopqrstuvwxyz", null);
                // Invalid for both the long and short documents so getAttribs is never needed
                FixedOffsetCaret caret = new FixedOffsetCaret(5000);
                pane.setCaret(caret);

                CaretRowHighlighting highlighting = new CaretRowHighlighting(pane);
                // Force init so the document property listener is registered
                highlighting.getHighlights(0, longDoc.getLength());

                PlainDocument shortDoc = new PlainDocument();
                shortDoc.insertString(0, "ab", null);
                pane.setDocument(shortDoc);

                HighlightsSequence hs = highlighting.getHighlights(0, shortDoc.getLength());
                assertFalse("No caret-row highlights after swap with invalid caret", hs.moveNext());
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            } finally {
                log.removeHandler(handler);
                log.setLevel(prev);
            }
        });
        assertFalse("Document swap with invalid caret must not log WARNING", hadWarning[0]);
    }

    private static Handler warningHandler(final boolean[] hadWarning) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    hadWarning[0] = true;
                }
            }
            @Override public void flush() {}
            @Override public void close() {}
        };
    }

    /**
     * Caret that reports fixed offsets and skips DefaultCaret document binding.
     */
    private static final class FixedOffsetCaret extends DefaultCaret {
        private final int dot;
        private final int mark;

        FixedOffsetCaret(int offset) {
            this(offset, offset);
        }

        FixedOffsetCaret(int dot, int mark) {
            this.dot = dot;
            this.mark = mark;
        }

        @Override
        public void install(JTextComponent c) {
            // Intentionally skip DefaultCaret.install to keep fixed offsets
        }

        @Override
        public void deinstall(JTextComponent c) {
        }

        @Override
        public int getDot() {
            return dot;
        }

        @Override
        public int getMark() {
            return mark;
        }
    }
}

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

package org.netbeans.api.editor.caret;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;

/**
 * studioSL#2008 / studioSL#3023: EditorCaret must reset on true document swap.
 */
public class EditorCaretDocumentSwapTest extends NbTestCase {

    public EditorCaretDocumentSwapTest(String name) {
        super(name);
    }

    public void testCaretResetsOnDocumentSwap() throws Exception {
        final int[] resultDot = { -1 };
        SwingUtilities.invokeAndWait(() -> {
            try {
                JEditorPane pane = new JEditorPane("text/plain", "");
                Document longDoc = pane.getDocument();
                longDoc.insertString(0, "abcdefghijklmnopqrstuvwxyz", null);

                EditorCaret caret = new EditorCaret();
                pane.setCaret(caret);
                caret.setDot(20);
                assertEquals(20, caret.getDot());

                PlainDocument shortDoc = new PlainDocument();
                shortDoc.insertString(0, "ab", null);
                pane.setDocument(shortDoc);

                resultDot[0] = caret.getDot();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        assertEquals("Caret must reset to start after document swap", 0, resultDot[0]);
    }

    public void testCaretPreservedOnSameDocumentReinstall() throws Exception {
        final int[] resultDot = { -1 };
        SwingUtilities.invokeAndWait(() -> {
            try {
                JEditorPane pane = new JEditorPane("text/plain", "");
                Document doc = pane.getDocument();
                doc.insertString(0, "abcdefghijklmnopqrstuvwxyz", null);

                EditorCaret caret = new EditorCaret();
                pane.setCaret(caret);
                caret.setDot(15);
                assertEquals(15, caret.getDot());

                // IME-style: deinstall/install without changing the document
                caret.deinstall(pane);
                caret.install(pane);

                resultDot[0] = caret.getDot();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        assertEquals("Caret offset must be preserved on same-document reinstall", 15, resultDot[0]);
    }
}

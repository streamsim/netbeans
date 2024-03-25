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
package org.netbeans.modules.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import org.openide.util.Exceptions;

/**
 *
 * @author matteo
 */
public class NamedPipeURLConnection extends URLConnection {

    private RandomAccessFile pipe = null;
    private InputStream in = null;
    private OutputStream out = null;

    public NamedPipeURLConnection(URL url) throws FileNotFoundException {
        super(url);
        if (!"npipe".equals(url.getProtocol())) { // NOI18N
            throw new IllegalArgumentException("Expected 'npipe' protocol"); // NOI18N
        }
    }

    @Override
    public void connect() throws IOException {
        if (pipe != null) {
            return;
        }
        File file = new File(url.toString().substring(url.getProtocol().length() + 3));
        this.pipe = new RandomAccessFile(file, "rw"); // NOI18N
        FileChannel channel = this.pipe.getChannel();
        this.in = Channels.newInputStream(channel);
        this.out = Channels.newOutputStream(channel);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        return new InputStream() {

            @Override
            public int read() throws IOException {
                while (true) {
                    if (in.available() > 0) {
                        break;
                    }
                }
                return in.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                while (true) {
                    if (in.available() > 0) {
                        break;
                    }
                }
                return in.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                while (true) {
                    if (in.available() > 0) {
                        break;
                    }
                }
                return in.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                NamedPipeURLConnection.this.close();
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        connect();
        return new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                out.write(b);
                out.flush();
            }

            @Override
            public void write(byte[] b) throws IOException {
                out.write(b);
                out.flush();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
                out.flush();
            }

            @Override
            public void close() throws IOException {
                NamedPipeURLConnection.this.close();
            }
        };
    }

    public void close() throws IOException {
        if (pipe != null) {
            new Thread(() -> {
                try {
                    pipe.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }, "NamedPipeURLConnection.close() Thread").start();
        }
    }
}

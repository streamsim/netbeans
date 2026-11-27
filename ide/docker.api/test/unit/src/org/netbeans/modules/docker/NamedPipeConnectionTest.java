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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.Assume;
import org.netbeans.junit.NbTestCase;
import org.openide.util.BaseUtilities;

/**
 * Live integration test against a local Docker Engine named pipe.
 * Disabled by default; enable with {@code -Dtest.docker.npipe=true}.
 *
 * @author matteo
 */
public class NamedPipeConnectionTest extends NbTestCase {

    private static final String DOCKER_ENGINE_NAMED_PIPE_URL = "npipe:////./pipe/docker_engine"; // NOI18N
    private static final File DOCKER_ENGINE_FILE = new File("\\\\.\\pipe\\docker_engine"); // NOI18N

    public NamedPipeConnectionTest(String name) {
        super(name);
    }

    public void testPingDockerEngineViaNamedPipe() throws Exception {

        Assume.assumeTrue("Set -Dtest.docker.npipe=true to run this test",
                Boolean.getBoolean("test.docker.npipe"));

        Assume.assumeTrue("Named-pipe Docker transport is Windows-only",
                BaseUtilities.isWindows());

        Assume.assumeTrue("Docker Engine named pipe is not available: " + DOCKER_ENGINE_FILE,
                DOCKER_ENGINE_FILE.exists());

        URL url = new URL(null, DOCKER_ENGINE_NAMED_PIPE_URL, new NamedPipeURLStreamHandler());
        Endpoint endpoint = Endpoint.forNamedPipe(url);
        try {
            OutputStream os = endpoint.getOutputStream();
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                host = "localhost"; // NOI18N
            }

            os.write(("GET /_ping HTTP/1.1\r\n"
                      + "Host: " + host + "\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
            os.flush();

            InputStream is = endpoint.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            assertEquals("Docker Engine /_ping should return HTTP 200",
                    HttpURLConnection.HTTP_OK, response.getCode());
        } finally {
            endpoint.close();
        }
    }
}

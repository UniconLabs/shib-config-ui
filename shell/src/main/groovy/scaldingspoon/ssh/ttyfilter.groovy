package scaldingspoon.ssh

import org.apache.sshd.common.util.Buffer
import org.apache.sshd.server.shell.ProcessShellFactory

/*
 * Adapted from Apache MINA SSHD
 *
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

class TtyFilterInputStream extends FilterInputStream {
    EnumSet<ProcessShellFactory.TtyOptions> ttyOptions
    Buffer buffer

    TtyFilterInputStream(InputStream inputStream) {
        super(inputStream)
        buffer = new Buffer(32)
    }

    TtyFilterInputStream(InputStream inputStream, EnumSet<ProcessShellFactory.TtyOptions> ttyOptions) {
        this(inputStream)
        this.ttyOptions = ttyOptions
    }

    @Override
    int read() throws IOException {
        def b
        if (buffer.available() > 0) {
            b = buffer.byte
            buffer.compact()
        } else {
            b = super.read()
        }
        if (b == '\n' && ttyOptions.contains(ProcessShellFactory.TtyOptions.INlCr)) {
            b = '\r'
            Buffer buf = new Buffer()
            buf.putByte((byte) '\n')
            buf.putBuffer(buffer)
            buffer = buf
        } else if (b == '\r' && ttyOptions.contains(ProcessShellFactory.TtyOptions.ICrNl)) {
            b = '\n'
        }
        return (int)b
    }

    @Override
    int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len)
    }

    @Override
    int available() throws IOException {
        return super.available() + buffer.available()
    }
}

class TtyFilterOutputStream extends FilterOutputStream {
    EnumSet<ProcessShellFactory.TtyOptions> ttyOptions
    int lastChar

    TtyFilterOutputStream(OutputStream outputStream) {
        super(outputStream)
    }

    TtyFilterOutputStream(OutputStream outputStream, EnumSet<ProcessShellFactory.TtyOptions> ttyOptions) {
        this(outputStream)
        this.ttyOptions = ttyOptions
    }

    @Override
    void write(int b) throws IOException {
        if (b == '\n' && ttyOptions.contains(ProcessShellFactory.TtyOptions.ONlCr) && lastChar != '\r') {
            super.write((byte)'\r')
        }
        super.write(b)
        lastChar = b
    }

    @Override
    void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len)
    }
}
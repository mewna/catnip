/*
 * Copyright (c) 2019 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.util;

import com.mewna.catnip.util.MultipartBodyPublisher.PartsSpecification.TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * Adapted from https://stackoverflow.com/a/54675316
 *
 * @author amy
 * @since 3/25/19.
 */
public class MultipartBodyPublisher {
    private final Collection<PartsSpecification> partsSpecificationList = new ArrayList<>();
    private final String boundary = UUID.randomUUID().toString();
    
    public BodyPublisher build() {
        if(partsSpecificationList.isEmpty()) {
            throw new IllegalStateException("Must have at least one part to build multipart message.");
        }
        addFinalBoundaryPart();
        return BodyPublishers.ofByteArrays(PartsIterator::new);
    }
    
    public String getBoundary() {
        return boundary;
    }
    
    public MultipartBodyPublisher addPart(final String name, final String value) {
        final PartsSpecification newPart = new PartsSpecification();
        newPart.type = TYPE.STRING;
        newPart.name = name;
        newPart.value = value;
        partsSpecificationList.add(newPart);
        return this;
    }
    
    public MultipartBodyPublisher addPart(final String name, final Path value) {
        final PartsSpecification newPart = new PartsSpecification();
        newPart.type = TYPE.FILE;
        newPart.name = name;
        newPart.path = value;
        partsSpecificationList.add(newPart);
        return this;
    }
    
    public MultipartBodyPublisher addPart(final String name, final Supplier<InputStream> value, final String filename) {
        final PartsSpecification newPart = new PartsSpecification();
        newPart.type = TYPE.STREAM;
        newPart.name = name;
        newPart.stream = value;
        newPart.filename = filename;
        partsSpecificationList.add(newPart);
        return this;
    }
    
    public MultipartBodyPublisher addPart(final String name, final Supplier<InputStream> value, final String filename, final String contentType) {
        final PartsSpecification newPart = new PartsSpecification();
        newPart.type = TYPE.STREAM;
        newPart.name = name;
        newPart.stream = value;
        newPart.filename = filename;
        newPart.contentType = contentType;
        partsSpecificationList.add(newPart);
        return this;
    }
    
    private void addFinalBoundaryPart() {
        final PartsSpecification newPart = new PartsSpecification();
        newPart.type = TYPE.FINAL_BOUNDARY;
        newPart.value = "--" + boundary + "--";
        partsSpecificationList.add(newPart);
    }
    
    static class PartsSpecification {
        private TYPE type;
        private String name;
        private String value;
        private Path path;
        private Supplier<InputStream> stream;
        private String filename;
        private String contentType;
        
        public TYPE getType() {
            return type;
        }
        
        public void setType(final TYPE type) {
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(final String name) {
            this.name = name;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(final String value) {
            this.value = value;
        }
        
        public Path getPath() {
            return path;
        }
        
        public void setPath(final Path path) {
            this.path = path;
        }
        
        public Supplier<InputStream> getStream() {
            return stream;
        }
        
        public void setStream(final Supplier<InputStream> stream) {
            this.stream = stream;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public void setFilename(final String filename) {
            this.filename = filename;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(final String contentType) {
            this.contentType = contentType;
        }
        
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum TYPE {
            STRING, FILE, STREAM, FINAL_BOUNDARY
        }
    }
    
    class PartsIterator implements Iterator<byte[]> {
        private final Iterator<PartsSpecification> iter;
        private InputStream currentFileInput;
        
        private boolean done;
        private byte[] next;
        
        PartsIterator() {
            iter = partsSpecificationList.iterator();
        }
        
        @Override
        public boolean hasNext() {
            if(done) {
                return false;
            }
            if(next != null) {
                return true;
            }
            try {
                next = computeNext();
            } catch(final IOException e) {
                throw new UncheckedIOException(e);
            }
            if(next == null) {
                done = true;
                return false;
            }
            return true;
        }
        
        @Override
        public byte[] next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            final byte[] res = next;
            next = null;
            return res;
        }
        
        private byte[] computeNext() throws IOException {
            if(currentFileInput == null) {
                if(!iter.hasNext()) {
                    return null;
                }
                final PartsSpecification nextPart = iter.next();
                if(nextPart.type == TYPE.STRING) {
                    final String part =
                            "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=" + nextPart.name + "\r\n" +
                                    "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                                    nextPart.value + "\r\n";
                    return part.getBytes(StandardCharsets.UTF_8);
                }
                if(nextPart.type == TYPE.FINAL_BOUNDARY) {
                    return nextPart.value.getBytes(StandardCharsets.UTF_8);
                }
                final String filename;
                String contentType;
                if(nextPart.type == TYPE.FILE) {
                    final Path path = nextPart.path;
                    filename = path.getFileName().toString();
                    contentType = Files.probeContentType(path);
                    if(contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    currentFileInput = Files.newInputStream(path);
                } else {
                    filename = nextPart.filename;
                    contentType = nextPart.contentType;
                    if(contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    currentFileInput = nextPart.stream.get();
                }
                final String partHeader =
                        "--" + boundary + "\r\n" +
                                "Content-Disposition: file; name=" + nextPart.name + "; filename=" + filename + "\r\n" +
                                "Content-Type: " + contentType + "\r\n\r\n";
                return partHeader.getBytes(StandardCharsets.UTF_8);
            } else {
                final byte[] buf = new byte[8192];
                final int r = currentFileInput.read(buf);
                if(r > 0) {
                    final byte[] actualBytes = new byte[r];
                    System.arraycopy(buf, 0, actualBytes, 0, r);
                    return actualBytes;
                } else {
                    currentFileInput.close();
                    currentFileInput = null;
                    return "\r\n".getBytes(StandardCharsets.UTF_8);
                }
            }
        }
    }
}

package com.mewna.catnip.rest;

import io.vertx.core.buffer.Buffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <a href="https://stackoverflow.com/a/54675316">Original Source</a>
 * <p>
 * Included due to Java 11's HTTP Client's lack of understanding of Named-Multipart.
 * <p>
 * Modified to fit the code standards of this project, to compact it, to remove unnecessary capabilities (file read straight from disc and InputStream suppliers), and add capabilities necessary to add compatibility.
 */

public class MultipartBodyPublisher {
    private final Collection<PartsSpecification> partsSpecificationList = new ArrayList<>();
    @Getter
    private final String boundary = UUID.randomUUID().toString();
    
    public BodyPublisher build() {
        if(partsSpecificationList.isEmpty()) {
            throw new IllegalStateException("Must have at least one part to build multipart message.");
        }
        addFinalBoundary();
        return BodyPublishers.ofByteArrays(PartsIterator::new);
    }
    
    @ParametersAreNonnullByDefault
    public MultipartBodyPublisher addPart(final String name, final String value) {
        partsSpecificationList.add(new PartsSpecification(Type.STRING, name).value(Buffer.buffer(value)));
        return this;
    }
    
    @ParametersAreNonnullByDefault
    public MultipartBodyPublisher addPart(final String name, final String value, @Nullable final String contentType) {
        partsSpecificationList.add(new PartsSpecification(Type.STRING, name).value(Buffer.buffer(value)).contentType(contentType));
        return this;
    }
    
    //Using Vert.x's buffer. Reflects message's usage of Buffers.
    @ParametersAreNonnullByDefault
    public MultipartBodyPublisher addPart(final String name, final String filename, final Buffer value, @Nullable final String contentType) {
        partsSpecificationList.add(new PartsSpecification(Type.FILE, name).value(value).contentType(contentType));
        return this;
    }
    
    private void addFinalBoundary() {
        partsSpecificationList.add(new PartsSpecification(Type.FINAL_BOUNDARY, null));
    }
    
    public enum Type {
        STRING, FILE, FINAL_BOUNDARY
    }
    
    @RequiredArgsConstructor
    //@Getter // We technically don't need auto-generated getters as this is an internal class, and as such, we can ignore the need.
    @Setter
    @Accessors(fluent = true)
    protected class PartsSpecification {
        protected final Type type;
        protected final String name;
        protected Buffer value;
        protected String filename;
        protected String contentType;
        
        public String toString() {
            if(type == Type.FINAL_BOUNDARY) {
                return "--" + boundary + "--";
            }
            if(type == Type.FILE) {
                return "--" + boundary + "\r\nContent-Disposition: file; name=" + name + "; filename=" + filename + ";\r\nContent-Type:" + Objects.requireNonNullElse(contentType, "application/octet-stream") + "\r\n\r\n";
            }
            return "--" + boundary + "\r\nContent-Disposition: form-data; name=" + name + ";\r\nContent-Type:" + Objects.requireNonNullElse(contentType, "text/plain; charset=UTF-8") + "\r\n\r\n";
        }
    }
    
    class PartsIterator implements Iterator<byte[]> {
        
        private final Iterator<PartsSpecification> iter;
        private Buffer currentFileInput;
        private int index;
        
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
            next = computeNext();
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
        
        private byte[] computeNext() {
            if(currentFileInput == null) {
                if(!iter.hasNext()) {
                    return null;
                }
                final PartsSpecification nextPart = iter.next();
                if(nextPart.type == Type.STRING) {
                    final byte[] stub = nextPart.toString().getBytes(StandardCharsets.UTF_8);
                    final byte[] arr = new byte[nextPart.value.length() + stub.length + 2 /*2 to make up for \r\n*/];
                    System.arraycopy(stub, 0, arr, 0, stub.length);
                    nextPart.value.getBytes(arr, stub.length);
                    arr[arr.length - 2] = '\r';
                    arr[arr.length - 1] = '\n';
                    return arr;
                }
                if(nextPart.type == Type.FILE) {
                    currentFileInput = nextPart.value;
                }
                return nextPart.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                final int len = currentFileInput.length();
                final int remain = len - index;
                if(remain > 0) {
                    final byte[] buf;
                    if(remain >= 8192) {
                        buf = new byte[8192];
                        currentFileInput.getBytes(index, index += 8192, buf);
                    } else {
                        final int size = 8192 - remain;
                        buf = new byte[size];
                        currentFileInput.getBytes(index, index += size, buf);
                    }
                    return buf;
                } else {
                    currentFileInput = null;
                    index = 0;
                    return new byte[] {'\r', '\n'};
                }
            }
        }
    }
}
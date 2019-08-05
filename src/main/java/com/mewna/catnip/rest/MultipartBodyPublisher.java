package com.mewna.catnip.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Adapted from https://stackoverflow.com/a/54675316
 * Modified to fit the code standards of this project, to compact it, to remove unnecessary capabilities (file read straight from disc and InputStream suppliers), and add capabilities necessary to add compatibility (ie. Vert.x buffer support).
 *
 * @author kjp12
 * @since 3/25/2019
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
        partsSpecificationList.add(new PartsSpecification(Type.STRING, name).value(value.getBytes(StandardCharsets.UTF_8)));
        return this;
    }
    
    //Using Vert.x's buffer. Reflects message's usage of Buffers.
    @ParametersAreNonnullByDefault
    public MultipartBodyPublisher addPart(final String name, final String filename, final byte[] value) {
        partsSpecificationList.add(new PartsSpecification(Type.FILE, name).filename(filename).value(value));
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
        protected byte[] value;
        protected String filename;
        
        public String toString() {
            if(type == Type.FINAL_BOUNDARY) {
                return "--" + boundary + "--";
            }
            if(type == Type.FILE) {
                return "--" + boundary + "\r\nContent-Disposition: file; name=" + name + "; filename=" + filename + ";\r\nContent-Type:application/octet-stream\r\n\r\n";
            }
            return "--" + boundary + "\r\nContent-Disposition: form-data; name=" + name + ";\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n";
        }
    }
    
    class PartsIterator implements Iterator<byte[]> {
        
        private final Iterator<PartsSpecification> iter;
        private byte[] currentFileInput;
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
                    final byte[] arr = new byte[nextPart.value.length + stub.length + 2 /*2 to make up for \r\n*/];
                    System.arraycopy(stub, 0, arr, 0, stub.length);
                    System.arraycopy(nextPart.value, 0, arr, stub.length, nextPart.value.length);
                    arr[arr.length - 2] = '\r';
                    arr[arr.length - 1] = '\n';
                    return arr;
                }
                if(nextPart.type == Type.FILE) {
                    currentFileInput = nextPart.value;
                }
                return nextPart.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                final int len = currentFileInput.length;
                final int remain = len - index;
                if(remain > 0) {
                    final byte[] buf;
                    if(remain >= 8192) {
                        buf = new byte[8192];
                        System.arraycopy(currentFileInput, index, buf, 0, 8192);
                        index += 8192;
                    } else {
                        buf = new byte[remain];
                        System.arraycopy(currentFileInput, index, buf, 0, remain);
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
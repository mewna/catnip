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
 * Modified to fit the code standards of this project, to compact it, to remove
 * unnecessary capabilities (file read straight from disc and InputStream
 * suppliers), and add capabilities necessary to add compatibility.
 *
 * @author kjp12
 * @since 3/25/2019
 */

@SuppressWarnings("UnusedReturnValue")
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
        private final Iterator<PartsSpecification> parts;
        private final List<byte[]> next = new ArrayList<>();
        private boolean done;
        
        PartsIterator() {
            parts = partsSpecificationList.iterator();
        }
        
        @Override
        public boolean hasNext() {
            if(done) {
                return false;
            }
            if(!next.isEmpty()) {
                return true;
            }
            computeNext();
            if(next.isEmpty()) {
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
            return next.remove(0);
        }
        
        private void computeNext() {
            if(!parts.hasNext()) {
                return;
            }
            final var part = parts.next();
            next.add(part.toString().getBytes(StandardCharsets.UTF_8));
            if(part.type != Type.FINAL_BOUNDARY) {
                next.add(part.value);
                next.add(new byte[] {'\r', '\n'});
            }
        }
    }
}
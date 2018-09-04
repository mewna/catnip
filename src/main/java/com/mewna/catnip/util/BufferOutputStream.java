package com.mewna.catnip.util;

import io.vertx.core.buffer.Buffer;

import javax.annotation.Nonnull;
import java.io.OutputStream;

public final class BufferOutputStream extends OutputStream {
    private final Buffer buffer;
    
    public BufferOutputStream(final Buffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public void write(@Nonnull final byte[] b) {
        buffer.appendBytes(b);
    }
    
    @Override
    public void write(@Nonnull final byte[] b, final int off, final int len) {
        buffer.appendBytes(b, off, len);
    }
    
    @Override
    public void write(final int b) {
        buffer.appendByte((byte) (b & 0xFF));
    }
}

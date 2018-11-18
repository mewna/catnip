package com.mewna.catnip.util;

import io.vertx.core.buffer.Buffer;

import javax.annotation.Nonnull;
import java.io.OutputStream;

public final class BufferOutputStream extends OutputStream {
    private final Buffer buffer;
    private final int startOffset;
    private int position;
    
    public BufferOutputStream(final Buffer buffer, final int startOffset) {
        this.buffer = buffer;
        this.startOffset = startOffset;
    }
    
    @Override
    public void write(@Nonnull final byte[] b) {
        buffer.setBytes(startOffset + position, b);
        position += b.length;
    }
    
    @Override
    public void write(@Nonnull final byte[] b, final int off, final int len) {
        buffer.setBytes(startOffset + position, b, off, len);
        position += len;
    }
    
    @Override
    public void write(final int b) {
        buffer.appendByte((byte) (b & 0xFF));
        position++;
    }
}

package com.mewna.catnip.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * @author amy
 * @since 9/2/18.
 */
public class JsonPojoCodec<T> implements MessageCodec<T, T> {
    private final Class<T> type;
    private int i = 0;
    
    @SuppressWarnings("unchecked")
    public JsonPojoCodec(final Class<T> type) {
        this.type = type;
    }
    
    @Override
    public void encodeToWire(final Buffer buffer, final T t) {
        buffer.appendString(JsonObject.mapFrom(t).encode());
    }
    
    @Override
    public T decodeFromWire(final int pos, final Buffer buffer) {
        final Buffer out = Buffer.buffer();
        buffer.readFromBuffer(pos, out);
        final JsonObject data = new JsonObject(out.getString(0, out.length()));
        return data.mapTo(type);
    }
    
    @Override
    public T transform(final T t) {
        return t;
    }
    
    @Override
    public String name() {
        // This is kinda dumb, but ensures no dupe codec names
        final String result = "JsonPojoCodec-" + i;
        i++;
        return result;
    }
    
    @Override
    public byte systemCodecID() {
        return -1;
    }
}

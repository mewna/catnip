package com.mewna.catnip.util;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * @author amy
 * @since 9/2/18.
 */
public class JsonPojoCodec<T> implements MessageCodec<T, T> {
    private final Catnip catnip;
    private final Class<T> type;
    
    @SuppressWarnings("unchecked")
    public JsonPojoCodec(final Catnip catnip, final Class<T> type) {
        this.catnip = catnip;
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
        final T object = data.mapTo(type);
        if(object instanceof RequiresCatnip) {
            ((RequiresCatnip) object).catnip(catnip);
        }
        return object;
    }
    
    @Override
    public T transform(final T t) {
        if(t instanceof RequiresCatnip) {
            ((RequiresCatnip) t).catnip(catnip);
        }
        return t;
    }
    
    @Override
    public String name() {
        return "JsonPojoCodec-" + type.getName();
    }
    
    @Override
    public byte systemCodecID() {
        return -1;
    }
}

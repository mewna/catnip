package com.mewna.catnip.rest;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;

public class ResponsePayload {
    private final Buffer buffer;
    
    public ResponsePayload(final Buffer buffer) {
        this.buffer = buffer;
    }
    
    public String string() {
        return BodyCodecImpl.UTF8_DECODER.apply(buffer);
    }
    
    public JsonObject object() {
        return BodyCodecImpl.JSON_OBJECT_DECODER.apply(buffer);
    }
    
    public JsonArray array() {
        return BodyCodecImpl.JSON_ARRAY_DECODER.apply(buffer);
    }
}

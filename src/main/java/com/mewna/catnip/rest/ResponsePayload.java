package com.mewna.catnip.rest;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResponsePayload {
    private final Buffer buffer;
    
    ResponsePayload(final Buffer buffer) {
        this.buffer = buffer;
    }
    
    public String string() {
        return buffer.toString();
    }
    
    public JsonObject object() {
        return buffer.toJsonObject();
    }
    
    public JsonArray array() {
        return buffer.toJsonArray();
    }
}

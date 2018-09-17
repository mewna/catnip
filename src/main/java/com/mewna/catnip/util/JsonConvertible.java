package com.mewna.catnip.util;

import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface JsonConvertible {
    @Nonnull
    @CheckReturnValue
    JsonObject toJson();
}

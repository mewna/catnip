package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.entity.EntityBuilder;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author amy
 * @since 9/3/18.
 */
@RequiredArgsConstructor
abstract class RestHandler {
    @Getter(AccessLevel.PROTECTED)
    private final EntityBuilder entityBuilder = new EntityBuilder();
    
    @Getter(AccessLevel.PROTECTED)
    private final CatnipImpl catnip;
    
    @Nonnull
    @CheckReturnValue
    <T> Function<JsonArray, List<T>> mapObjectContents(@Nonnull final Function<JsonObject, T> builder) {
        return array -> {
            final Collection<T> result = new ArrayList<>(array.size());
            for(final Object object : array) {
                if(!(object instanceof JsonObject)) {
                    throw new IllegalArgumentException("Expected array to contain only objects, but found " +
                            (object == null ? "null" : object.getClass())
                    );
                }
                result.add(builder.apply((JsonObject) object));
            }
            return ImmutableList.copyOf(result);
        };
    }
}

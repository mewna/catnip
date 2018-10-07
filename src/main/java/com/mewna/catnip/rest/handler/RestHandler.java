package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author amy
 * @since 9/3/18.
 */
abstract class RestHandler {
    @Getter(AccessLevel.PROTECTED)
    private final EntityBuilder entityBuilder;
    
    @Getter(AccessLevel.PROTECTED)
    private final CatnipImpl catnip;
    
    RestHandler(final CatnipImpl catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    static <T> Function<JsonArray, List<T>> mapObjectContents(@Nonnull final Function<JsonObject, T> builder) {
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
    
    @SuppressWarnings("unchecked")
    static <T> T uncheckedCast(@Nullable final Object object) {
        return (T)object;
    }
    
    // Copied from JDA:
    // https://github.com/DV8FromTheWorld/JDA/blob/9e593c5d5e1abf0967998ac5fcc0d915495e0758/src/main/java/net/dv8tion/jda/core/utils/MiscUtil.java#L179-L198
    // Thank JDA devs! <3
    static String encodeUTF8(final String chars) {
        try {
            return URLEncoder.encode(chars, "UTF-8");
        } catch(final UnsupportedEncodingException e) {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }
}

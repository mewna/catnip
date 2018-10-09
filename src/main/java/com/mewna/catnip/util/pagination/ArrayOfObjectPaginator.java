package com.mewna.catnip.util.pagination;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author natanbc
 * @since 10/9/18.
 */
public abstract class ArrayOfObjectPaginator<T, P extends ArrayOfObjectPaginator<T, P>> extends BasePaginator<T, JsonArray, P> {
    private final Function<JsonObject, T> mapper;
    
    public ArrayOfObjectPaginator(@Nonnull final Function<T, String> idOf, @Nonnull final Function<JsonObject, T> mapper,
                                  @Nonnegative final int maxRequestSize) {
        super(idOf, maxRequestSize);
        this.mapper = mapper;
    }
    
    @Override
    protected void update(@Nonnull final RequestState<T> state, @Nonnull final JsonArray data) {
        for(final Object object : data) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all values to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            state.update(mapper.apply((JsonObject)object));
            if(state.done()) {
                return;
            }
        }
    }
}

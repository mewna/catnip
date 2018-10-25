package com.mewna.catnip.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mewna.catnip.Catnip;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 5/9/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface Entity {
    /**
     * Returns the catnip instance associated with this entity.
     *
     * @return The catnip instance of this entity.
     */
    @JsonIgnore
    Catnip catnip();
    
    /**
     * Map this entity instance to a JSON object.
     * @return A JSON object representing this entity.
     */
    @Nonnull
    @JsonIgnore
    default JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
    
    @Nonnull
    @JsonIgnore
    @SuppressWarnings("ClassReferencesSubclass")
    static <T> T fromJson(@Nonnull final Catnip catnip, @Nonnull final Class<T> type, @Nonnull final JsonObject json) {
        final T t = json.mapTo(type);
        // Yeah I know this is a Bad Thing:tm: to do with referencing the
        // subclass, but it was the easiest way to do things :<
        if(t instanceof RequiresCatnip) {
            ((RequiresCatnip) t).catnip(catnip);
        }
        return t;
    }
}

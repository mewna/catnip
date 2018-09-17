package com.mewna.catnip.rest.invite;

import com.mewna.catnip.util.JsonConvertible;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@Accessors(fluent = true)
@Getter
@Setter
public class InviteCreateOptions implements JsonConvertible {
    private int maxAge;
    private int maxUses;
    private boolean temporary;
    private boolean unique;
    
    @Nonnull
    @CheckReturnValue
    public static InviteCreateOptions create() {
        return new InviteCreateOptions();
    }
    
    @Nonnull
    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("max_age", maxAge)
                .put("max_uses", maxUses)
                .put("temporary", temporary)
                .put("unique", unique);
    }
}

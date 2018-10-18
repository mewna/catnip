package com.mewna.catnip.rest.guild;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.entity.guild.Role;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author SamOphis
 * @since 10/18/2018
 */

@Getter(onMethod_ = {@CheckReturnValue, @Nullable})
@Setter(onParam_ = @Nonnull, onMethod_ = {@CheckReturnValue, @Nullable})
@Accessors(fluent = true, chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class MemberData {
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private List<String> roles;
    
    private String nickname;
    private String channelId;
    private Boolean mute;
    private Boolean deaf;
    
    @Nonnull
    @CheckReturnValue
    public Collection<String> roles() {
        return ImmutableList.copyOf(roles);
    }
    
    @CheckReturnValue
    @Nonnull
    public MemberData addRole(@Nonnull final Role role) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        roles.add(role.id());
        return this;
    }
    
    @CheckReturnValue
    @Nonnull
    public MemberData addRole(@Nonnull final String roleId) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        roles.add(roleId);
        return this;
    }
    
    @CheckReturnValue
    @Nonnull
    public JsonObject toJson() {
        final JsonObject object = new JsonObject();
        if (roles != null) {
            object.put("roles", new JsonArray(roles));
        }
        if (nickname != null) {
            object.put("nick", nickname);
        }
        if (mute != null) {
            object.put("mute", mute);
        }
        if (deaf != null) {
            object.put("deaf", deaf);
        }
        if (channelId != null) {
            object.put("channel_id", channelId);
        }
        return object;
    }
}


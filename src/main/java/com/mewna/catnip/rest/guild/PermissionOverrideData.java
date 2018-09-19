package com.mewna.catnip.rest.guild;

import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.PermissionOverride;
import com.mewna.catnip.entity.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.Role;
import com.mewna.catnip.entity.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.JsonConvertible;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.*;

public class PermissionOverrideData implements JsonConvertible {
    private final Set<Permission> allow = EnumSet.noneOf(Permission.class);
    private final Set<Permission> deny = EnumSet.noneOf(Permission.class);
    private final OverrideType type;
    private final String targetId;
    
    public PermissionOverrideData(@Nonnull final RoleData role) {
        type = OverrideType.ROLE;
        targetId = String.valueOf(role.id());
    }
    
    public PermissionOverrideData(@Nonnull final OverrideType type, @Nonnull final String targetId) {
        this.type = type;
        this.targetId = targetId;
    }
    
    public static PermissionOverrideData create(@Nonnull final RoleData role) {
        return new PermissionOverrideData(role);
    }
    
    public static PermissionOverrideData create(@Nonnull final Role role) {
        return new PermissionOverrideData(OverrideType.ROLE, role.id());
    }
    
    public static PermissionOverrideData create(@Nonnull final Member member) {
        return new PermissionOverrideData(OverrideType.MEMBER, member.id());
    }
    
    public static PermissionOverrideData create(@Nonnull final User user) {
        return new PermissionOverrideData(OverrideType.MEMBER, user.id());
    }
    
    public static PermissionOverrideData create(@Nonnull final PermissionOverride override) {
        return new PermissionOverrideData(override.type(), override.id())
                .allow(override.allow())
                .deny(override.deny());
    }
    
    public OverrideType type() {
        return type;
    }
    
    public PermissionOverrideData allow(@Nonnull final Permission... permissions) {
        return allow(Arrays.asList(permissions));
    }
    
    public PermissionOverrideData allow(@Nonnull final Collection<Permission> permissions) {
        deny.removeAll(permissions);
        allow.addAll(permissions);
        return this;
    }
    
    public PermissionOverrideData deny(@Nonnull final Permission... permissions) {
        return deny(Arrays.asList(permissions));
    }
    
    public PermissionOverrideData deny(@Nonnull final Collection<Permission> permissions) {
        allow.removeAll(permissions);
        deny.addAll(permissions);
        return this;
    }
    
    @Nonnull
    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("id", targetId)
                .put("type", type.getKey())
                .put("allow", Permission.from(allow))
                .put("deny", Permission.from(deny));
    }
}

package com.mewna.catnip.entity.impl;

import com.mewna.catnip.entity.Role;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * @author Julia Rogers
 * @since 9/2/18
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleImpl implements Role {
    private String id;
    private String name;
    private int color;
    private boolean hoist;
    private int position;
    private Set<Permission> permissions;
    private boolean managed;
    private boolean mentionable;
}

package com.mewna.catnip.entity;

import lombok.*;

import java.util.Set;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private String id;
    private String nick;
    private Set<String> roles;
    private boolean mute;
    private boolean deaf;
    private String joinedAt;
}

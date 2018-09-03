package com.mewna.catnip.entity;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String id;
    private String discriminator;
    private String avatar;
    private boolean bot;
}

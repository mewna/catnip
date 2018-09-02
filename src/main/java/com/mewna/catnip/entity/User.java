package com.mewna.catnip.entity;

import lombok.*;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String id;
    private String discriminator;
    private String avatar;
    private boolean bot;
}

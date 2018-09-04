package com.mewna.catnip.entity;

import com.mewna.catnip.entity.util.ImageOptions;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 9/4/18.
 */
public interface User {
    @Nonnull
    String username();
    
    @Nonnull
    String id();
    
    @Nonnull
    String discriminator();
    
    @Nonnull
    String avatar();
    
    boolean bot();
    
    boolean isAvatarAnimated();
    
    @Nonnull
    String defaultAvatarUrl();
    
    @Nonnull
    String avatarUrl();
    
    @Nonnull
    String effectiveAvatarUrl(@Nonnull ImageOptions options);
    
    @Nonnull
    String effectiveAvatarUrl();
}

package com.mewna.catnip.entity;

import com.mewna.catnip.rest.invite.InviteCreateOptions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface GuildChannel extends Channel {
    @Nonnull
    @CheckReturnValue
    String name();
    
    @Nonnull
    @CheckReturnValue
    String guildId();
    
    @CheckReturnValue
    int position();
    
    @Nullable
    @CheckReturnValue
    String parentId();
    
    @Override
    @CheckReturnValue
    default boolean isDM() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGroupDM() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGuild() {
        return true;
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<CreatedInvite> createInvite(@Nullable final InviteCreateOptions options) {
        return catnip().rest().channel().createInvite(id(), options);
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<CreatedInvite> createInvite() {
        return createInvite(null);
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<CreatedInvite>> fetchInvites() {
        return catnip().rest().channel().getChannelInvites(id());
    }
}

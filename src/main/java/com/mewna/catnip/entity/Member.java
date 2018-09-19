package com.mewna.catnip.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
public interface Member extends Snowflake {
    
    /**
     * The unique snowflake ID of the user.
     *
     * @return User's ID. Never null.
     */
    @Nonnull
    String id();
    
    /**
     * The id of the guild this role is from.
     *
     * @return String representing the guild ID.
     */
    @Nonnull
    String guildId();
    
    /**
     * The user's nickname in this guild.
     *
     * @return User's nickname. Null if not set.
     */
    @Nullable
    String nick();
    
    /**
     * The user's roles in this guild.
     *
     * @return A {@link Set} of the user's roles.
     */
    @Nonnull
    Set<String> roles();
    
    /**
     * Whether the user is voice muted.
     * <br>Voice muted user cannot transmit voice.
     *
     * @return True if muted, false otherwise.
     */
    boolean mute();
    
    /**
     * Whether the user is deafened.
     * <br>Deafened users cannot receive nor send voice.
     *
     * @return True if deafened, false otherwise.
     */
    boolean deaf();
    
    /**
     * When the user joined the server last.
     * <br>Members who have joined, left, then rejoined will only have the most recent join exposed.
     *
     * @return The {@link OffsetDateTime date and time} the member joined the guild.
     */
    @Nonnull
    OffsetDateTime joinedAt();
    
    /**
     * Creates a DM channel with this member's user.
     *
     * @return Future with the result of the DM creation.
     */
    default CompletableFuture<DMChannel> createDM() {
        return catnip().rest().user().createDM(id());
    }
}

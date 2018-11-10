package com.mewna.catnip.entity.guild;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.impl.MemberImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = MemberImpl.class)
public interface Member extends Snowflake {
    
    /**
     * The unique snowflake ID of the user.
     *
     * @return User's ID. Never null.
     */
    @Nonnull
    String id();
    
    /**
     * The id of the guild this member is from.
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
     * The ids of the user's roles in this guild.
     *
     * @return A {@link Set} of the ids of the user's roles.
     */
    @Nonnull
    @CheckReturnValue
    Set<String> roleIds();
    
    /**
     * The user's roles in this guild.
     *
     * @return A {@link Set} of the user's roles.
     */
    @Nonnull
    @CheckReturnValue
    default Set<Role> roles() {
        return ImmutableSet.copyOf(roleIds().stream()
                .map(e -> catnip().cache().role(guildId(), e))
                .collect(Collectors.toSet()));
    }
    
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
     * <br>Members who have joined, left, then rejoined will only have the most
     * recent join exposed.
     * <br>This may be null under some conditions, ex. a member leaving a
     * guild. In cases like this, catnip will attempt to load the old data from
     * the cache if possible, but it may not work, hence nullability.
     *
     * @return The {@link OffsetDateTime date and time} the member joined the guild.
     */
    @Nullable
    OffsetDateTime joinedAt();
    
    /**
     * Creates a DM channel with this member's user.
     *
     * @return Future with the result of the DM creation.
     */
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<DMChannel> createDM() {
        return catnip().rest().user().createDM(id());
    }
}

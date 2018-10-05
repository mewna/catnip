package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author natanbc
 * @since 9/5/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomEmojiImpl implements CustomEmoji, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String guildId;
    private String name;
    private List<String> roles;
    private User user;
    private boolean requiresColons;
    private boolean managed;
    private boolean animated;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof CustomEmoji && ((CustomEmoji)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("CustomEmoji (%s)", forMessage());
    }
}

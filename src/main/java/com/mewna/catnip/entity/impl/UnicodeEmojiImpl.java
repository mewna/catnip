package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 9/5/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class UnicodeEmojiImpl implements UnicodeEmoji, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String name;
    private boolean requiresColons;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof UnicodeEmoji && ((UnicodeEmoji)obj).name().equals(name);
    }
    
    @Override
    public String toString() {
        return String.format("UnicodeEmoji (%s)", forMessage());
    }
}

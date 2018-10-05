package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Ready;
import com.mewna.catnip.entity.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/4/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReadyImpl implements Ready {
    private transient Catnip catnip;
    
    private int version;
    private User user;
    private List<String> trace;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public String toString() {
        return String.format("Ready (v%s, %s#%s (%s))", version, user.username(), user.discriminator(), user.id());
    }
}

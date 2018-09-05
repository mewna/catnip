package com.mewna.catnip.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author natanbc
 * @since 9/2/18.
 */
public interface Embed {
    @Nullable
    @CheckReturnValue
    String title();
    
    @Nonnull
    @CheckReturnValue
    EmbedType type();
    
    @Nullable
    @CheckReturnValue
    String description();
    
    @Nullable
    @CheckReturnValue
    String url();
    
    @Nullable
    @CheckReturnValue
    OffsetDateTime timestamp();
    
    @Nullable
    @CheckReturnValue
    Integer color();
    
    @Nullable
    @CheckReturnValue
    Footer footer();
    
    @Nullable
    @CheckReturnValue
    Image image();
    
    @Nullable
    @CheckReturnValue
    Thumbnail thumbnail();
    
    @Nullable
    @CheckReturnValue
    Video video();
    
    @Nullable
    @CheckReturnValue
    Provider provider();
    
    @Nullable
    @CheckReturnValue
    Author author();
    
    @Nonnull
    @CheckReturnValue
    List<Field> fields();

    enum EmbedType {
        IMAGE("image"),
        VIDEO("video"),
        LINK("link"),
        RICH("rich"),
        UNKNOWN("");

        @Getter
        private final String key;

        EmbedType(final String key) {
            this.key = key;
        }

        @Nonnull
        @CheckReturnValue
        public static EmbedType byKey(@Nonnull final String key) {
            for(final EmbedType type : values()) {
                if(type.key.equals(key)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
    
    interface Author {
        @Nonnull
        @CheckReturnValue
        String name();
        
        @Nullable
        @CheckReturnValue
        String url();
        
        @Nullable
        @CheckReturnValue
        String iconUrl();
        
        @Nullable
        @CheckReturnValue
        String proxyIconUrl();
    }
    
    interface Field {
        @Nonnull
        @CheckReturnValue
        String name();
        
        @Nonnull
        @CheckReturnValue
        String value();
        
        @CheckReturnValue
        boolean inline();
    }
    
    interface Footer {
        @Nonnull
        @CheckReturnValue
        String text();
        
        @Nullable
        @CheckReturnValue
        String iconUrl();
        
        @Nullable
        @CheckReturnValue
        String proxyIconUrl();
    }

    interface Image {
        @Nonnull
        @CheckReturnValue
        String url();
    
        @Nullable
        @CheckReturnValue
        String proxyUrl();
        
        @CheckReturnValue
        int height();
        
        @CheckReturnValue
        int width();
    }

    interface Provider {
        @Nullable
        @CheckReturnValue
        String name();
    
        @Nonnull
        @CheckReturnValue
        String url();
    }

    interface Thumbnail {
        @Nonnull
        @CheckReturnValue
        String url();
    
        @Nullable
        @CheckReturnValue
        String proxyUrl();
        
        @CheckReturnValue
        int height();
    
        @CheckReturnValue
        int width();
    }

    interface Video {
        @Nonnull
        @CheckReturnValue
        String url();
    
        @CheckReturnValue
        int height();
        
        @CheckReturnValue
        int width();
    }
}

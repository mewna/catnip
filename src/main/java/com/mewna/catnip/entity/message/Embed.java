package com.mewna.catnip.entity.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.EmbedImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.AuthorImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.FieldImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.FooterImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.ImageImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.ProviderImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.ThumbnailImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.VideoImpl;
import lombok.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * All methods of this class (and inner classes) have nullable returns, unless otherwise stated.
 *
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = EmbedImpl.class)
public interface Embed {
    /**
     * Embed title.
     *
     * @return Title of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    String title();
    
    /**
     * Embed type.
     *
     * @return Type of the embed. Never null.
     */
    @Nonnull
    @CheckReturnValue
    EmbedType type();
    
    /**
     * Embed description.
     *
     * @return Description of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    String description();
    
    /**
     * Embed URL.
     *
     * @return URL of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    String url();
    
    /**
     * Embed timestamp.
     *
     * @return Timestamp of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    OffsetDateTime timestamp();
    
    /**
     * Embed color.
     *
     * @return Color of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Integer color();
    
    /**
     * Embed footer.
     *
     * @return Footer of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Footer footer();
    
    /**
     * Embed image.
     *
     * @return Image of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Image image();
    
    /**
     * Embed thumbnail.
     *
     * @return Thumbnail of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Thumbnail thumbnail();
    
    /**
     * Embed video.
     *
     * @return Video of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Video video();
    
    /**
     * Embed provider.
     *
     * @return Provider of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Provider provider();
    
    /**
     * Embed author.
     *
     * @return Author of the embed, or null if absent.
     */
    @Nullable
    @CheckReturnValue
    Author author();
    
    /**
     * Embed fields.
     *
     * @return Fields of the embed. Never null.
     */
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
    
    @JsonDeserialize(as = AuthorImpl.class)
    interface Author {
        /**
         * Name of the author.
         * 
         * @return Embed author's name. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String name();
    
        /**
         * URL of the author.
         *
         * @return Embed author's URL, or null if absent.
         */
        @CheckReturnValue
        String url();
    
        /**
         * Icon URL of the author.
         *
         * @return Embed author's icon URL, or null if absent.
         */
        @CheckReturnValue
        String iconUrl();
    
        /**
         * Icon proxy URL of the author.
         *
         * @return Embed author's icon proxy URL, or null if absent.
         */
        @CheckReturnValue
        String proxyIconUrl();
    }
    
    @JsonDeserialize(as = FieldImpl.class)
    interface Field {
        /**
         * Name of the field.
         *
         * @return Field's name. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String name();
    
        /**
         * Value of the field.
         *
         * @return Field's value. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String value();
    
        /**
         * Whether the field is inline.
         *
         * @return True if the field is inline, false otherwise.
         */
        @CheckReturnValue
        boolean inline();
    }
    
    @JsonDeserialize(as = FooterImpl.class)
    interface Footer {
        /**
         * Text of the footer.
         *
         * @return Footer's text. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String text();
    
        /**
         * Icon URL of the footer.
         *
         * @return Footer's icon URL, or null if absent.
         */
        @CheckReturnValue
        String iconUrl();
    
        /**
         * Icon proxy URL of the footer.
         *
         * @return Footer's icon proxy URL, or null if absent.
         */
        @CheckReturnValue
        String proxyIconUrl();
    }
    
    @JsonDeserialize(as = ImageImpl.class)
    interface Image {
        /**
         * URL of the image.
         * 
         * @return Image's URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
    
        /**
         * Proxy URL of the image.
         *
         * @return Image's proxy URL, or null if absent.
         */
        @CheckReturnValue
        String proxyUrl();
    
        /**
         * Height of the image.
         *
         * @return Image's height, or -1 if absent.
         */
        @CheckReturnValue
        int height();
    
        /**
         * Width of the image.
         *
         * @return Image's width, or -1 if absent.
         */
        @CheckReturnValue
        int width();
    }
    
    @JsonDeserialize(as = ProviderImpl.class)
    interface Provider {
        /**
         * Name of the provider.
         * 
         * @return Provider's name, or null if absent.
         */
        @CheckReturnValue
        String name();
    
        /**
         * URL of the provider.
         *
         * @return Provider's URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
    }
    
    @JsonDeserialize(as = ThumbnailImpl.class)
    interface Thumbnail {
        /**
         * URL of the thumbnail.
         *
         * @return Thumbnail's URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
    
        /**
         * Proxy URL of the thumbnail.
         *
         * @return Thumbnail's proxy URL, or null if absent.
         */
        @CheckReturnValue
        String proxyUrl();
    
        /**
         * Height of the thumbnail.
         *
         * @return Thumbnail's height, or -1 if absent.
         */
        @CheckReturnValue
        int height();
    
        /**
         * Width of the thumbnail.
         *
         * @return Thumbnail's width, or -1 if absent.
         */
        @CheckReturnValue
        int width();
    }
    
    @JsonDeserialize(as = VideoImpl.class)
    interface Video {
        /**
         * URL of the video.
         *
         * @return Video's URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
    
        /**
         * Height of the video.
         *
         * @return Video's height, or -1 if absent.
         */
        @CheckReturnValue
        int height();
    
        /**
         * Width of the video.
         *
         * @return Video's width, or -1 if absent.
         */
        @CheckReturnValue
        int width();
    }
}

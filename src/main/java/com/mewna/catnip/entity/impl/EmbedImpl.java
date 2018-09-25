package com.mewna.catnip.entity.impl;

import com.mewna.catnip.entity.Embed;
import com.mewna.catnip.entity.Embed.Author;
import com.mewna.catnip.entity.Embed.Field;
import com.mewna.catnip.entity.Embed.Footer;
import com.mewna.catnip.entity.Embed.Image;
import com.mewna.catnip.entity.Embed.Provider;
import com.mewna.catnip.entity.Embed.Thumbnail;
import com.mewna.catnip.entity.Embed.Video;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor
@AllArgsConstructor
public class EmbedImpl implements Embed {
    private String title;
    private EmbedType type;
    private String description;
    private String url;
    private OffsetDateTime timestamp;
    private Integer color;
    private Footer footer;
    private Image image;
    private Thumbnail thumbnail;
    private Video video;
    private Provider provider;
    private Author author;
    private List<? extends Field> fields;
    
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<Field> fields() {
        return (List<Field>)fields;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorImpl implements Author {
        private String name;
        private String url;
        private String iconUrl;
        private String proxyIconUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldImpl implements Field {
        private String name;
        private String value;
        private boolean inline;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FooterImpl implements Footer {
        private String text;
        private String iconUrl;
        private String proxyIconUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageImpl implements Image {
        private String url;
        private String proxyUrl;
        private int height;
        private int width;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderImpl implements Provider {
        private String name;
        private String url;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThumbnailImpl implements Thumbnail {
        private String url;
        private String proxyUrl;
        private int height;
        private int width;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoImpl implements Video {
        private String url;
        private int height;
        private int width;
    }
}


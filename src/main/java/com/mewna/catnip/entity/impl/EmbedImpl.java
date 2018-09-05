package com.mewna.catnip.entity.impl;

import com.mewna.catnip.entity.Embed;
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
@NoArgsConstructor
@AllArgsConstructor
public class EmbedImpl implements Embed {
    private String title;
    private EmbedType type;
    private String description;
    private String url;
    private OffsetDateTime timestamp;
    private Integer color;
    private Embed.Footer footer;
    private Embed.Image image;
    private Embed.Thumbnail thumbnail;
    private Embed.Video video;
    private Embed.Provider provider;
    private Embed.Author author;
    private List<? extends Embed.Field> fields;
    
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<Embed.Field> fields() {
        return (List<Embed.Field>)fields;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author implements Embed.Author {
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
    public static class Field implements Embed.Field {
        private String name;
        private String value;
        private boolean inline;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Footer implements Embed.Footer {
        private String text;
        private String iconUrl;
        private String proxyIconUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image implements Embed.Image {
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
    public static class Provider implements Embed.Provider {
        private String name;
        private String url;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Thumbnail implements Embed.Thumbnail {
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
    public static class Video implements Embed.Video {
        private String url;
        private int height;
        private int width;
    }
}


package com.mewna.catnip.entity.impl;

import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
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
public class RichEmbed {
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
    private List<Field> fields;

    public enum EmbedType {
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
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
    public static class Field {
        private String name;
        private String value;
        private boolean inline;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Footer {
        private String text;
        private String iconUrl;
        private String proxyIconUrl;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
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
    public static class Provider {
        private String name;
        private String url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Thumbnail {
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
    public static class Video {
        private String url;
        private int height;
        private int width;
    }
}

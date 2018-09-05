package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.Embed;
import com.mewna.catnip.entity.impl.EmbedImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class EmbedBuilder {
    // @formatter:off
    private String title;
    private String description;
    private String url;
    private Integer color;
    private Footer footer;
    private Image image;
    private Thumbnail thumbnail;
    private Author author;
    private final List<Field> fields = new ArrayList<>();
    // @formatter:on
    
    public EmbedBuilder() {
    }
    
    public EmbedBuilder(final Embed embed) {
        throw new UnsupportedOperationException("Building embeds from existing embeds is currently unsupported");
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder title(@Nullable final String title) {
        this.title = title;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder description(@Nullable final String description) {
        this.description = description;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder url(@Nullable final String url) {
        this.url = url;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder color(@Nullable final Integer color) {
        this.color = color;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder footer(@Nullable final String text) {
        return footer(text, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder footer(@Nullable final String text, @Nullable final String iconUrl) {
        return footer(new Footer(text, iconUrl, null));
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder footer(@Nullable final Footer footer) {
        this.footer = footer;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder image(@Nullable final String url) {
        image = new Image(url, null, 0, 0);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder thumbnail(@Nullable final String url) {
        thumbnail = new Thumbnail(url, null, 0, 0);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder author(@Nullable final String name) {
        return author(name, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder author(@Nullable final String name, @Nullable final String url) {
        return author(name, url, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder author(@Nullable final String name, @Nullable final String url, @Nullable final String iconUrl) {
        return author(new Author(name, url, iconUrl, null));
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder author(@Nullable final Author author) {
        this.author = author;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder field(@Nonnull final String name, @Nonnull final String value, final boolean inline) {
        return field(new Field(name, value, inline));
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder field(@Nonnull final Field field) {
        if(fields.size() == 25) {
            throw new IllegalStateException("Tried to add an embed field, but we're at the cap (25)!");
        }
        fields.add(field);
        return this;
    }
    
    public Embed build() {
        final EmbedImplBuilder builder = EmbedImpl.builder();
        if(title != null && !title.isEmpty()) {
            builder.title(title);
        }
        if(description != null && !description.isEmpty()) {
            builder.description(description);
        }
        if(url != null && !url.isEmpty()) {
            builder.url(url);
        }
        if(color != null) {
            builder.color(color);
        }
        if(footer != null) {
            builder.footer(footer);
        }
        if(image != null) {
            builder.image(image);
        }
        if(thumbnail != null) {
            builder.thumbnail(thumbnail);
        }
        if(author != null) {
            builder.author(author);
        }
        if(fields.isEmpty()) {
            builder.fields(Collections.emptyList());
        } else {
            if(fields.size() > 25) {
                throw new IllegalStateException("Tried to add an embed field, but we're at the cap (25)!");
            }
            builder.fields(fields);
        }
        return builder.build();
    }
}

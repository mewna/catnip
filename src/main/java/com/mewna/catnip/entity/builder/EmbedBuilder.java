package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Embed.Author;
import com.mewna.catnip.entity.message.Embed.Field;
import com.mewna.catnip.entity.message.Embed.Footer;
import com.mewna.catnip.entity.message.Embed.Image;
import com.mewna.catnip.entity.message.Embed.Thumbnail;
import com.mewna.catnip.entity.impl.EmbedImpl;
import com.mewna.catnip.entity.impl.EmbedImpl.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author amy
 * @since 9/4/18.
 */
@Setter(onParam_ = @Nullable, onMethod_ = {@CheckReturnValue, @Nonnull})
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
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
    
    public EmbedBuilder(final Embed embed) {
        title = embed.title();
        description = embed.description();
        url = embed.url();
        color = embed.color();
        footer = embed.footer();
        image = embed.image();
        thumbnail = embed.thumbnail();
        author = embed.author();
        fields.addAll(embed.fields());
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder color(@Nullable final Color color) {
        if (color != null) {
            // Mask off the alpha bits
            this.color = color.getRGB() & 0x00FFFFFF;
        }
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder footer(@Nullable final String text, @Nullable final String iconUrl) {
        return footer(new FooterImpl(text, iconUrl, null));
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder image(@Nullable final String url) {
        image = new ImageImpl(url, null, 0, 0);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public EmbedBuilder thumbnail(@Nullable final String url) {
        thumbnail = new ThumbnailImpl(url, null, 0, 0);
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
        return author(new AuthorImpl(name, url, iconUrl, null));
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
        return field(new FieldImpl(name, value, inline));
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
        int len = 0;
        final EmbedImplBuilder builder = EmbedImpl.builder();
        if(title != null && !title.isEmpty()) {
            if(title.length() > 256) {
                throw new IllegalStateException("Title exceeds 256 characters!");
            }
            len += title.length();
            builder.title(title);
            
        }
        if(description != null && !description.isEmpty()) {
            if(description.length() > 2048) {
                throw new IllegalStateException("Description exceeds 2048 characters!");
            }
            builder.description(description);
            len += description.length();
        }
        if(url != null && !url.isEmpty()) {
            builder.url(url);
        }
        if(color != null) {
            builder.color(color);
        }
        if(footer != null) {
            if(footer.text().length() > 2048) {
                throw new IllegalStateException("Footer text exceeds 2048 characters!");
            }
            builder.footer(footer);
            len += footer.text().length();
        }
        if(image != null) {
            builder.image(image);
        }
        if(thumbnail != null) {
            builder.thumbnail(thumbnail);
        }
        if(author != null) {
            if(author.name().length() > 256) {
                throw new IllegalStateException("Author's name exceeds 256 characters!");
            }
            len += author.name().length();
            builder.author(author);
        }
        if(fields.isEmpty()) {
            builder.fields(Collections.emptyList());
        } else {
            if(fields.size() > 25) {
                throw new IllegalStateException("Tried to add an embed field, but we're at the cap (25)!");
            }
            for(final Field field : fields) {
                if(field.name().length() > 256) {
                    throw new IllegalStateException("Field name exceeds 256 characters!");
                }
                if(field.value().length() > 1024) {
                    throw new IllegalStateException("Field value exceeds 1024 characters!");
                }
                len += field.name().length();
                len += field.value().length();
            }
            builder.fields(fields);
        }
        if(len > 6000) {
            throw new IllegalStateException("Total embed length exceeds 6000 characters!");
        }
        return builder.build();
    }
}
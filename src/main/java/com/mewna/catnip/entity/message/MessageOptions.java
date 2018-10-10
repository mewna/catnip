package com.mewna.catnip.entity.message;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import io.vertx.core.buffer.Buffer;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author SamOphis
 * @since 10/10/2018
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class MessageOptions {
    private String content;
    private Embed embed;
    
    @Setter(AccessLevel.NONE)
    private List<ImmutablePair<String, Buffer>> files;
    
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final File file) {
        return addFile(file.getName(), file);
    }
    
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("file doesn't exist!");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("file cannot be read!");
        }
        try {
            return addFile(name, Files.toByteArray(file));
        } catch (final IOException exc) {
            throw new IllegalArgumentException("cannot read data from file!", exc);
        }
    }
    
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final InputStream stream) {
        try {
            return addFile(name, ByteStreams.toByteArray(stream));
        } catch (final IOException exc) {
            throw new IllegalArgumentException("cannot read data from inputstream!", exc);
        }
    }
    
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final byte[] data) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(new ImmutablePair<>(name, Buffer.buffer(data)));
        return this;
    }
    
    @CheckReturnValue
    public boolean hasFiles() {
        return files != null; // because checking via getter creates a new list each time.
    }
    
    public List<ImmutablePair<String, Buffer>> files() {
        return ImmutableList.copyOf(files);
    }
}

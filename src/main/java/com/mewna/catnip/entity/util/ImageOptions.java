package com.mewna.catnip.entity.util;

import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@Getter
public class ImageOptions {
    private ImageType type = ImageType.PNG;
    private int size = -1;
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions type(@Nullable ImageType type) {
        if(type == null) {
            type = ImageType.PNG;
        }
        this.type = type;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions gif() {
        return type(ImageType.GIF);
    }
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions jpg() {
        return type(ImageType.JPG);
    }
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions png() {
        return type(ImageType.PNG);
    }
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions webp() {
        return type(ImageType.WEBP);
    }
    
    @Nonnull
    @CheckReturnValue
    public ImageOptions size(@Nonnegative final int size) {
        if(size < 16) {
            throw new IllegalArgumentException("Size must be greater than or equal to 16");
        }
        if(size > 2048) {
            throw new IllegalArgumentException("Size must be smaller than or equal to 2048");
        }
        if(!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Size must be a power of two");
        }
        this.size = size;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public String buildUrl(@Nonnull final String base) {
        return base + '.' + type.getFileExtension() + (size == -1 ? "" : "?size=" + size);
    }
    
    private static boolean isPowerOfTwo(final int i) {
        final int minusOne = i - 1;
        return (i & minusOne) == 0;
    }
}

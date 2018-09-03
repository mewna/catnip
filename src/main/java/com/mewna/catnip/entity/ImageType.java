package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public enum ImageType {
    GIF, JPG, PNG, WEBP;
    
    private final String alternativeExtension;
    
    ImageType(@Nonnull final String alternativeExtension) {
        this.alternativeExtension = alternativeExtension;
    }
    
    ImageType() {
        this(null);
    }
    
    @Nonnull
    @CheckReturnValue
    public String getFileExtension() {
        return name().toLowerCase();
    }
    
    @Nonnull
    @CheckReturnValue
    public static ImageType fromExtension(@Nonnull final String extension) {
        for(final ImageType type : values()) {
            if(type.getFileExtension().equals(extension)) {
                return type;
            }
            if(type.alternativeExtension != null && type.alternativeExtension.equals(extension)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching file type for extension " + extension);
    }
}

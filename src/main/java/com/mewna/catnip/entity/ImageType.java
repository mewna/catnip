package com.mewna.catnip.entity;

public enum ImageType {
    GIF, JPG, PNG, WEBP;
    
    private final String alternativeExtension;
    
    ImageType(final String alternativeExtension) {
        this.alternativeExtension = alternativeExtension;
    }
    
    ImageType() {
        this(null);
    }
    
    public String getFileExtension() {
        return name().toLowerCase();
    }
    
    public static ImageType fromExtension(final String extension) {
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

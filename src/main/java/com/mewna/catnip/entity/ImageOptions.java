package com.mewna.catnip.entity;

import lombok.Getter;

@Getter
public class ImageOptions {
    private ImageType type = ImageType.PNG;
    private int size = -1;
    
    public ImageOptions type(ImageType type) {
        if(type == null) {
            type = ImageType.PNG;
        }
        this.type = type;
        return this;
    }
    
    public ImageOptions gif() {
        return type(ImageType.GIF);
    }
    
    public ImageOptions jpg() {
        return type(ImageType.JPG);
    }
    
    public ImageOptions png() {
        return type(ImageType.PNG);
    }
    
    public ImageOptions webp() {
        return type(ImageType.WEBP);
    }
    
    public ImageOptions size(final int size) {
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
    
    public String buildUrl(final String base) {
        return base + '.' + type.getFileExtension() + (size == -1 ? "" : "?size=" + size);
    }
    
    private static boolean isPowerOfTwo(final int i) {
        final int minusOne = i - 1;
        return (i & minusOne) == 0;
    }
}

package com.mewna.catnip.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class Utils {
    public static final List<String> VALID_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif"
    );
    public static final long DISCORD_EPOCH = 1420070400000L;
    
    private Utils() {}
    
    @Nonnull
    @CheckReturnValue
    public static OffsetDateTime creationTimeOf(final long id) {
        final long discordTimestamp = id >> 22;
        final Instant instant = Instant.ofEpochMilli(discordTimestamp + DISCORD_EPOCH);
        return instant.atOffset(ZoneOffset.UTC);
    }
    
    public static void validateImageUri(@Nonnull final URI imageUri) {
        if(!imageUri.getScheme().equals("data")) {
            throw new IllegalArgumentException("Only data URIs are supported");
        }
        final String data = imageUri.getSchemeSpecificPart();
        final int endContentType = data.indexOf(';');
        if(endContentType == -1) {
            throw new IllegalArgumentException("Malformed URI: unable to find end of content type");
        }
        final String contentType = data.substring(0, endContentType);
        if(!VALID_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Content type of " + contentType + " does not match " +
                    "expected values " + VALID_CONTENT_TYPES);
        }
        if(!data.startsWith("base64,", contentType.length() + 1)) {
            throw new IllegalArgumentException("Content not base64 encoded");
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public static URI asImageDataUri(@Nonnull final byte[] bytes) {
        final String contentType = probeContentType(bytes, "image/jpeg").toLowerCase();
        if(!VALID_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Content type of " + contentType + " does not match " +
                    "expected values " + VALID_CONTENT_TYPES);
        }
        return asImageDataUri(bytes, contentType);
    }
    
    @Nonnull
    @CheckReturnValue
    public static URI asImageDataUri(@Nonnull final byte[] bytes, @Nonnull final String forceContentType) {
        final String uri = "data:" + forceContentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        try {
            return new URI(uri);
        } catch(final URISyntaxException e) {
            throw new IllegalArgumentException("Unable to build URI", e);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public static String probeContentType(@Nonnull final byte[] bytes, @Nonnull final String defaultValue) {
        final String probed = probeContentType(bytes);
        if(probed == null) {
            return defaultValue;
        }
        return probed;
    }
    
    @Nullable
    @CheckReturnValue
    public static String probeContentType(@Nonnull final byte[] bytes) {
        try {
            return URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
        } catch(final IOException e) {
            return null;
        }
    }
}

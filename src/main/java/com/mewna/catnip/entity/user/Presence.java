package com.mewna.catnip.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author amy
 * @since 9/21/18.
 */
@SuppressWarnings("unused")
public interface Presence {
    @Nonnull
    OnlineStatus status();
    
    @Nullable
    Activity activity();
    
    @Accessors(fluent = true, chain = true)
    enum OnlineStatus {
        ONLINE,
        IDLE,
        DND,
        OFFLINE,;
        
        @Nonnull
        public static OnlineStatus fromString(@Nonnull final String status) {
            switch(status) {
                case "online": {
                    return ONLINE;
                }
                case "idle": {
                    return IDLE;
                }
                case "dnd": {
                    return DND;
                }
                case "offline": {
                    return OFFLINE;
                }
                default: {
                    throw new IllegalArgumentException("Unknown status: " + status);
                }
            }
        }
        
        @Nonnull
        public String asString() {
            return name().toLowerCase();
        }
    }
    
    @Accessors(fluent = true, chain = true)
    @RequiredArgsConstructor
    enum ActivityType {
        PLAYING(0),
        STREAMING(1),
        LISTENING(2),
        WATCHING(3),;
        @Getter
        private final int id;
        
        @Nonnull
        public static ActivityType byId(@Nonnegative final int id) {
            for(final ActivityType type : values()) {
                if(type.id == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown ActivityType: " + id);
        }
        
        @Nonnull
        public String asString() {
            return name().toLowerCase();
        }
    }
    
    @Accessors(fluent = true, chain = true)
    @RequiredArgsConstructor
    enum ActivityFlag {
        INSTANCE(1), // 1 << 0
        JOIN(1 << 1),
        SPECTATE(1 << 2),
        JOIN_REQUEST(1 << 3),
        SYNC(1 << 4),
        PLAY(1 << 5),;
        
        private final int bits;
        
        @Nonnull
        public static Set<ActivityFlag> fromInt(final int flags) {
            final Set<ActivityFlag> set = EnumSet.noneOf(ActivityFlag.class);
            for(final ActivityFlag flag : values()) {
                if((flags & flag.bits) == flag.bits) {
                    set.add(flag);
                }
            }
            return set;
        }
    }
    
    interface ActivityTimestamps {
        long start();
        
        long end();
    }
    
    interface ActivityParty {
        @Nullable
        String id();
        
        int currentSize();
        
        int maxSize();
    }
    
    interface ActivityAssets {
        @Nullable
        String largeImage();
        
        @Nullable
        String largeText();
        
        @Nullable
        String smallImage();
        
        @Nullable
        String smallText();
    }
    
    interface ActivitySecrets {
        @Nullable
        String join();
        
        @Nullable
        String spectate();
        
        @Nullable
        String match();
    }
    
    interface Activity {
        @Nonnull
        String name();
        
        @Nonnull
        ActivityType type();
        
        @Nullable
        String url();
        
        @Nullable
        ActivityTimestamps timestamps();
        
        @Nullable
        String applicationId();
        
        @Nullable
        String details();
        
        @Nullable
        String state();
        
        @Nullable
        ActivityParty party();
        
        @Nullable
        ActivityAssets assets();
        
        @Nullable
        ActivitySecrets secrets();
        
        boolean instance();
        
        @Nullable
        Set<ActivityFlag> flags();
    }
}

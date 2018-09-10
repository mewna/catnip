package com.mewna.catnip.extension.manager;

import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DefaultExtensionManager implements ExtensionManager {
    @Getter
    private final Catnip catnip;
    private final Collection<Extension> loadedExtensions = new ConcurrentHashSet<>();
    
    @Override
    public ExtensionManager loadExtension(@Nonnull final Extension extension) {
        if(!loadedExtensions.contains(extension)) {
            extension.catnip(catnip);
            catnip.vertx().deployVerticle(extension);
            loadedExtensions.add(extension);
        }
        return this;
    }
    
    @Override
    public ExtensionManager unloadExtension(@Nonnull final Extension extension) {
        if(loadedExtensions.contains(extension)) {
            catnip.vertx().undeploy(extension.deploymentID());
            loadedExtensions.remove(extension);
        }
        return this;
    }
    
    @Nonnull
    @Override
    public Set<Extension> matchingExtensions(@Nonnull final String regex) {
        return ImmutableSet.copyOf(loadedExtensions.stream()
                .filter(e -> e.name().matches(regex))
                .collect(Collectors.toSet()));
    }
    
    @Nonnull
    @Override
    public Set<Extension> matchingExtensions(@Nonnull final Class<? extends Extension> extensionClass) {
        return ImmutableSet.copyOf(loadedExtensions.stream()
                .filter(e -> extensionClass.isAssignableFrom(e.getClass()))
                .collect(Collectors.toSet()));
    }
}

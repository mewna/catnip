package com.mewna.catnip.extension.manager;

import com.mewna.catnip.extension.Extension;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * An {@link ExtensionManager} implementation is exactly what it sounds like:
 * it manages {@link Extension}s and takes care of lifecycle hooks, injecting
 * {@link com.mewna.catnip.Catnip} instances, and so on. Proper implementations
 * of this class will, among other things, pay attention to the caveats
 * mentioned in the {@link Extension} docs, to ensure compatibility with the
 * {@link DefaultExtensionManager}.
 *
 * @author amy
 * @since 9/6/18
 */
public interface ExtensionManager {
    /**
     * Load the given extension instance. Note than an extension may not be
     * loaded more than once, and attempting to load an extension multiple
     * times will no-op.
     *
     * @param extension The extension to load.
     */
    void loadExtension(@Nonnull Extension extension);
    
    /**
     * Unload the given extension instance. If the extension is not already
     * loaded, this method will be a no-op.
     *
     * @param extension The extension to unload.
     */
    void unloadExtension(@Nonnull Extension extension);
    
    /**
     * Get all loaded extensions whose names match the specified regex. This
     * method will only return extensions loaded by the current instance.
     *
     * @param regex The regex to match extension names against.
     *
     * @return A possibly-empty set of extensions that have names matching the
     * supplied regex.
     */
    @Nonnull
    Set<Extension> matchingExtensions(@Nonnull String regex);
    
    /**
     * Get all loaded extensions that are instantiated from the given class.
     * This method will only return extensions loaded by the current instance.
     *
     * @param extensionClass The extension class to find instances of.
     *
     * @return A possibly-empty set of extensions that are instances of the
     * supplied class.
     */
    @Nonnull
    Set<Extension> matchingExtensions(@Nonnull Class<? extends Extension> extensionClass);
}

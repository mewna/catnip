package com.mewna.catnip.extension;

import com.mewna.catnip.Catnip;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <strong>If you are unsure if you need to implement this interface, you
 * probably want {@link AbstractExtension} instead.</strong>
 * <p/>
 * An extension is like a "plugin" of sorts - basically, it gives a
 * standardized way to provide extra functionality on top of catnip that is
 * *not* user-level application code. An example of this is providing a command
 * library of some sort. Your library code would be implemented as an extension
 * that an end-user could then optionally load in.
 * <p/>
 * Note that this class extends {@link Verticle}. This is because extensions
 * are expected to be deployed as vert.x verticles. A proper catnip
 * implementation will keep track of these deployments to allow unloading or
 * reloading extensions at runtime. Since this class extends {@link Verticle},
 * the easiest way for a custom extension to be "compliant" is to simply extend
 * {@link AbstractExtension}, which itself extends {@link AbstractVerticle}, as
 * well as implementing this interface.
 * <p/>
 * Extensions are deployed as vert.x verticles. vert.x provides two lifecycle
 * hooks - {@link Verticle#start(Future)} and {@link Verticle#stop(Future)} -
 * that should be used for managing an extension's lifecycle. This is not, say,
 * a Bukkit server, and as such, there is no need for lifecycle hooks like
 * {@code preload} or {@code unload} or similar to be provided. It is possible
 * that this may change in a future catnip update, but is currently unlikely.
 * Note that {@link AbstractVerticle#start()} and {@link AbstractVerticle#stop()}
 * are provided as convenience methods for when a {@link Future} is not needed,
 * and will automatically be available if implementations are based off of
 * {@link AbstractExtension} or {@link AbstractVerticle}.
 * <p/>
 * Note that the lifecycle callbacks are called <strong>SYNCHRONOUSLY</strong>
 * by vert.x, and as such you should take care to not block the event loop in
 * those callbacks!
 *
 * @author amy
 * @since 9/6/18
 */
@SuppressWarnings("unused")
public interface Extension extends Verticle {
    /**
     * The name of this extension. Note that an extension's name is
     * <strong>NOT</strong> guaranteed unique, and so your code should NOT rely
     * on this for uniqueness!
     *
     * @return The name of this extension.
     */
    @Nonnull
    String name();
    
    /**
     * The catnip instance this extension is registered to. This will not be
     * {@code null}, as a proper extension manager implementation should inject
     * the current catnip instance BEFORE deploying the extension.
     *
     * @return The catnip instance this extension is registered to.
     */
    @Nonnull
    Catnip catnip();
    
    /**
     * Inject a catnip instance into this extension. This should be the same as
     * the catnip version that is deploying this extension, and must not be
     * {@code null}. A proper extension manager implementation will call this
     * method to inject a catnip instance BEFORE deploying the extension, ie.
     * {@link Verticle#start(Future)} / {@link AbstractVerticle#start()} will
     * be called AFTER this method.
     *
     * @param catnip The catnip instance to inject. May not be {@code null}.
     */
    Extension catnip(@Nonnull Catnip catnip);
    
    // TODO: Verify that AbstractVerticle counts as implementing this
    String deploymentID();
}

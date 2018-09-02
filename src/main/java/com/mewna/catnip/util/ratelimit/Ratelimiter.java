package com.mewna.catnip.util.ratelimit;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * @author amy
 * @since 8/16/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface Ratelimiter {
    /**
     * Checks if the id is ratelimited, based on the period and the
     * limit-per-period.
     *
     * @param id       id to check
     * @param periodMs period of ratelimit reset
     * @param limit    max "uses" before ratelimit for a given period is hit
     *
     * @return A (isRatelimited, amountRemaining) tuple
     */
    ImmutablePair<Boolean, Long> checkRatelimit(String id, long periodMs, long limit);
}

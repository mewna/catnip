package com.mewna.catnip.util.pagination;

/**
 * @author natanbc
 * @since 10/10/18
 */
@FunctionalInterface
public interface PaginationCallback<T> {
    /**
     * Accepts a fetched entity. Pagination stops if this method returns false.
     *
     * @param entity Entity fetched.
     *
     * @return True, if more entities should be paginated.
     */
    boolean accept(T entity);
}

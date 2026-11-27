package repository;

import domain.Entity;
import util.paging.Page;
import util.paging.Pageable;



public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {

    /**
     * Retrieves a single page of entities, ordered by identifier.
     *
     * @param pageable the requested page (zero-based page number and page size), must not be null
     * @return a {@link Page} holding the entities on that page and the total number of stored entities;
     * the element list is empty if the requested page lies after the last one
     * @throws IllegalArgumentException if pageable is null
     */
    Page<E> findAllOnPage(Pageable pageable);
}

package domain;

/**
 * Generic contract representing an identifiable entity in the domain model.
 *
 * @param <ID> the type of the unique identifier
 */
public interface Entity<ID> {

    /**
     * Retrieves the unique identifier of the entity.
     *
     * @return the unique entity ID
     */
    ID getId();
}
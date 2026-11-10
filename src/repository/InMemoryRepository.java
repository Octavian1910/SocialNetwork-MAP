package repository;

import domain.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository implementation utilizing a hash map for storage.
 *
 * @param <ID> the identifier type
 * @param <E>  the entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    protected final Map<ID, E> entities = new HashMap<>();

    public InMemoryRepository() {
    }

    @Override
    public void add(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null!");
        }
        ID id = entity.getId();
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null!");
        }
        if (entities.containsKey(id)) {
            throw new RepositoryException("Entity with ID " + id + " already exists!");
        }
        entities.put(id, entity);
    }

    @Override
    public Optional<E> update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null!");
        }
        ID id = entity.getId();
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null!");
        }
        if (!entities.containsKey(id)) {
            return Optional.of(entity);
        }
        entities.put(id, entity);
        return Optional.empty();
    }

    @Override
    public Optional<E> remove(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }
        return Optional.ofNullable(entities.remove(id));
    }

    @Override
    public Optional<E> findById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Collection<E> findAll() {
        return new ArrayList<>(entities.values());
    }
}
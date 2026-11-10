package repository;

import domain.FriendShip;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Concrete file repository storing {@link FriendShip} entities.
 */
public class FriendShipFileRepository extends AbstractFileRepository<String, FriendShip> {

    public FriendShipFileRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected FriendShip extractEntity(String line) {
        String[] fields = line.split(";", -1);
        if (fields.length != 3) {
            throw new RepositoryException("Invalid friendship format (expected 3 tokens): " + line);
        }
        try {
            int firstUserId = Integer.parseInt(fields[0].trim());
            int secondUserId = Integer.parseInt(fields[1].trim());
            LocalDateTime date = LocalDateTime.parse(fields[2].trim());
            return new FriendShip(firstUserId, secondUserId, date);
        } catch (NumberFormatException e) {
            throw new RepositoryException("Invalid numeric IDs in friendship line: " + line, e);
        } catch (DateTimeParseException e) {
            throw new RepositoryException("Invalid ISO date format in line: " + line, e);
        }
    }

    @Override
    protected String createEntityAsString(FriendShip friendShip) {
        return friendShip.getFirstUserId() + ";" +
                friendShip.getSecondUserId() + ";" +
                friendShip.getDate().toString();
    }

    /**
     * Normalizes a friendship composite key to the internal form "smallerId-largerId".
     */
    private String normalizeId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }
        String[] parts = id.split("-", -1);
        if (parts.length != 2) {
            throw new RepositoryException("Invalid friendship ID format (expected 'id1-id2'): " + id);
        }
        try {
            int first = Integer.parseInt(parts[0].trim());
            int second = Integer.parseInt(parts[1].trim());
            return Math.min(first, second) + "-" + Math.max(first, second);
        } catch (NumberFormatException e) {
            throw new RepositoryException("Invalid numeric ID within composite key: " + id, e);
        }
    }

    @Override
    public Optional<FriendShip> findById(String id) {
        return super.findById(normalizeId(id));
    }

    @Override
    public Optional<FriendShip> remove(String id) {
        return super.remove(normalizeId(id));
    }
}
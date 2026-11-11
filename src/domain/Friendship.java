package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing an undirected friendship relationship between two users.
 * The identifier is consistently normalized as "smallerId-largerId".
 */
public class Friendship implements Entity<String> {
    private final Long firstUserId;
    private final Long secondUserId;
    private final LocalDateTime date;

    /**
     * Constructs a Friendship with an explicit creation timestamp.
     *
     * @param firstUserId  ID of the first user
     * @param secondUserId ID of the second user
     * @param date         the timestamp when the friendship was formed
     */
    public Friendship(Long firstUserId, Long secondUserId, LocalDateTime date) {
        if (firstUserId == null || secondUserId == null) {
            throw new IllegalArgumentException("User IDs in friendship must not be null!");
        }
        this.firstUserId = Math.min(firstUserId, secondUserId);
        this.secondUserId = Math.max(firstUserId, secondUserId);
        this.date = date;
    }

    /**
     * Constructs a Friendship using the current timestamp.
     *
     * @param firstUserId  ID of the first user
     * @param secondUserId ID of the second user
     */
    public Friendship(Long firstUserId, Long secondUserId) {
        this(firstUserId, secondUserId, LocalDateTime.now());
    }

    public Long getFirstUserId() {
        return firstUserId;
    }

    public Long getSecondUserId() {
        return secondUserId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String getId() {
        return firstUserId + "-" + secondUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(firstUserId, that.firstUserId) &&
                Objects.equals(secondUserId, that.secondUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstUserId, secondUserId);
    }

    @Override
    public String toString() {
        return "FriendShip{firstUserId=" + firstUserId +
                ", secondUserId=" + secondUserId +
                ", date=" + date + "}";
    }
}
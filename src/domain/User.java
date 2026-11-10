package domain;

import java.util.Objects;

/**
 * Domain entity representing an individual registered in the social network.
 */
public class User implements Entity<Integer> {
    private final int id;
    private final String firstName;
    private final String lastName;

    /**
     * Constructs a new User.
     *
     * @param id        the unique integer identifier
     * @param firstName the user's first name
     * @param lastName  the user's last name
     */
    public User(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", firstName='" + firstName + "', lastName='" + lastName + "'}";
    }
}
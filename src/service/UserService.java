package service;

import domain.User;
import domain.validator.ValidationException;
import domain.validator.Validator;
import repository.Repository;
import repository.RepositoryException;

import java.util.Collection;
import java.util.Optional;

/**
 * Service orchestrating operations on User entities.
 */
public class UserService {
    private final Repository<Integer, User> repository;
    private final Validator<User> validator;

    public UserService(Repository<Integer, User> repository, Validator<User> validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /**
     * Validates and adds a new user.
     *
     * @param id        user unique identifier
     * @param firstName user's first name
     * @param lastName  user's last name
     * @throws ValidationException if user violates domain rules
     * @throws RepositoryException if user with the same ID already exists
     */
    public void addUser(Integer id, String firstName, String lastName) {
        User user = new User(id, firstName, lastName);
        validator.validate(user);
        repository.add(user);
    }

    /**
     * Removes an existing user by identifier.
     *
     * @param id user ID
     * @throws ServiceException if the user does not exist
     */
    public void removeUser(Integer id) {
        Optional<User> removed = repository.remove(id);
        if (removed.isEmpty()) {
            throw new ServiceException("User with ID " + id + " does not exist and cannot be deleted!");
        }
    }

    /**
     * Finds a user by identifier.
     *
     * @param id user ID
     * @return the User instance
     * @throws ServiceException if user was not found
     */
    public User findUserById(Integer id) {
        return repository.findById(id)
                .orElseThrow( () -> new ServiceException("User with ID " + id + " does not exist!"));
    }

    /**
     * Retrieves all registered users.
     *
     * @return collection of users
     */
    public Collection<User> getAllUsers() {
        return repository.findAll();
    }
}
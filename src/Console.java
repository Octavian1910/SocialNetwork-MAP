import domain.Friendship;
import domain.User;
import domain.validator.ValidationException;
import repository.RepositoryException;
import repository.factory.RepositoryType;
import service.FriendShipService;
import service.NetworkService;
import service.ServiceException;
import service.UserService;

import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

/**
 * Text-based console user interface.
 */
public class Console {
    private final UserService userService;
    private final FriendShipService friendShipService;
    private final NetworkService networkService;
    private final Scanner scanner = new Scanner(System.in);
    private final RepositoryType repositoryType;

    public Console(UserService userService,
                   FriendShipService friendShipService,
                   NetworkService networkService,
                   RepositoryType repositoryType) {
        this.userService = userService;
        this.friendShipService = friendShipService;
        this.networkService = networkService;
        this.repositoryType = repositoryType;
    }

    public void run() {
        int option = -1;
        do {
            printMenu();
            try {
                option = Integer.parseInt(scanner.nextLine().trim());
                switch (option) {
                    case 1 -> showAllUsers();
                    case 2 -> addUser();
                    case 3 -> removeUser();
                    case 4 -> showAllFriendships();
                    case 5 -> addFriendship();
                    case 6 -> removeFriendship();
                    case 7 -> showNumberOfCommunities();
                    case 8 -> showMostSociableCommunity();
                    case 0 -> System.out.println("Exiting application. Goodbye!");
                    default -> System.out.println("Invalid option! Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input Error: Please enter a valid numeric value!");
            } catch (ValidationException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Repository Error: " + e.getMessage());
            } catch (ServiceException e) {
                System.out.println("Service Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Argument Error: " + e.getMessage());
            }
        } while (option != 0);
    }

    private void printMenu() {
        System.out.println("\n=== SOCIAL NETWORK ===");
        System.out.println("1. Show all users");
        System.out.println("2. Add user");
        System.out.println("3. Remove user");
        System.out.println("4. Show all friendships");
        System.out.println("5. Add friendship");
        System.out.println("6. Remove friendship");
        System.out.println("7. Number of communities");
        System.out.println("8. Most sociable community");
        System.out.println("0. Exit");
        System.out.print("Option: ");
    }

    private void showAllUsers() {
        Collection<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("There are no registered users.");
            return;
        }
        for (User user : users) {
            System.out.println(user);
        }
    }

    private void addUser() {
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        User savedUser;
        if (repositoryType == RepositoryType.DATABASE) {
            savedUser = userService.addUser(firstName, lastName);
        } else {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            savedUser = userService.addUser(id, firstName, lastName);
        }

        System.out.println("User added successfully with ID: " + savedUser.getId());
    }

    private void removeUser() {
        System.out.print("User ID to remove: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        networkService.removeUserAndFriendships(id);
        System.out.println("User and associated friendships have been removed!");
    }

    private void showAllFriendships() {
        Collection<Friendship> friendships = friendShipService.getAllFriendShips();
        if (friendships.isEmpty()) {
            System.out.println("There are no registered friendships.");
            return;
        }
        for (Friendship fs : friendships) {
            System.out.println(fs);
        }
    }

    private void addFriendship() {
        System.out.print("First user ID: ");
        Long firstId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Second user ID: ");
        Long secondId = Long.parseLong(scanner.nextLine().trim());
        friendShipService.addFriendShip(firstId, secondId);
        System.out.println("Friendship added successfully!");
    }

    private void removeFriendship() {
        System.out.print("First user ID: ");
        Long firstId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Second user ID: ");
        Long secondId = Long.parseLong(scanner.nextLine().trim());
        friendShipService.removeFriendShip(firstId, secondId);
        System.out.println("Friendship removed successfully!");
    }

    private void showNumberOfCommunities() {
        int count = networkService.getNumberOfCommunities();
        System.out.println("Number of communities (connected components): " + count);
    }

    private void showMostSociableCommunity() {
        Set<Long> community = networkService.getMostSociableCommunity();
        if (community.isEmpty()) {
            System.out.println("There are no communities.");
            return;
        }
        System.out.println("The most sociable community contains users:");
        for (Long userId : community) {
            System.out.println(" - " + userService.findUserById(userId));
        }
    }
}
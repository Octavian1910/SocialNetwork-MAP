package ui;

import domain.FriendShip;
import domain.User;
import domain.validator.ValidationException;
import repository.RepositoryException;
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

    public Console(UserService userService,
                   FriendShipService friendShipService,
                   NetworkService networkService) {
        this.userService = userService;
        this.friendShipService = friendShipService;
        this.networkService = networkService;
    }

    /**
     * Starts the interactive command-line interface loop.
     */
    public void run() {
        int option = -1;
        do {
            printMenu();
            try {
                option = Integer.parseInt(scanner.nextLine().trim());
                switch (option) {
                    case 1:
                        showAllUsers();
                        break;
                    case 2:
                        addUser();
                        break;
                    case 3:
                        removeUser();
                        break;
                    case 4:
                        showAllFriendships();
                        break;
                    case 5:
                        addFriendship();
                        break;
                    case 6:
                        removeFriendship();
                        break;
                    case 7:
                        showNumberOfCommunities();
                        break;
                    case 8:
                        showMostSociableCommunity();
                        break;
                    case 0:
                        System.out.println("Exiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option! Please try again.");
                        break;
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
        System.out.print("ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        userService.addUser(id, firstName, lastName);
        System.out.println("User added successfully!");
    }

    private void removeUser() {
        System.out.print("User ID to remove: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        networkService.removeUserAndFriendships(id);
        System.out.println("User and associated friendships have been removed!");
    }

    private void showAllFriendships() {
        Collection<FriendShip> friendships = friendShipService.getAllFriendShips();
        if (friendships.isEmpty()) {
            System.out.println("There are no registered friendships.");
            return;
        }
        for (FriendShip fs : friendships) {
            System.out.println(fs);
        }
    }

    private void addFriendship() {
        System.out.print("First user ID: ");
        int firstId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Second user ID: ");
        int secondId = Integer.parseInt(scanner.nextLine().trim());

        friendShipService.addFriendShip(firstId, secondId);
        System.out.println("Friendship added successfully!");
    }

    private void removeFriendship() {
        System.out.print("First user ID: ");
        int firstId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Second user ID: ");
        int secondId = Integer.parseInt(scanner.nextLine().trim());

        friendShipService.removeFriendShip(firstId, secondId);
        System.out.println("Friendship removed successfully!");
    }

    private void showNumberOfCommunities() {
        int count = networkService.getNumberOfCommunities();
        System.out.println("Number of communities (connected components): " + count);
    }

    private void showMostSociableCommunity() {
        Set<Integer> community = networkService.getMostSociableCommunity();
        if (community.isEmpty()) {
            System.out.println("There are no communities.");
            return;
        }
        System.out.println("The most sociable community contains users:");
        for (Integer userId : community) {
            System.out.println(" - " + userService.findUserById(userId));
        }
    }
}
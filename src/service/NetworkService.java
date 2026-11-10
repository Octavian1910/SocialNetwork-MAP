package service;

import domain.User;

import java.util.*;

/**
 * Service responsible for graph-related social network analysis.
 */
public class NetworkService {
    private final UserService userService;
    private final FriendShipService friendShipService;

    public NetworkService(UserService userService, FriendShipService friendShipService) {
        this.userService = userService;
        this.friendShipService = friendShipService;
    }

    private int getLongestPathInCommunity(List<Integer> community) {
        if (community == null || community.size() <= 1) {
            return 0;
        }

        int maxDistance = 0;
        for (int i = 0; i < community.size(); i++) {
            for (int j = i + 1; j < community.size(); j++) {
                int dist = getDistance(community.get(i), community.get(j));
                if (dist > maxDistance) {
                    maxDistance = dist;
                }
            }
        }
        return maxDistance;
    }


    /**
     * Computes the shortest path between two nodes using Breadth-First Search (BFS).
     *
     * @param startId  source user ID
     * @param targetId destination user ID
     * @return shortest distance, or -1 if unreachable
     */
    private int getDistance(int startId, int targetId) {
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> distances = new HashMap<>();

        queue.add(startId);
        distances.put(startId, 0);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            if (currentId == targetId) {
                return distances.get(currentId);
            }

            for (Integer friendId : friendShipService.getFriendIds(currentId)) {
                if (!distances.containsKey(friendId)) {
                    distances.put(friendId, distances.get(currentId) + 1);
                    queue.add(friendId);
                }
            }
        }
        return -1;
    }

    /**
     * Traverses a connected component using recursive Depth-First Search (DFS).
     *
     * @param userId  starting user ID
     * @param visited set tracking visited nodes
     */
    private void dfs(int userId, Set<Integer> visited) {
        dfs(userId, visited, null);
    }

    private void dfs(int userId, Set<Integer> visited, List<Integer> community) {
        visited.add(userId);
        if (community != null) {
            community.add(userId);
        }
        friendShipService.getFriendIds(userId).stream()
                .filter(friendId -> !visited.contains(friendId))
                .forEach(friendId -> dfs(friendId, visited, community));
    }


    /**
     * Computes the total number of communities (connected components) in the network.
     *
     * @return count of connected components
     */
    public int getNumberOfCommunities() {
        Set<Integer> visited = new HashSet<>();
        return (int) userService.getAllUsers().stream()
                .filter(user -> {
                    if (!visited.contains(user.getId())) {
                        dfs(user.getId(), visited);
                        return true;
                    }
                    return false;
                })
                .count();
    }

    /**
     * Determines the most sociable community (connected component with the longest path).
     *
     * @return set of user IDs forming the most sociable community
     */
    public Set<Integer> getMostSociableCommunity() {
        return getAllCommunities().stream()
                .max(Comparator.comparingInt(this::getLongestPathInCommunity))
                .map(HashSet::new) //transforma list<Integer> in Set
                .orElseGet(HashSet::new); //returneaza set gol daca reteaua nu are nicio comunitate
    }


    private List<List<Integer>> getAllCommunities() {
        Set<Integer> visited = new HashSet<>();
        List<List<Integer>> communities = new ArrayList<>();

        userService.getAllUsers().forEach(user -> {
            if (!visited.contains(user.getId())) {
                List<Integer> currentCommunity = new ArrayList<>();
                dfs(user.getId(), visited, currentCommunity);
                communities.add(currentCommunity);
            }
        });
        return communities;
    }


    /**
     * Removes a user and all their associated friendships, ensuring storage consistency.
     *
     * @param userId user ID to delete
     */
    public void removeUserAndFriendships(int userId) {
        friendShipService.removeAllFriendshipsOfUser(userId);
        userService.removeUser(userId);
    }

    /**
     * Retrieves the community (connected component) containing the specified user.
     *
     * @param userId user ID
     * @return set of user IDs belonging to that community
     * @throws ServiceException if the user does not exist
     */
    public Set<Integer> getCommunity(int userId) {
        Set<Integer> community = new HashSet<>();
        userService.findUserById(userId);
        dfs(userId, community);
        return community;
    }
}
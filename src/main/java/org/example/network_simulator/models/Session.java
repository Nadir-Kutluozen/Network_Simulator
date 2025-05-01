package org.example.network_simulator.models;

/**
 * Design to hold the user information when log in or
 */
public class Session {
    private static User currentUser;

    /**
     * this class is design to hold the user information during the application!
     * @param user user object (User model)
     */
    public static void setUser(User user) {
        currentUser = user; // reference to the user object created when user login or register
    }

    /**
     * returns the reference of the user.
     * @return User object
     */
    public static User getUser() {
        return currentUser;
    }
}


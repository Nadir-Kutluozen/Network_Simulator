package org.example.network_simulator.models;

/**
 * User model to create the user object!
 */
public class User {
    private int id;
    private String username;
    private String email;

    // if a user may want to change the password because, they forget the password!
    private String password;

    /**
     *
     * @param id unique id for user.
     * @param username name of the user.
     * @param email email of the user.
     */
    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public User() {
        this.id = 00;
        this.username = "name";
        this.email = "@mail";
    }

    /**
     * @return id of the User Object.
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return username of the User object.
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return email of the User Object.
     */
    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

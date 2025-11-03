package com.example.users.patterns;

import com.example.users.entity.User;

public class UserComponent implements IUserComponent {

    protected final User user;

    public UserComponent(User user) {
        this.user = user;
    }

    @Override
    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public String getRole() {
        return user.getRole();
    }

    public User getUser() {
        return user;
    }
}

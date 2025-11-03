package com.example.users.patterns;

public abstract class UserDecorator implements IUserComponent {

    protected final IUserComponent decoratedUser;

    public UserDecorator(IUserComponent decoratedUser) {
        this.decoratedUser = decoratedUser;
    }

    @Override
    public String getFullName() {
        return decoratedUser.getFullName();
    }

    @Override
    public String getRole() {
        return decoratedUser.getRole();
    }
}

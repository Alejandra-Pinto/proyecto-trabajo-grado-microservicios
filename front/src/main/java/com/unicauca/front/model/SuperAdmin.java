package com.unicauca.front.model;

public class SuperAdmin {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;

    public SuperAdmin() {}

    //Constructor
    public SuperAdmin(int id, String firstName, String lastName, String phone,
                      String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    //Constructor sin id (por ejemplo, para crear uno nuevo)
    public SuperAdmin(String firstName, String lastName, String phone,
                      String email, String password) {
        this(-1, firstName, lastName, phone, email, password);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}


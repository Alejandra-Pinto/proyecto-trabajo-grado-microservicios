package com.example.submission.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "director")
    private List<DegreeWork> directedWorks;

    @OneToMany(mappedBy = "coDirector1")
    private List<DegreeWork> coDirectedWorks1;

    @OneToMany(mappedBy = "coDirector2")
    private List<DegreeWork> coDirectedWorks2;

    public Professor() {
    }

    public Professor(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

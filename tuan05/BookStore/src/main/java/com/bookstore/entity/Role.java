package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50, message = "Name of role can not be over 50 characters")
    @NotBlank(message = "Name of role can not be blank")
    @Column(name = "name" , length = 50, nullable = false)
    private String name;

    @Size(max = 250, message = "Description must be under 250 characters")
    @Column(name = "description", length = 250)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;
}

package com.example.logistics.domain;

import com.example.logistics.domain.type.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false, name = "full_name")
    private String fullName;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /** Check by enum */
    public boolean hasRole(RoleType rt) {
        return roles != null && roles.stream().anyMatch(r -> r.getName() == rt);
    }

    /** Check by string - this is what Thymeleaf uses */
    public boolean hasRole(String typeName) {
        try {
            return hasRole(RoleType.valueOf(typeName));
        } catch (Exception e) {
            return false;
        }
    }

    /** Convenience flags (optional but useful for templates) */
    public boolean isAdmin()    { return hasRole(RoleType.ADMIN); }
    public boolean isEmployee() { return hasRole(RoleType.EMPLOYEE); }
    public boolean isClient()   { return hasRole(RoleType.CLIENT); }
}
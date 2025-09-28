package com.safespend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(nullable = false, length = 120)
    private String password;

    // Comma-separated authorities: e.g. "ROLE_USER,ROLE_ADMIN"
    @Column(nullable = false, length = 200)
    private String roles;

    public AppUser() {}
    public AppUser(String username, String password, String roles) {
        this.username = username; this.password = password; this.roles = roles;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRoles() { return roles; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRoles(String roles) { this.roles = roles; }
}

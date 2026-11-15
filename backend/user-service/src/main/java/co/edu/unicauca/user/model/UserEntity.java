package co.edu.unicauca.user.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    private Set<String> roles = new HashSet<>();

    protected UserEntity() {
    }

    public UserEntity(String username, String fullName, String email, Set<String> roles) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.roles = new HashSet<>(roles);
    }

    /** Synchronizes profile attributes and role assignments from the validated Keycloak token. */
    public void synchronizeProfile(String fullName, String email, Set<String> roles) {
        this.fullName = fullName;
        this.email = email;
        this.roles = new HashSet<>(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
}

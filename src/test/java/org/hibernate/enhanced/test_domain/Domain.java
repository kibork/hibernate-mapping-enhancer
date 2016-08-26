package org.hibernate.enhanced.test_domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kibork on 8/25/16.
 */
@Entity
public class Domain {

    // ------------------ Constants  --------------------

    // ------------------ Fields     --------------------

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    private Set<Role> roles = new HashSet<>();

    @OneToMany
    private Set<User> users = new HashSet<>();

    // ------------------ Properties --------------------

    public Long getId() {
        return id;
    }

    public Domain setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Domain setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Domain setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Domain setUsers(Set<User> users) {
        this.users = users;
        return this;
    }


    // ------------------ Logic      --------------------
}

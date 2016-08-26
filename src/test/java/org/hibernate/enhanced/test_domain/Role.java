package org.hibernate.enhanced.test_domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kibork on 8/25/16.
 */
@Entity
public class Role {

    // ------------------ Constants  --------------------

    // ------------------ Fields     --------------------

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    private List<User> users = new ArrayList<>();

    // ------------------ Properties --------------------

    public Long getId() {
        return id;
    }

    public Role setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Role setName(String name) {
        this.name = name;
        return this;
    }

    public List<User> getUsers() {
        return users;
    }

    public Role setUsers(List<User> users) {
        this.users = users;
        return this;
    }


    // ------------------ Logic      --------------------
}

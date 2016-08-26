package org.hibernate.enhanced.test_domain;

import javax.persistence.*;

/**
 * Created by kibork on 8/25/16.
 */
@Entity
@Table(name = "Users")
public class User {

    // ------------------ Constants  --------------------

    // ------------------ Fields     --------------------

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String email;

    @ManyToOne
    private Role role;

    private Sex sex;

    // ------------------ Properties --------------------

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public User setRole(Role role) {
        this.role = role;
        return this;
    }

    public Sex getSex() {
        return sex;
    }

    public User setSex(Sex sex) {
        this.sex = sex;
        return this;
    }

    // ------------------ Logic      --------------------

    public enum Sex {
        MALE,
        FEMALE
    }
}

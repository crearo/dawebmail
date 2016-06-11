package com.sigmobile.dawebmail.database;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by rish on 11/6/16.
 */
public class User extends SugarRecord implements Serializable {

    public String username;
    public String password;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

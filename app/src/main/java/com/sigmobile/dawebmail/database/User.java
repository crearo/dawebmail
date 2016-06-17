package com.sigmobile.dawebmail.database;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rish on 11/6/16.
 */
public class User extends SugarRecord<User> implements Serializable {

    private String username;
    private String password;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static User getUserFromUserName(String username) {
        return Select.from(User.class)
                .where(Condition.prop(StringUtil.toSQLName("username")).eq(username))
                .first();
    }

    public static boolean doesUserExist(String username, String pwd) {
        User user = Select.from(User.class)
                .where(Condition.prop(StringUtil.toSQLName("username")).eq(username))
                .where(Condition.prop(StringUtil.toSQLName("password")).eq(pwd))
                .first();
        if (user != null)
            return true;
        return false;
    }

    public static User createNewUser(User newUser) {
        if (doesUserExist(newUser.username, newUser.password)) {
            return null;
        }
        newUser.save();
        return newUser;
    }

    public static void deleteUser(User user) {
        user.delete();
    }

    public static List<User> getAllUsers() {
        return User.listAll(User.class);
    }

    public static int getUsersCount() {
        return User.listAll(User.class).size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!username.equals(user.username)) return false;
        return password.equals(user.password);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
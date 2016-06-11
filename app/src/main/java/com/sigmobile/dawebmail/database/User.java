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
public class User extends SugarRecord implements Serializable {

    public String username;
    public String password;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User getUserFromUserName(String username, String pwd) {
        return Select.from(User.class)
                .where(Condition.prop(StringUtil.toSQLName("username")).eq(username))
                .where(Condition.prop(StringUtil.toSQLName("password")).eq(pwd))
                .first();
    }

    public static boolean doesUserExist(String username, String pwd) {
        if (getUserFromUserName(username, pwd) != null)
            return true;
        return false;
    }

    public static User createNewUser(User newUser) {
        if (getUserFromUserName(newUser.username, newUser.password) != null) {
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
}
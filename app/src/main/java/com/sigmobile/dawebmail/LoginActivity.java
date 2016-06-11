package com.sigmobile.dawebmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.sigmobile.dawebmail.asyncTasks.Login;
import com.sigmobile.dawebmail.asyncTasks.LoginListener;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class LoginActivity extends AppCompatActivity implements LoginListener {

    @Bind(R.id.login_username)
    EditText usernametf;

    @Bind(R.id.login_password)
    EditText pwdtf;

    @Bind(R.id.login_loginbtn)
    Button loginbtn;

    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    String username = "", pwd = "";

    ProgressDialog progressDialog;

    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        showUpdateDialog();

        toolbar.setTitle("DAWebmail");
        toolbar.setTitleTextColor(getResources().getColor(R.color.EmailBackground));
        setSupportActionBar(toolbar);

        usernametf.requestFocus();

        currentUser = UserSettings.getCurrentUser(this);

        if (currentUser == null) {
            // user not logged in
            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    username = usernametf.getText().toString().trim();
                    pwd = pwdtf.getText().toString();

                    if (!username.contains("@daiict.ac.in")) {
                        username = username + "@daiict.ac.in";
                    }

                    if (User.doesUserExist(username, pwd)) {
                        Snackbar.make(view, "This user already exists - logging in now", Snackbar.LENGTH_LONG).show();
                        UserSettings.setCurrentUser(User.getUserFromUserName(username), getApplicationContext());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        User user = new User(username, pwd);
                        new Login(user, getApplicationContext(), LoginActivity.this).execute();
                    }
                }
            });
        } else {
            // user already logged in and has an account username
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onPreLogin() {
        progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging In", true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(loginbtn.getWindowToken(), 0);
        Snackbar.make(findViewById(R.id.login_rellay), "Attempting Login. Hold On.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPostLogin(boolean loginSuccess, String timeTaken, User user) {
        progressDialog.dismiss();
        if (!loginSuccess) {
            Snackbar.make(findViewById(R.id.login_rellay), "Login Unsuccessful", Snackbar.LENGTH_LONG).show();
            usernametf.setText(username);
            pwdtf.setText("");
        } else {
            Snackbar.make(findViewById(R.id.login_rellay), "Login Successful!", Snackbar.LENGTH_LONG).show();
            User.createNewUser(user);
            UserSettings.setCurrentUser(user, getApplicationContext());
            startActivity(new Intent(this, MainActivity.class));
            finish();
            usernametf.setText("");
            pwdtf.setText("");
        }
    }

    private void showUpdateDialog() {
        if (!UserSettings.getAlertShown(getApplicationContext())) {
            final MaterialDialog materialDialog = new MaterialDialog(LoginActivity.this);
            materialDialog.setTitle("And it's up!");
            materialDialog.setMessage("We've changed the entire structure of the application. Material UI + you'll receive notifications, smoother than ever! I'm really thankful to all those that contributed.\n\nSend, and delete will show up soon too. :D");
            materialDialog.show();
            materialDialog.setPositiveButton("Alright!", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    materialDialog.dismiss();
                }
            });
            UserSettings.setAlertShown(getApplicationContext(), true);
        }
    }
}
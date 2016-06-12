package com.sigmobile.dawebmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

    @Bind(R.id.login_button_container)
    RelativeLayout loginContainer;

    @Bind(R.id.login_tool_bar)
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

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        usernametf.requestFocus();
        currentUser = UserSettings.getCurrentUser(this);

        if (currentUser == null) {
            // user not logged in
            loginContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginContainer.setEnabled(false);
                    username = usernametf.getText().toString().trim();
                    pwd = pwdtf.getText().toString();

                    if (!username.contains("@" + getString(R.string.webmail_domain))) {
                        username = username + "@" + getString(R.string.webmail_domain);
                    }

                    if (User.doesUserExist(username, pwd)) {
                        Snackbar.make(view, getString(R.string.snackbar_login_user_exist), Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                UserSettings.setCurrentUser(User.getUserFromUserName(username), getApplicationContext());
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }, 1000);
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
    public void onBackPressed() {
        if (UserSettings.getCurrentUser(this) == null) {
            if (User.getAllUsers().size() > 0)
                UserSettings.setCurrentUser(User.getAllUsers().get(0), getApplicationContext());
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onPreLogin() {
        progressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.dialog_logging_in), true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(loginContainer.getWindowToken(), 0);
        Snackbar.make(loginContainer, getString(R.string.snackbar_login_attempting), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPostLogin(boolean loginSuccess, String timeTaken, User user) {
        progressDialog.dismiss();
        loginContainer.setEnabled(true);
        if (!loginSuccess) {
            Snackbar.make(loginContainer, getString(R.string.snackbar_login_failed), Snackbar.LENGTH_LONG).show();
            usernametf.setText(username);
            pwdtf.setText("");
        } else {
            Snackbar.make(loginContainer, getString(R.string.snackbar_login_successful), Snackbar.LENGTH_LONG).show();
            user = User.createNewUser(user);
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
            materialDialog.setTitle(getString(R.string.app_name));
            materialDialog.setMessage(getString(R.string.release_notes_2));
            materialDialog.show();
            materialDialog.setCanceledOnTouchOutside(true);
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
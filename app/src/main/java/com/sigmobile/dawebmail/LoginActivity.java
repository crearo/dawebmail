package com.sigmobile.dawebmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sigmobile.dawebmail.asyncTasks.Login;
import com.sigmobile.dawebmail.asyncTasks.LoginListener;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class LoginActivity extends AppCompatActivity implements LoginListener {

    @Bind(R.id.login_username)
    EditText usernameField;
    @Bind(R.id.login_password)
    EditText passwordField;
    @Bind(R.id.login_button_container)
    RelativeLayout loginButtonContainer;
    @Bind(R.id.login_tool_bar)
    Toolbar toolbar;

    private String enteredUsername = "";
    private String enteredPassword = "";
    private ProgressDialog progressDialog;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        currentUser = UserSettings.getCurrentUser(this);

        showUpdateDialog();
        setupToolbar();
        setupEditTexts();

        if (currentUser == null) {
            // user not logged in
            loginButtonContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginButtonContainer.setEnabled(false);
                    enteredUsername = usernameField.getText().toString().trim();
                    enteredPassword = passwordField.getText().toString();

                    if (!enteredUsername.contains("@" + getString(R.string.webmail_domain))) {
                        enteredUsername = enteredUsername + "@" + getString(R.string.webmail_domain);
                    }

                    if (User.doesUserExist(enteredUsername, enteredPassword)) {
                        Snackbar.make(view, getString(R.string.snackbar_login_user_exist), Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                UserSettings.setCurrentUser(User.getUserFromUserName(enteredUsername), getApplicationContext());
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }, 1000);
                    } else {
                        User user = new User(enteredUsername, enteredPassword);
                        new Login(user, getApplicationContext(), LoginActivity.this).execute();
                    }
                }
            });
        } else {
            // user already logged in and has an account enteredUsername
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbarText));
        setSupportActionBar(toolbar);
    }

    private void setupEditTexts() {
        usernameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    usernameField.clearFocus();
                    passwordField.requestFocus();
                    return true;
                }
                return false;
            }
        });

        passwordField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginButtonContainer.performClick();
                    return true;
                }
                return false;
            }
        });

        usernameField.requestFocus();
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
        progressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.dialog_msg_logging_in), true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(loginButtonContainer.getWindowToken(), 0);
        Snackbar.make(loginButtonContainer, getString(R.string.snackbar_login_attempting), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPostLogin(boolean loginSuccess, String timeTaken, User user) {
        progressDialog.dismiss();
        loginButtonContainer.setEnabled(true);
        if (!loginSuccess) {
            Snackbar.make(loginButtonContainer, getString(R.string.snackbar_login_failed), Snackbar.LENGTH_LONG).show();
            usernameField.setText(enteredUsername);
            passwordField.setText("");
        } else {
            Snackbar.make(loginButtonContainer, getString(R.string.snackbar_login_successful), Snackbar.LENGTH_LONG).show();
            user = User.createNewUser(user);
            UserSettings.setCurrentUser(user, getApplicationContext());
            startActivity(new Intent(this, MainActivity.class));
            finish();
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    private void showUpdateDialog() {
        if (!UserSettings.getAlertShown(getApplicationContext())) {
            final MaterialDialog materialDialog = new MaterialDialog(LoginActivity.this);
            materialDialog.setTitle(getString(R.string.app_name));
            materialDialog.setMessage(getString(R.string.release_notes_2));
            materialDialog.setCanceledOnTouchOutside(false);
            materialDialog.setPositiveButton("Lets Go!", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    materialDialog.dismiss();
                }
            });
            materialDialog.show();
            UserSettings.setAlertShown(getApplicationContext(), true);
        }
    }
}
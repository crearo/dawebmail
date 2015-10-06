package rish.crearo.dawebmaillite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import rish.crearo.dawebmaillite.asyncTasks.Login;
import rish.crearo.dawebmaillite.asyncTasks.LoginListener;
import rish.crearo.dawebmaillite.database.User;

public class LoginActivity extends AppCompatActivity implements LoginListener {

    @Bind(R.id.login_username)
    EditText usernametf;

    @Bind(R.id.login_password)
    EditText pwdtf;

    @Bind(R.id.login_loginbtn)
    Button loginbtn;

    String username = "", pwd = "";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        usernametf.requestFocus();

        if (User.getUsername(getApplicationContext()) == null || User.getUsername(getApplicationContext()).equalsIgnoreCase("null")) {
            // user not logged in
            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    username = usernametf.getText().toString().trim();
                    pwd = pwdtf.getText().toString();
                    User.setUsername(username, getApplicationContext());
                    User.setPassword(pwd, getApplicationContext());
                    new Login(getApplicationContext(), LoginActivity.this).execute();
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
    public void onPostLogin(boolean loginSuccess, String timeTaken) {
        progressDialog.dismiss();
        if (!loginSuccess) {
            Snackbar.make(findViewById(R.id.login_rellay), "Login Unsuccessful", Snackbar.LENGTH_LONG).show();
            User.setUsername("null", getApplicationContext());
            User.setPassword("null", getApplicationContext());
            usernametf.setText(username);
            pwdtf.setText("");
        } else {
            Snackbar.make(findViewById(R.id.login_rellay), "Login Successful!", Snackbar.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            usernametf.setText("");
            pwdtf.setText("");
        }
    }
}
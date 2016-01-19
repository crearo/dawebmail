package com.sigmobile.dawebmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.sigmobile.dawebmail.asyncTasks.Login;
import com.sigmobile.dawebmail.asyncTasks.LoginListener;
import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {

    @Bind(R.id.loginbtn)
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ButterKnife.bind(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Login(getApplicationContext(), new LoginListener() {
                    @Override
                    public void onPreLogin() {
                        System.out.println("Logging in");
                    }

                    @Override
                    public void onPostLogin(boolean loginSuccess, String timeTaken) {
                        System.out.println("Fini sendMsg");
                    }
                }).execute();

                new RefreshInbox(getApplicationContext(), new RefreshInboxListener() {
                    @Override
                    public void onPreRefresh() {
                        System.out.println("Refreshing");
                    }

                    @Override
                    public void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails) {
                        System.out.println("Fini Refresh " + success);
                    }
                }, Constants.INBOX).execute();
            }
        });
    }
}

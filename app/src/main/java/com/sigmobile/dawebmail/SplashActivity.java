package com.sigmobile.dawebmail;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.Settings;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Settings settings = new Settings(getApplicationContext());


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                User user = new User("NULL", "NULL");
                user.save();
                user.delete();
                settings.save(Settings.KEY_DATABASE_CREATED, true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Snackbar.make(findViewById(R.id.splash_img), "Dude, what? Patience.", Snackbar.LENGTH_LONG).show();
    }
}

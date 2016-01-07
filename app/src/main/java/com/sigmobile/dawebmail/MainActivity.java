package com.sigmobile.dawebmail;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.fragments.FeedbackFragment;
import com.sigmobile.dawebmail.fragments.InboxFragment;
import com.sigmobile.dawebmail.fragments.SettingsFragment;
import com.sigmobile.dawebmail.fragments.SmartBoxFragment;
import com.sigmobile.dawebmail.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_tool_bar)
    Toolbar toolbar;

    @Bind(R.id.main_drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.main_drawer)
    NavigationView navigationView;

    @Bind(R.id.header_title)
    TextView headerTitle;

    int mCurrentSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        setUpNavDrawer();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_1:
                        Snackbar.make(findViewById(R.id.main_rellay), "Inbox", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 0;
                        selectItem(mCurrentSelectedPosition);
                        return true;
                    case R.id.navigation_item_2:
                        Snackbar.make(findViewById(R.id.main_rellay), "SmartBox", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 1;
                        selectItem(mCurrentSelectedPosition);
                        return true;
                    case R.id.navigation_item_3:
                        Snackbar.make(findViewById(R.id.main_rellay), "Settings", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 2;
                        selectItem(mCurrentSelectedPosition);
                        return true;
                    case R.id.navigation_item_4:
                        Snackbar.make(findViewById(R.id.main_rellay), "Feedback", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 3;
                        selectItem(mCurrentSelectedPosition);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void setUpNavDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_three);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        Snackbar.make(findViewById(R.id.main_rellay), "Inbox", Snackbar.LENGTH_SHORT).show();
        mCurrentSelectedPosition = 0;
        selectItem(mCurrentSelectedPosition);

        headerTitle.setText(User.getUsername(getApplicationContext()));
    }

    private void selectItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new InboxFragment();
                break;
            case 1:
                fragment = new SmartBoxFragment();
                break;
            case 2:
                fragment = new SettingsFragment();
                break;
            case 3:
                fragment = new FeedbackFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_rellay, fragment).commit();
            fragmentManager.popBackStack();
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        }, 25);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.SAVED_FRAGMENT, mCurrentSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectItem(savedInstanceState.getInt(Constants.SAVED_FRAGMENT, mCurrentSelectedPosition));

    }

    /*
    Initially used for setting up service. Now done in broadcast receiver
    private void setUpService() {
        SharedPreferences prefs = getSharedPreferences(Constants.ON_FIRST_RUN, Context.MODE_PRIVATE);
        Printer.println("FIRSTRUN = " + !prefs.getBoolean(Constants.RUN_EXCEPT_ON_FIRST, false));
        if (prefs.getBoolean(Constants.RUN_EXCEPT_ON_FIRST, false)) {
            Printer.println("RUNNING SERVICE RESTART");
            BackgroundRunner.stopService(getApplicationContext());
            BackgroundRunner.startHourlyService(getApplicationContext());
        }
        prefs.edit().putBoolean(Constants.RUN_EXCEPT_ON_FIRST, true).commit();
    }*/
}
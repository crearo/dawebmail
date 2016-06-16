package com.sigmobile.dawebmail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.fragments.FolderFragment;
import com.sigmobile.dawebmail.fragments.InboxFragment;
import com.sigmobile.dawebmail.fragments.SmartBoxFragment;
import com.sigmobile.dawebmail.services.NotificationMaker;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    @Bind(R.id.main_tool_bar)
    Toolbar toolbar;
    @Bind(R.id.main_frame_layout)
    FrameLayout frameLayout;

    private Drawer drawer;
    private AccountHeader accountHeader;
    private PrimaryDrawerItem pInbox, pSmartBox, pSentBox, pTrashBox;
    private SecondaryDrawerItem sSettings, sFeedback;
    private ArrayList<IProfile> allAccountHeaders;

    private IDrawerItem selectedDrawerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupToolbar();
        setupDrawer();

        selectedDrawerItem = pInbox;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedDrawerItem != null) {
            setToolbarTitle(selectedDrawerItem);
        }
        setSelectedAccountHeader(true);
        // ToDO : Send false herre, but make sure account is selected correctly.
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {

        pInbox = new PrimaryDrawerItem().withName(getString(R.string.drawer_inbox)).withIcon(R.drawable.inbox);
        pSmartBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_smartbox)).withIcon(R.drawable.fire_element);
        pSentBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_sent)).withIcon(R.drawable.sent);
        pTrashBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_trash)).withIcon(R.drawable.trash);

        sSettings = (SecondaryDrawerItem) new SecondaryDrawerItem().withName(getString(R.string.drawer_settings)).withIcon(R.drawable.settings);
        sFeedback = (SecondaryDrawerItem) new SecondaryDrawerItem().withName(getString(R.string.drawer_feedback)).withIcon(R.drawable.feedback);

        sSettings.withSelectable(false);
        sFeedback.withSelectable(false);

        setupAllAccountHeaders();
        final String createAccountString = getString(R.string.drawer_new_account);

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withProfiles(allAccountHeaders)
                .withHeaderBackground(new ColorDrawable(getResources().getColor(R.color.primary_dark)))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if (profile.getName().getText().equals(createAccountString)) {
                            UserSettings.setCurrentUser(null, getApplicationContext());
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            drawer.closeDrawer();
                            return true;
                        } else {
                            UserSettings.setCurrentUser(User.getUserFromUserName(profile.getName().getText()), getApplicationContext());
                            drawer.closeDrawer();
                            if (selectedDrawerItem == null)
                                selectedDrawerItem = pInbox;
                            else if (!selectedDrawerItem.isSelectable())
                                selectedDrawerItem = pInbox;
                            setDrawerSelection(selectedDrawerItem);
                            setToolbarTitle(selectedDrawerItem);
                            return true;
                        }
                    }
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        pInbox,
                        pSentBox,
                        pTrashBox,
                        new DividerDrawerItem(),
                        pSmartBox,
                        new DividerDrawerItem(),
                        sSettings,
                        sFeedback
                ).withDelayOnDrawerClose(200)
                .withCloseOnClick(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Fragment fragment = null;
                        Bundle bundle = null;
                        String fragmentTag = "";
                        drawer.closeDrawer();
                        if (drawerItem == null)
                            drawerItem = pInbox;

                        selectedDrawerItem = (PrimaryDrawerItem) drawerItem;
                        setToolbarTitle(selectedDrawerItem);
                        if (selectedDrawerItem.equals(pInbox)) {
                            fragment = new InboxFragment();
                            Snackbar.make(frameLayout, getString(R.string.drawer_inbox), Snackbar.LENGTH_SHORT).show();
                            fragmentTag = Constants.FRAGMENT_TAG_INBOX;
                        } else if (selectedDrawerItem.equals(pSmartBox)) {
                            fragment = new SmartBoxFragment();
                            Snackbar.make(frameLayout, getString(R.string.drawer_smartbox), Snackbar.LENGTH_SHORT).show();
                            fragmentTag = Constants.FRAGMENT_TAG_SMARTBOX;
                        } else if (selectedDrawerItem.equals(pSentBox)) {
                            fragment = new FolderFragment();
                            bundle = new Bundle();
                            bundle.putString(Constants.FOLDER, Constants.SENT);
                            fragment.setArguments(bundle);
                            fragmentTag = Constants.FRAGMENT_TAG_FOLDER;
                            Snackbar.make(frameLayout, getString(R.string.drawer_sent), Snackbar.LENGTH_SHORT).show();
                        } else if (selectedDrawerItem.equals(pTrashBox)) {
                            fragment = new FolderFragment();
                            bundle = new Bundle();
                            bundle.putString(Constants.FOLDER, Constants.TRASH);
                            fragment.setArguments(bundle);
                            fragmentTag = Constants.FRAGMENT_TAG_FOLDER;
                            Snackbar.make(frameLayout, getString(R.string.drawer_trash), Snackbar.LENGTH_SHORT).show();
                        } else if (selectedDrawerItem.equals(sSettings)) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        } else if (selectedDrawerItem.equals(sFeedback)) {
                            Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        }

                        if (fragment != null) {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.main_frame_layout, fragment, fragmentTag).commit();
                            fragmentManager.popBackStack();
                        }
                        return false;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    private void setSelectedAccountHeader(boolean shouldClick) {
        String currentUsername = UserSettings.getCurrentUser(this).username;
        for (int i = 0; i < allAccountHeaders.size(); i++) {
            if (allAccountHeaders.get(i).getName().getText().equals(currentUsername))
                accountHeader.setActiveProfile(allAccountHeaders.get(i), shouldClick);
        }
    }

    private void setupAllAccountHeaders() {
        allAccountHeaders = new ArrayList<>();
        for (User user : User.getAllUsers()) {
            allAccountHeaders.add(new ProfileDrawerItem().withName(user.username).withIcon(getResources().getDrawable(R.drawable.git_user)));
        }

        final String createAccount = getString(R.string.drawer_new_account);
        allAccountHeaders.add(new ProfileDrawerItem().withName(createAccount).withIcon(getResources().getDrawable(R.drawable.plus)));
    }

    public void showLogoutDialog(final User currentUser) {
        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setCanceledOnTouchOutside(true);
        materialDialog.setTitle(getString(R.string.dialog_title_logout));
        if (User.getUsersCount() >= 2)
            materialDialog.setMessage(getString(R.string.dialog_msg_logout_multi));
        else
            materialDialog.setMessage(getString(R.string.dialog_msg_logout_single));
        materialDialog.setNegativeButton("", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.setPositiveButton(getString(R.string.dialog_btn_logout), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putBoolean(Constants.TOGGLE_MOBILEDATA, false);
                editor.putBoolean(Constants.TOGGLE_WIFI, false);

                EmailMessage.deleteAllMailsOfUser(currentUser);

                NotificationMaker.cancelNotification(getApplicationContext());
                materialDialog.dismiss();
                Snackbar.make(frameLayout, getString(R.string.snackbar_logging_out), Snackbar.LENGTH_LONG).show();

                /**
                 * Delete the current User and set the next user in line as current user
                 */
                User.deleteUser(currentUser);
                if (User.getAllUsers().size() != 0)
                    UserSettings.setCurrentUser(User.getAllUsers().get(0), getApplicationContext());
                else
                    UserSettings.setCurrentUser(null, getApplicationContext());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }, 1000);
            }
        });
        materialDialog.show();
    }

    private void setToolbarTitle(IDrawerItem drawerItem) {
        String currentUserName = "NaN";
        if (UserSettings.getCurrentUser(getApplicationContext()) != null)
            currentUserName = UserSettings.getCurrentUser(getApplicationContext()).username;
        if (currentUserName.startsWith("20")) {
            currentUserName = currentUserName.substring(0, currentUserName.indexOf("@"));
            if (currentUserName.length() > 3)
                currentUserName = currentUserName.substring(currentUserName.length() - 3);
        } else {
            currentUserName = currentUserName.substring(0, 3);
        }

        String toolbarTitle = "";
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pInbox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_inbox) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSentBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_sent) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSmartBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_smartbox) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pTrashBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_trash) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(sSettings.getName().getText()))
            toolbarTitle = getString(R.string.drawer_settings);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(sFeedback.getName().getText()))
            toolbarTitle = getString(R.string.drawer_feedback);
        getSupportActionBar().setTitle(toolbarTitle);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void setDrawerSelection(IDrawerItem drawerItem) {
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pInbox.getName().getText()))
            drawer.setSelection(pInbox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSentBox.getName().getText()))
            drawer.setSelection(pSentBox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSmartBox.getName().getText()))
            drawer.setSelection(pSmartBox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pTrashBox.getName().getText()))
            drawer.setSelection(pTrashBox);
    }
}
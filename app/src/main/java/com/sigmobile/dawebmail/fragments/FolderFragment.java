package com.sigmobile.dawebmail.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sigmobile.dawebmail.MainActivity;
import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.adapters.MailAdapter;
import com.sigmobile.dawebmail.asyncTasks.MultiMailAction;
import com.sigmobile.dawebmail.asyncTasks.MultiMailActionListener;
import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rish on 6/10/15.
 */

public class FolderFragment extends Fragment implements RefreshInboxListener, MultiMailActionListener, MailAdapter.MultiMailActionSelectedListener {

    @Bind(R.id.folder_empty_view)
    LinearLayout emptyLayout;
    @Bind(R.id.folder_recycleView)
    RecyclerView recyclerView;
    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.searchET)
    EditText searchET;
    @Bind(R.id.folder_delete_fab)
    FloatingActionButton fabDelete;

    private MailAdapter mailAdapter;
    private ProgressDialog progressDialog, progressDialog2;
    private ArrayList<EmailMessage> allEmails;
    private User currentUser;
    private String folder;

    public FolderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folder, container, false);
        ButterKnife.bind(FolderFragment.this, rootView);
        Bundle args = getArguments();
        folder = args.getString(Constants.FOLDER, Constants.SENT);

        if (folder.equals(Constants.SENT))
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.sent));
        else if (folder.equals(Constants.TRASH))
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.trash));

        currentUser = UserSettings.getCurrentUser(getActivity());
        progressDialog = new ProgressDialog(getActivity());

        registerInternalBroadcastReceivers();
        setupMailAdapter();
        setupSwipeRefreshLayout();
        setupSearchBar();

        new RefreshInbox(currentUser, getActivity(), FolderFragment.this, folder, Constants.REFRESH_TYPE_REFRESH).execute();

        swipeRefreshLayout.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * This is done for maintaining the fragment lifecycle. Read onPostRefresh comment.
         **/
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE, -1) != -1) {
                onPostRefresh(bundle.getInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE));
            }
        }
    }

    private void registerInternalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshAdapter();
            }
        }, new IntentFilter(Constants.BROADCAST_REFRESH_ADAPTERS));
    }

    private void setupMailAdapter() {
        allEmails = new ArrayList<>();
        mailAdapter = new MailAdapter(allEmails, getActivity(), this, folder);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mailAdapter);
    }

    private void setupSearchBar() {
        /*
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                if (charSequence.length() >= 2) {
                    for (int i = 0; i < allEmails.size(); i++) {
                        EmailMessage email = allEmails.get(i);
                        if (email.fromName.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.fromAddress.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.subject.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.dateInMillis.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.content.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())) {
                        } else {
                            allEmails.remove(email);
                            i--;
                        }
                    }
                    mailAdapter = new MailAdapter(allEmails, getActivity(), SentFragment.this, Constants.SENT);
                    recyclerView.setAdapter(mailAdapter);
                    System.out.println("SEARCHED RESULTS COUNT = " + mailAdapter.getCount());
                } else {
                    refreshAdapter();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        */
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshInbox(currentUser, getActivity(), FolderFragment.this, folder, Constants.REFRESH_TYPE_REFRESH).execute();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.darker_gray,
                android.R.color.holo_blue_dark);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_inbox_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Animation slide_down = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_out_bottom);
            Animation slide_up = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_in_bottom);

            if (searchET.getVisibility() == View.GONE) {
                searchET.setVisibility(View.VISIBLE);
                searchET.startAnimation(slide_up);
                searchET.requestFocus();
                item.setIcon(R.drawable.ic_action_close);
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(searchET, InputMethodManager.SHOW_FORCED);
            } else {
                item.setIcon(R.drawable.ic_action_search);
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                searchET.startAnimation(slide_down);
                searchET.setVisibility(View.GONE);
            }
            return true;
        } else if (id == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreRefresh() {
        progressDialog2 = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_msg_loading), true);
        progressDialog2.setCancelable(false);
        progressDialog2.show();
    }

    @Override
    public void onPostRefresh(boolean success, final ArrayList<EmailMessage> refreshedEmails, User user) {

        allEmails = new ArrayList<>(refreshedEmails);
        /**
         * This is done for maintaining the fragment lifecycle.
         * Check if the fragment is attached to the activity
         *       if it isn't, then set bundle stating that a refresh is required.
         */
        FolderFragment thisFragment = (FolderFragment) getFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_FOLDER);
        if (!thisFragment.isAdded()) {
            if (thisFragment != null) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE, refreshedEmails.size());
                thisFragment.setArguments(bundle);
            }
        } else {
            onPostRefresh(refreshedEmails.size());
        }
    }

    private void onPostRefresh(final int refreshedEmailsSize) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
                if (refreshedEmailsSize == 0)
                    Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_new_webmail_zero), Snackbar.LENGTH_LONG).show();
                else if (refreshedEmailsSize == 1)
                    Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_new_webmail_one), Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(swipeRefreshLayout, refreshedEmailsSize + getString(R.string.snackbar_new_webmail_many), Snackbar.LENGTH_LONG).show();
                progressDialog2.dismiss();

                if (allEmails.size() != 0) {
                    emptyLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.GONE);
                }

            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPreMultiMailAction() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_msg_attempting_action));
        progressDialog.show();
    }

    @Override
    public void onPostMultiMailAction(boolean success, String mailAction, ArrayList<EmailMessage> emailsForMultiAction) {
        if (!success)
            Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_unsuccessful), Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_successful), Snackbar.LENGTH_LONG).show();

        progressDialog.dismiss();
        refreshAdapter();
        fabDelete.setVisibility(View.GONE);
    }

    public void refreshAdapter() {
        mailAdapter = new MailAdapter(allEmails, getActivity(), this, folder);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mailAdapter);
    }

    public void logout() {
        ((MainActivity) getActivity()).showLogoutDialog(currentUser);
    }

    @Override
    public void onItemClickedForDelete(final ArrayList<EmailMessage> emailsMarkedForAction) {

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_deleting), Snackbar.LENGTH_LONG).show();
                new MultiMailAction(currentUser, getActivity(), FolderFragment.this, emailsMarkedForAction, getString(R.string.msg_action_trash)).execute();
            }
        });

        if (emailsMarkedForAction.size() > 0) {
            if (fabDelete.getVisibility() != View.VISIBLE) {
                fabDelete.setVisibility(View.VISIBLE);
                fabDelete.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom));
            }
        } else {
            if (fabDelete.getVisibility() != View.GONE) {
                fabDelete.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom));
                fabDelete.setVisibility(View.GONE);
            }
        }
    }
}
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
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sigmobile.dawebmail.ComposeActivity;
import com.sigmobile.dawebmail.MainActivity;
import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.adapters.MailAdapter;
import com.sigmobile.dawebmail.asyncTasks.MultiMailAction;
import com.sigmobile.dawebmail.asyncTasks.MultiMailActionListener;
import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.CurrentUser;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.AnalyticsAPI;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by rish on 6/10/15.
 */

public class InboxFragment extends Fragment implements RefreshInboxListener, MultiMailActionListener, MailAdapter.MultiMailActionSelectedListener {

    private static final String TAG = "InboxFragment";

    @Bind(R.id.inbox_empty_view)
    LinearLayout emptyLayout;
    @Bind(R.id.inbox_recycleView)
    RecyclerView recyclerView;
    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.inbox_fab_menu)
    FloatingActionsMenu fabMenu;
    com.getbase.floatingactionbutton.FloatingActionButton fabTrash;
    com.getbase.floatingactionbutton.FloatingActionButton fabDelete;
    com.getbase.floatingactionbutton.FloatingActionButton fabMarkRead;
    com.getbase.floatingactionbutton.FloatingActionButton fabMarkUnread;
    @Bind(R.id.inbox_send_fab)
    FloatingActionButton fabSend;

    @OnClick(R.id.inbox_send_fab)
    public void sendFab() {
        startActivity(new Intent(getActivity(), ComposeActivity.class));
    }

    private MailAdapter mailAdapter;
    private ProgressDialog progressDialog, progressDialog2;
    private ArrayList<EmailMessage> allEmails;
    private User currentUser;

    public InboxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(InboxFragment.this, rootView);

        fabTrash = ButterKnife.findById(fabMenu, R.id.fab_action_trash);
        fabDelete = ButterKnife.findById(fabMenu, R.id.fab_action_delete);
        fabMarkRead = ButterKnife.findById(fabMenu, R.id.fab_action_mark_read);
        fabMarkUnread = ButterKnife.findById(fabMenu, R.id.fab_action_mark_unread);

        currentUser = CurrentUser.getCurrentUser(getActivity());
        progressDialog = new ProgressDialog(getActivity());

        if (currentUser == null) {
            emptyLayout.setVisibility(View.VISIBLE);
            return rootView;
        }

        registerInternalBroadcastReceivers();
        setupMailAdapter();
        setupSwipeRefreshLayout();
        setupSearchBar();
        setupDeleteAndComposeFABs(false);

        emptyLayout.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupDeleteAndComposeFABs(false);
        /**
         * This is done for maintaining the fragment lifecycle. Read onPostRefresh comment.
         **/
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE, -1) != -1) {
                onPostRefresh(bundle.getInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE));
            }
        }

        refreshAdapter();
    }

    private void setupMailAdapter() {
        allEmails = (ArrayList<EmailMessage>) EmailMessage.getAllMailsOfUser(currentUser);
        Collections.reverse(allEmails);
        mailAdapter = new MailAdapter(allEmails, getActivity(), this, Constants.INBOX);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mailAdapter);

        if (allEmails.size() == 0) {
            new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_LOAD_MORE).execute();
            allEmails = new ArrayList<>();
        }

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                fabMenu.collapse();
            }
        });
    }

    private void registerInternalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshAdapter();
            }
        }, new IntentFilter(Constants.BROADCAST_REFRESH_ADAPTERS));
    }

    private void setupSearchBar() {

    }

    private void setupDeleteAndComposeFABs(boolean isDeleteVisible) {
        if (!isDeleteVisible) {
            final Animation animation = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_out_bottom);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fabMenu.setVisibility(View.GONE);
                    fabSend.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_in_bottom));
                    fabSend.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (fabMenu.isExpanded()) {
                fabMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fabMenu.startAnimation(animation);
                    }
                }, 300);
            } else {
                fabMenu.startAnimation(animation);
            }
        } else {
            if (fabSend.getVisibility() == View.VISIBLE) {
                final Animation animation = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_out_bottom);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fabSend.setVisibility(View.GONE);
                        fabMenu.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_in_bottom));
                        fabMenu.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fabSend.startAnimation(animation);
            }
        }
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_REFRESH).execute();
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
            Snackbar.make(recyclerView, getString(R.string.snackbar_search_pressed), Snackbar.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
        } else if (id == R.id.action_loadmore) {
            new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_LOAD_MORE).execute();
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
        allEmails.addAll(refreshedEmails);
        /**
         * ToDo : This is ugly code. Please modify.
         * This is done for maintaining the fragment lifecycle.
         * Check if the fragment is attached to the activity
         *       if it isn't, then set bundle stating that a refresh is required.
         */
        if (getFragmentManager() != null) {
            InboxFragment thisFragment = (InboxFragment) getFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_INBOX);
            if (thisFragment != null) {
                if (!thisFragment.isAdded()) {
                    if (thisFragment != null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.BUNDLE_ON_POST_REFRESH_EMAILS_SIZE, refreshedEmails.size());
                        thisFragment.setArguments(bundle);
                    }
                } else {
                    onPostRefresh(refreshedEmails.size());
                }
            } else {
                refreshAdapter();
                progressDialog2.dismiss();
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            refreshAdapter();
            progressDialog2.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onPostRefresh(int size) {
        refreshAdapter();
        if (size == 0)
            Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_webmail_zero), Snackbar.LENGTH_LONG).show();
        else if (size == 1)
            Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_webmail_one), Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(swipeRefreshLayout, size + getString(R.string.snackbar_webmail_many), Snackbar.LENGTH_LONG).show();
        progressDialog2.dismiss();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPreMultiMailAction() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_msg_attempting_action));
        progressDialog.show();
    }

    @Override
    public void onPostMultiMailAction(boolean success, String msgAction, ArrayList<EmailMessage> emailsForMultiAction) {

        /* If msgAction was delete or trash, delete the email from database */
        if (success) {
            if (msgAction.equals(getString(R.string.msg_action_delete)) || msgAction.equals(getString(R.string.msg_action_trash))) {
                for (EmailMessage emailMessage : emailsForMultiAction) {
                    emailMessage.delete();
                }
            } else if (msgAction.equals(getString(R.string.msg_action_read))) {
                for (EmailMessage emailMessage : emailsForMultiAction) {
                    emailMessage.setReadUnread(Constants.WEBMAIL_READ);
                    emailMessage.save();
                }
            } else if (msgAction.equals(getString(R.string.msg_action_unread))) {
                for (EmailMessage emailMessage : emailsForMultiAction) {
                    emailMessage.setReadUnread(Constants.WEBMAIL_UNREAD);
                    emailMessage.save();
                }
            }
            emailsForMultiAction.clear();
        }

        if (!success) {
            if (msgAction.equals(getString(R.string.msg_action_trash)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trash_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_delete)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_read)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_read_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_unread)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_unread_unsuccessful), Snackbar.LENGTH_LONG).show();
        } else {
            if (msgAction.equals(getString(R.string.msg_action_trash)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trash_successful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_delete)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_successful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_read)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_read_successful), Snackbar.LENGTH_LONG).show();
            else if (msgAction.equals(getString(R.string.msg_action_unread)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_unread_successful), Snackbar.LENGTH_LONG).show();
        }

        progressDialog.dismiss();
        refreshAdapter();
    }

    public void refreshAdapter() {
        allEmails = (ArrayList<EmailMessage>) EmailMessage.getAllMailsOfUser(currentUser);
        Collections.reverse(allEmails);
        mailAdapter.setEmails(allEmails);
        mailAdapter.notifyDataSetChanged();
    }

    public void logout() {
        ((MainActivity) getActivity()).showLogoutDialog(currentUser);
    }

    @Override
    public void onItemClickedForDelete(final ArrayList<EmailMessage> emailsMarkedForAction) {

        if (emailsMarkedForAction.size() == 0)
            setupDeleteAndComposeFABs(false);
        else
            setupDeleteAndComposeFABs(true);
        fabMenu.setEnabled(true);

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDeleteDialog(emailsMarkedForAction);
            }
        });

        fabTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionTrash(emailsMarkedForAction);
            }
        });

        fabMarkRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionRead(emailsMarkedForAction);
            }
        });

        fabMarkUnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionUnread(emailsMarkedForAction);
            }
        });
    }

    private void showConfirmDeleteDialog(final ArrayList<EmailMessage> emailsMarkedForAction) {

        fabMenu.collapse();

        final MaterialDialog materialDialog = new MaterialDialog(getActivity());
        materialDialog.setTitle(getString(R.string.dialog_title_permanently_delete));
        materialDialog.setMessage(getString(R.string.dialog_msg_permanently_delete));
        materialDialog.setPositiveButton(getString(R.string.dialog_btn_positive_permanently_delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionDelete(emailsMarkedForAction);
                materialDialog.dismiss();
            }
        });
        materialDialog.setNegativeButton(getString(R.string.dialog_btn_negative_permanently_delete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionTrash(emailsMarkedForAction);
                materialDialog.dismiss();
            }
        });
        materialDialog.setCanceledOnTouchOutside(true);
        materialDialog.show();
    }

    private void performActionDelete(ArrayList<EmailMessage> emailsMarkedForAction) {
        Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_deleting), Snackbar.LENGTH_LONG).show();
        new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_delete)).execute();
        setupDeleteAndComposeFABs(false);
        AnalyticsAPI.sendMultipleMailAction(AnalyticsAPI.ACTION_DELETE, emailsMarkedForAction, getContext());
    }

    private void performActionTrash(ArrayList<EmailMessage> emailsMarkedForAction) {
        Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trashing), Snackbar.LENGTH_LONG).show();
        new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_trash)).execute();
        setupDeleteAndComposeFABs(false);
        AnalyticsAPI.sendMultipleMailAction(AnalyticsAPI.ACTION_TRASH, emailsMarkedForAction, getContext());
    }

    private void performActionRead(ArrayList<EmailMessage> emailsMarkedForAction) {
        Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_marking_read), Snackbar.LENGTH_LONG).show();
        new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_read)).execute();
        setupDeleteAndComposeFABs(false);
        AnalyticsAPI.sendMultipleMailAction(AnalyticsAPI.ACTION_MARK_READ, emailsMarkedForAction, getContext());
    }

    private void performActionUnread(ArrayList<EmailMessage> emailsMarkedForAction) {
        Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_marking_unread), Snackbar.LENGTH_LONG).show();
        new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_unread)).execute();
        setupDeleteAndComposeFABs(false);
        AnalyticsAPI.sendMultipleMailAction(AnalyticsAPI.ACTION_MARK_UNREAD, emailsMarkedForAction, getContext());
    }
}
package com.sigmobile.dawebmail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sigmobile.dawebmail.asyncTasks.MailAction;
import com.sigmobile.dawebmail.asyncTasks.MailActionListener;
import com.sigmobile.dawebmail.asyncTasks.ViewMailListener;
import com.sigmobile.dawebmail.asyncTasks.ViewMailManager;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.BasePath;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.DateUtils;
import com.sigmobile.dawebmail.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewEmail extends AppCompatActivity implements ViewMailListener {

    private static final String TAG = "ViewEmail";

    private EmailMessage currentEmail;
    private String EMAIL_TYPE;
    private ProgressDialog progressDialog;
    private User currentUser;

    @Bind(R.id.view_tool_bar)
    Toolbar toolbar;
    @Bind(R.id.viewmail_sender)
    TextView senderName;
    @Bind(R.id.viewmail_subject)
    TextView subject;
    @Bind(R.id.viewmail_senderbottom)
    TextView senderNameBottom;
    @Bind(R.id.viewmail_datebottom)
    TextView dateBottom;
    @Bind(R.id.viewmail_webview)
    WebView contentContainer;
    @Bind(R.id.viewmail_attach_ll)
    LinearLayout attachmentsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_email);
        ButterKnife.bind(this);

        currentUser = UserSettings.getCurrentUser(this);
        progressDialog = new ProgressDialog(ViewEmail.this);

        setupBundleReceivers();
        setupToolbar();
        showEmailContent();

    }

    private void showEmailContent() {
        if (currentEmail.content.equals("") || currentEmail.content == null) {
            setEmailContent(getString(R.string.error_connect_to_view_email));
            /*
            *since we dont have to sendMsg in to view the webmail anymore
            * (since basic auth in restapi provides us to be logged in always)
            * I am directly calling viewmailmanager
             */
            new ViewMailManager(currentUser, getApplicationContext(), ViewEmail.this, currentEmail).execute();
        } else {
            setEmailContent(currentEmail.content);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRefreshBroadcast();
                finish();
            }
        });
        getSupportActionBar().setTitle("@" + currentEmail.fromName);
    }

    private void setupBundleReceivers() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            currentEmail = (EmailMessage) bundle.getSerializable(Constants.CURRENT_EMAIL_SERIALIZABLE);
            EMAIL_TYPE = bundle.getString(Constants.CURRENT_EMAIL_TYPE);
            long EMAIL_ID = bundle.getLong(Constants.CURRENT_EMAIL_ID);
            if (EMAIL_ID != -1)
                currentEmail.setId(EMAIL_ID);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendRefreshBroadcast();
        finish();
    }

    @Override
    public void onPreView() {
        progressDialog = ProgressDialog.show(ViewEmail.this, "", getString(R.string.dialog_msg_fetching_content), true);
        progressDialog.setCancelable(false);
    }

    @Override
    public void onPostView(EmailMessage emailMessage) {
        if (emailMessage != null) {

            if (currentEmail.readUnread.equals(Constants.WEBMAIL_UNREAD))
                markWebmailAsRead();

            setEmailContent(emailMessage.content);
            emailMessage.readUnread = Constants.WEBMAIL_READ;
            if (EMAIL_TYPE.equals(Constants.INBOX)) {
                emailMessage.save();
            }
            currentEmail = emailMessage;
        } else {
            setEmailContent(getString(R.string.error_connect_to_view_email));
        }
        progressDialog.dismiss();
    }


    public void setEmailContent(String html) {
        String mime = "text/html";
        String encoding = "utf-8";

        contentContainer.getSettings().setJavaScriptEnabled(true);
        contentContainer.loadDataWithBaseURL(null, html, mime, encoding, null);

        senderName.setText(currentEmail.fromAddress);
        subject.setText(currentEmail.subject);
        senderNameBottom.setText(currentEmail.fromName);
        dateBottom.setText(DateUtils.getDate(getApplicationContext(), Long.parseLong(currentEmail.dateInMillis)));

        final ArrayList<String> attachmentsList = BasePath.getAttachmentsPaths(getApplicationContext(), currentEmail.contentID);
        for (int i = 0; i < attachmentsList.size(); i++) {
            TextView textView = new TextView(this);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(10, 10, 10, 10);
            textView.setLayoutParams(layoutParams);
            textView.setText("Attachment-" + i + " " + (new File(attachmentsList.get(i))).getName().split("-")[1]);
            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "" + (new File(attachmentsList.get(finalI))).getName(), Snackbar.LENGTH_LONG).show();
                    FileUtils.openDoc(getApplicationContext(), attachmentsList.get(finalI));
                }
            });
            attachmentsContainer.addView(textView);
        }
    }

    private void sendRefreshBroadcast() {
        Intent intent = new Intent(Constants.BROADCAST_REFRESH_ADAPTERS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void markWebmailAsRead() {
        new MailAction(getApplicationContext(), currentUser, getString(R.string.msg_action_read), "" + currentEmail.contentID, new MailActionListener() {
            @Override
            public void onPreMailAction() {

            }

            @Override
            public void onPostMailAction(boolean success) {
            }
        }).execute();
    }
}
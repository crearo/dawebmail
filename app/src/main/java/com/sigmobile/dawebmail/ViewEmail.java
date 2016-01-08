package com.sigmobile.dawebmail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.sigmobile.dawebmail.asyncTasks.ViewMailListener;
import com.sigmobile.dawebmail.asyncTasks.ViewMailManager;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.utils.ConnectionManager;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.DateUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class ViewEmail extends AppCompatActivity implements ViewMailListener {

    EmailMessage currentEmail;
    String EMAIL_TYPE;

    String username, pwd;
    ProgressDialog progdialog;

    @Bind(R.id.view_tool_bar)
    Toolbar toolbar;

    @Bind(R.id.viewmail_sender)
    TextView tvsender;

    @Bind(R.id.viewmail_subject)
    TextView tvsubject;

    @Bind(R.id.viewmail_senderbottom)
    TextView tvsenderbottom;

    @Bind(R.id.viewmail_datebottom)
    TextView tvdatebottom;

    @Bind(R.id.viewmail_webview)
    WebView webView_viewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_email);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRefreshBroadcast();
                finish();
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            currentEmail = (EmailMessage) bundle.getSerializable(Constants.CURRENT_EMAIL_SERIALIZABLE);
            EMAIL_TYPE = bundle.getString(Constants.CURRENT_EMAIL_TYPE);
            long EMAIL_ID = bundle.getLong(Constants.CURRENT_EMAIL_ID);
            if (EMAIL_ID != -1)
                currentEmail.setId(EMAIL_ID);
        }

        webView_viewContent.setBackgroundColor(Color.parseColor("#E7E7E7"));
        progdialog = new ProgressDialog(ViewEmail.this);

        getSupportActionBar().setTitle("@" + currentEmail.fromName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.TextPrimaryAlternate));

        if (currentEmail.content.equals("") || currentEmail.content == null) {
            setEmailContent("<html><head></head><body>Connect to the Internet to download content</body></html>");
            /*
            *since we dont have to login in to view the webmail anymore
            * (since basic auth in restapi provides us to be logged in always)
            * I am directly calling viewmailmanager
             */
            new ViewMailManager(getApplicationContext(), ViewEmail.this, currentEmail).execute();
            Log.d("T", "Called ViewMailManager");
        } else {
            setEmailContent(currentEmail.content);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Constants.BROADCAST_REFRESH_ADAPTERS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }

    @Override
    public void onPreView() {
        progdialog = ProgressDialog.show(ViewEmail.this, "", "Fetching Content.", true);
        progdialog.setCancelable(false);
    }

    @Override
    public void onPostView(EmailMessage emailMessage) {
        if (emailMessage != null) {
            setEmailContent(emailMessage.content);
            emailMessage.readUnread = Constants.WEBMAIL_READ;
            if (EMAIL_TYPE.equals(Constants.INBOX)) {
                Log.wtf("VE", "" + emailMessage.getId());
                emailMessage.save();
            }
            currentEmail = emailMessage;
        } else {
            setEmailContent("<html><head></head><body>Connect to the Internet to download content</body></html>");
        }
        progdialog.dismiss();
    }


    public void createDownloadDialog(final int whichattatchment) {
        final MaterialDialog materialDialog = new MaterialDialog(ViewEmail.this);
        materialDialog.setTitle("Download the attachment?");
        if (ConnectionManager.isConnectedByMobileData(ViewEmail.this))
            materialDialog.setMessage("You are conected over mobile network.");
        if (ConnectionManager.isConnectedByWifi(ViewEmail.this))
            materialDialog.setMessage("You are conected over wifi network.");

        materialDialog.setNegativeButton("No", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.setPositiveButton("Download", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.show();

    }

    public void setEmailContent(String html) {
        String mime = "text/html";
        String encoding = "utf-8";

        webView_viewContent.getSettings().setJavaScriptEnabled(true);
        webView_viewContent.loadDataWithBaseURL(null, html, mime, encoding, null);

        tvsender.setText(currentEmail.fromAddress);
        tvsubject.setText(currentEmail.subject);
        tvsenderbottom.setText(currentEmail.fromName);
        tvdatebottom.setText(DateUtils.getDate(Long.parseLong(currentEmail.dateInMillis)));
    }

    private void sendRefreshBroadcast() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(Constants.BROADCAST_REFRESH_ADAPTERS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
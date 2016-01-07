package com.sigmobile.dawebmail;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sigmobile.dawebmail.asyncTasks.Login;
import com.sigmobile.dawebmail.asyncTasks.LoginListener;
import com.sigmobile.dawebmail.asyncTasks.ViewMailListener;
import com.sigmobile.dawebmail.asyncTasks.ViewMailManager;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.ConnectionManager;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Printer;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class ViewEmail extends AppCompatActivity implements LoginListener, ViewMailListener {

    EmailMessage currentEmail;
    long currentEmailID;

    TextView tvsender, tvsubject, tvsenderbottom, tvdatebottom;
    ImageView att1, att2, att3;
    LinearLayout attll1, attll2, attll3;
    TextView tvatt;
    String username, pwd;
    WebView webView_viewContent;

    ProgressDialog progdialog;

    @Bind(R.id.view_tool_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_email);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRefreshBroadcast();
                finish();
            }
        });

        currentEmailID = getIntent().getLongExtra(Constants.CURRENT_EMAIL_ID, -1);
        currentEmail = EmailMessage.findById(EmailMessage.class, currentEmailID);

        tvsender = (TextView) findViewById(R.id.viewmail_sender);
        tvsubject = (TextView) findViewById(R.id.viewmail_subject);
        tvsenderbottom = (TextView) findViewById(R.id.viewmail_senderbottom);
        tvdatebottom = (TextView) findViewById(R.id.viewmail_datebottom);
        att1 = (ImageView) findViewById(R.id.viewemail_attachment1);
        att2 = (ImageView) findViewById(R.id.viewemail_attachment2);
        att3 = (ImageView) findViewById(R.id.viewemail_attachment3);

        attll1 = (LinearLayout) findViewById(R.id.viewemail_attll1);
        attll2 = (LinearLayout) findViewById(R.id.viewemail_attll2);
        attll3 = (LinearLayout) findViewById(R.id.viewemail_attll3);
        tvatt = (TextView) findViewById(R.id.viewemail_attatchment1_tv);

        webView_viewContent = (WebView) findViewById(R.id.viewmail_webview);
        webView_viewContent.setBackgroundColor(Color.parseColor("#E7E7E7"));

        attll1.setVisibility(View.GONE);
        attll2.setVisibility(View.GONE);
        attll3.setVisibility(View.GONE);

        showEmailAttachments();

        attll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createDownloadDialog(1);
            }
        });
        attll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createDownloadDialog(2);
            }
        });
        attll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createDownloadDialog(3);
            }
        });

        progdialog = new ProgressDialog(ViewEmail.this);

        if (currentEmail.content.equals("isempty")) {
            // load content only if logged in.
            setWebviewContent("<html><head></head><body>Connect to the Internet to download content</body></html>");
            tvdatebottom.setText("" + currentEmail.dateInMillis);
            tvsender.setText("" + currentEmail.fromName);
            tvsenderbottom.setText("");
            tvsubject.setText("" + currentEmail.subject);

            System.out.println("ISEMPTY");
            if (!User.isLoggedIn()) {
                System.out.println("User Not Logged In");
                new Login(getApplicationContext(), ViewEmail.this).execute();
            } else {
                System.out.println("Logged In, Viewing Mail");
                new ViewMailManager(getApplicationContext(), ViewEmail.this, currentEmail).execute();
            }
        } else {
            Printer.println("fromName - " + currentEmail.fromName);
            Printer.println("fromadd - " + currentEmail.fromAddress);

            if (currentEmail.fromName.contains("...") && (!currentEmail.fromAddress.equals("isempty")))
                tvsender.setText(currentEmail.fromAddress);
            else
                tvsender.setText(currentEmail.fromAddress);

            tvsubject.setText(currentEmail.subject);

            if (currentEmail.content.equals("isempty")) {
                setWebviewContent("<html><head></head><body>Connect to the Internet to download content</body></html>");
            } else {
                setWebviewContent(currentEmail.content);
            }
            tvsenderbottom.setText(currentEmail.fromName);
            tvdatebottom.setText(currentEmail.dateInMillis);
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
    public void onPreLogin() {
        progdialog = ProgressDialog.show(ViewEmail.this, "", "Logging in.", true);
        progdialog.setCancelable(false);
    }

    @Override
    public void onPostLogin(boolean loginSuccess, String timeTaken) {
        if (loginSuccess) {
            Toast.makeText(ViewEmail.this.getApplicationContext(), "Logged in!", Toast.LENGTH_SHORT).show();
            progdialog.dismiss();
            new ViewMailManager(ViewEmail.this, ViewEmail.this, currentEmail).execute();
        } else {
            Toast.makeText(ViewEmail.this.getApplicationContext(), "Login Unsuccessful", Toast.LENGTH_SHORT).show();
            progdialog.dismiss();
        }
        ViewEmail.this.invalidateOptionsMenu();
    }

    @Override
    public void onPreView() {
        progdialog = ProgressDialog.show(ViewEmail.this, "", "Fetching Content.", true);
        progdialog.setCancelable(false);
    }

    @Override
    public void onPostView(boolean success) {
        tvsender.setText(currentEmail.fromAddress);
        setWebviewContent(currentEmail.content);
        tvsubject.setText(currentEmail.subject);
        tvsenderbottom.setText(currentEmail.fromName);
        tvdatebottom.setText(currentEmail.dateInMillis);
        showEmailAttachments();
        progdialog.dismiss();
    }


    public void downloadAttachment(String link, int attnumber) {

        Printer.println("link = " + link);

        if (!(link.equals("isempty") || link.equals("notempty"))) {
            link = link.substring(0, link.indexOf("&auth"));
            Printer.println(link);
            String webpage = link;

            String authString = username + ":" + pwd;
            Printer.println("auth string: " + authString);
            String authStringEnc = Base64.encodeToString(authString.getBytes(), Base64.DEFAULT);

            Printer.println("Base64 encoded auth string: " + authStringEnc);
            DownloadManager downloadManager = (DownloadManager) ViewEmail.this.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(webpage));
            request.addRequestHeader("Authorization", "Basic " + authStringEnc);
            request.setTitle(currentEmail.subject + "_Attachment_" + attnumber);
            request.setDestinationInExternalFilesDir(ViewEmail.this.getApplicationContext(), Environment.DIRECTORY_DOWNLOADS,
                    currentEmail.subject + "_Attachment_" + attnumber);
            downloadManager.enqueue(request);

            startActivity(new Intent().setAction(DownloadManager.ACTION_VIEW_DOWNLOADS));
        }
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

    public void showEmailAttachments() {
    }

    public void setWebviewContent(String html) {
        String mime = "text/html";
        String encoding = "utf-8";

        webView_viewContent.getSettings().setJavaScriptEnabled(true);
        webView_viewContent.loadDataWithBaseURL(null, html, mime, encoding, null);
    }

    private void sendRefreshBroadcast() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(Constants.BROADCAST_REFRESH_ADAPTERS);
//        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
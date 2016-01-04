package rish.crearo.dawebmaillite;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContributeActivity extends AppCompatActivity {

    @Bind(R.id.contri_email)
    ImageView email;

    @Bind(R.id.contri_blog)
    TextView blog;

    @Bind(R.id.contri_github)
    TextView github;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);

        ButterKnife.bind(this);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blog.getText().toString()));
                startActivity(browserIntent);
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(github.getText().toString()));
                startActivity(browserIntent);
            }
        });
    }

    public void sendEmail() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        sendIntent.setData(Uri.parse("bhardwaj.rish@gmail.com"));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bhardwaj.rish@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DAWebmail");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi Rish!\n\nHere are a few suggestions/complaints about the webmail app : \n\n");
        startActivity(sendIntent);
    }

}

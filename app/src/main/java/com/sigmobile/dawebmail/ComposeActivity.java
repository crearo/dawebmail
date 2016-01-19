package com.sigmobile.dawebmail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeActivity extends AppCompatActivity {

    @Bind(R.id.compose_toolbar)
    Toolbar toolbar;

    @Bind(R.id.compose_to_id)
    EditText et_to;

    @Bind(R.id.compose_subject)
    EditText et_subject;

    @Bind(R.id.compose_content)
    EditText et_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        ButterKnife.bind(this);



    }
}

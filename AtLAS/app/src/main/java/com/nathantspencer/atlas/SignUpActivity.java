package com.nathantspencer.atlas;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button mFinishSignUpButton = (Button) findViewById(R.id.finish_sign_up);
        mFinishSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finishSignUp();
            }
        });
    }

    private void finishSignUp() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

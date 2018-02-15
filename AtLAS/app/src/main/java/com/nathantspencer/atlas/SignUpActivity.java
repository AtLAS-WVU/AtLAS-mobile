package com.nathantspencer.atlas;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity which provides an interface for user sign up.
 */
public class SignUpActivity extends AppCompatActivity {

    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mFirstNameView;
    private AutoCompleteTextView mLastNameView;
    private EditText mPasswordView;

    protected static GeneralRequest mGeneralRequest;

    private class SignUpRequestResponder implements RequestResponder {

        SignUpRequestResponder(){}

        public void onResponse(String response) {
            // grab value of response field "success"
            Boolean success = false;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                success = jsonResponse.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // clear fields
            mUsernameView.setText("");
            mPasswordView.setText("");
            mEmailView.setText("");
            mFirstNameView.setText("");
            mLastNameView.setText("");

            if (success) {

                // show success message
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                builder.setMessage("Your account has been created. You may now log in with this account.")
                        .setTitle("Account Created");
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.dismiss();

                // transition to LoginActivity
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            } else {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String debugString = jsonResponse.getString("debug");

                    // show login failure alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(debugString)
                            .setTitle("Sign Up Failed");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } catch (JSONException e) {
                    e.printStackTrace();

                    // show failure message
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage("Something went wrong on our end. Try again!")
                            .setTitle("Hmm...");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.last_name);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button finishSignUpButton = (Button) findViewById(R.id.finish_sign_up);
        finishSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finishSignUp();
            }
        });
    }

    private void finishSignUp()
    {
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String email = mEmailView.getText().toString();
        final String firstName = mFirstNameView.getText().toString();
        final String lastName = mLastNameView.getText().toString();

        Map<String, String> parameterBody = new HashMap<>();
        parameterBody.put("username", username);
        parameterBody.put("password", password);
        parameterBody.put("email", email);
        parameterBody.put("first_name", firstName);
        parameterBody.put("last_name", lastName);

        GeneralRequest generalRequest = LoginActivity.GetGeneralRequest();
        generalRequest.POSTRequest("CreateAccount.php", parameterBody, new SignUpActivity.SignUpRequestResponder());
    }

}

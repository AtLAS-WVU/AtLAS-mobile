package com.nathantspencer.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.nathantspencer.atlas.LoginActivity.mGeneralRequest;

public class AddFriendActivity extends AppCompatActivity {

    private View mSearchButton;
    private View mAddFriendButton;

    private TextView mUsernameText;
    private TextView mResultText;
    private TextView mFirstNameText;
    private TextView mLastNameText;
    private TextView mFriendStatusText;
    private TextView mFriendStatusLabel;

    private class UserSearchRequestResponder implements RequestResponder {

        UserSearchRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            // grab value of response field "success"
            Boolean success = false;
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                success = jsonResponse.getBoolean("success");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            if(success)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    String firstName = jsonResponse.getString("first_name");
                    String lastName = jsonResponse.getString("last_name");
                    String friendStatus = jsonResponse.getString("friend_status").toUpperCase();

                    mResultText.setText(mUsernameText.getText().toString());
                    mFirstNameText.setText(firstName);
                    mLastNameText.setText(lastName);
                    mFriendStatusText.setText(friendStatus);

                    mResultText.setVisibility(View.VISIBLE);
                    mFirstNameText.setVisibility(View.VISIBLE);
                    mLastNameText.setVisibility(View.VISIBLE);
                    mFriendStatusLabel.setVisibility(View.VISIBLE);
                    mFriendStatusText.setVisibility(View.VISIBLE);

                    SharedPreferences sharedPref = AddFriendActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                    String username = sharedPref.getString("atlasUsername", "");

                    if(friendStatus.equals("STRANGER") && !username.equals(mUsernameText.getText().toString()))
                    {
                        mAddFriendButton.setVisibility(View.VISIBLE);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    String debug = jsonResponse.getString("debug");
                    if(debug.equals("Unable to find friend's ID"))
                    {
                        mResultText.setText("User not found.");
                        mResultText.setVisibility(View.VISIBLE);
                        mFirstNameText.setVisibility(View.INVISIBLE);
                        mLastNameText.setVisibility(View.INVISIBLE);
                        mFriendStatusLabel.setVisibility(View.INVISIBLE);
                        mFriendStatusText.setVisibility(View.INVISIBLE);
                        mAddFriendButton.setVisibility(View.INVISIBLE);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchButton = findViewById(R.id.search_button);
        mUsernameText = (TextView) findViewById(R.id.username_text);
        mResultText = (TextView) findViewById(R.id.result_text);
        mFirstNameText = (TextView) findViewById(R.id.first_name_text);
        mLastNameText = (TextView) findViewById(R.id.last_name_text);
        mFriendStatusLabel = (TextView) findViewById(R.id.friend_status_label);
        mFriendStatusText = (TextView) findViewById(R.id.friend_status_text);
        mAddFriendButton = findViewById(R.id.add_friend_button);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send search request
                SharedPreferences sharedPref = AddFriendActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                String username = sharedPref.getString("atlasUsername", "");
                String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");
                String friendUsername = mUsernameText.getText().toString();

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("friend_username", friendUsername);
                parameterBody.put("token", atlasLoginKey);

                if(!friendUsername.equals(""))
                {
                    mGeneralRequest.POSTRequest("UserByUsername.php", parameterBody, new AddFriendActivity.UserSearchRequestResponder());
                }
            }
        });

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send search request
                SharedPreferences sharedPref = AddFriendActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                String username = sharedPref.getString("atlasUsername", "");
                String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");
                String friendUsername = mUsernameText.getText().toString();

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("friend_username", friendUsername);
                parameterBody.put("token", atlasLoginKey);

                if(!friendUsername.equals(""))
                {
                    mGeneralRequest.POSTRequest("UserByUsername.php", parameterBody, new AddFriendActivity.UserSearchRequestResponder());
                }
            }
        });

        mResultText.setVisibility(View.INVISIBLE);
        mFirstNameText.setVisibility(View.INVISIBLE);
        mLastNameText.setVisibility(View.INVISIBLE);
        mFriendStatusLabel.setVisibility(View.INVISIBLE);
        mFriendStatusText.setVisibility(View.INVISIBLE);
        mAddFriendButton.setVisibility(View.INVISIBLE);
    }

}

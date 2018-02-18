package com.nathantspencer.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.nathantspencer.atlas.LoginActivity.mGeneralRequest;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mSignOutButton;
    private View mAddFriendButton;
    private View mMapView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // clear all elements before displaying those which are relevant
            mAddFriendButton.setVisibility(View.GONE);
            mMapView.setVisibility(View.GONE);

            switch (item.getItemId()) {

                case R.id.navigation_history:
                    return true;

                case R.id.navigation_map:
                    mMapView.setVisibility(View.VISIBLE);
                    return true;

                case R.id.navigation_send:
                    mAddFriendButton.setVisibility(View.VISIBLE);

                    return true;

                default:
                    return false;
            }
        }

    };

    private class LogoutRequestResponder implements RequestResponder {

        LogoutRequestResponder()
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
                // remove authentication key
                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("atlasLoginKey");
                editor.apply();

                // move back to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                // show login failure alert
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Operation failed. Please contact support!")
                        .setTitle("Logout Failed");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignOutButton = (FloatingActionButton) findViewById(R.id.signOutButton);
        mAddFriendButton = findViewById(R.id.add_friend_button);
        mMapView = findViewById(R.id.map_view);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(1).setChecked(true);

        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String username = sharedPref.getString("atlasUsername", "");
        TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send sign out request
                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                String username = sharedPref.getString("atlasUsername", "");
                String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("key", atlasLoginKey);
                mGeneralRequest.POSTRequest("LogUserOut.php", parameterBody, new LogoutRequestResponder());

            }
        });

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                startActivity(intent);
            }
        });

    }

}

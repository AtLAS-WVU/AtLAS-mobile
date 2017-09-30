package com.nathantspencer.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FloatingActionButton mSignOutButton;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    mTextMessage.setText(R.string.title_history);
                    return true;
                case R.id.navigation_map:
                    mTextMessage.setText(R.string.title_map);
                    return true;
                case R.id.navigation_send:
                    mTextMessage.setText(R.string.title_send);
                    return true;
                default:
                    return false;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSignOutButton = (FloatingActionButton) findViewById(R.id.signOutButton);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
    }

}

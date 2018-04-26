package com.nathantspencer.atlas;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.nathantspencer.atlas.LoginActivity.mGeneralRequest;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext;

    private FloatingActionButton mSignOutButton;
    private View mAddFriendButton;
    private TextView mPendingDeliveriesText;
    private SupportMapFragment mMapFragment;
    private ListView mFriendsList;
    private ListView mDeliveriesList;

    private ArrayList<String> mFriendUsernames;
    private ArrayList<Boolean> mFriendIsPending;
    private ArrayList<String> mFriendNames;

    private ArrayList<String> mDeliveryUsernames;
    private ArrayList<Boolean> mDeliveryIsPending;
    private ArrayList<String> mDeliveryStatuses;
    private ArrayList<String> mDeliveryDescriptions;
    private ArrayList<String> mDeliveryRequestNumber;

    private GoogleMap mMap;
    private LatLng mLocation;
    private Boolean mInitalLocationSet;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // clear all elements before displaying those which are relevant
            mAddFriendButton.setVisibility(View.GONE);
            mFriendsList.setVisibility(View.GONE);
            mDeliveriesList.setVisibility(View.GONE);
            mPendingDeliveriesText.setVisibility(View.GONE);
            mMapFragment.getView().setVisibility(View.GONE);

            switch (item.getItemId()) {

                case R.id.navigation_history:
                    mDeliveriesList.setVisibility(View.VISIBLE);
                    mPendingDeliveriesText.setVisibility(View.VISIBLE);
                    return true;

                case R.id.navigation_map:
                    mMapFragment.getView().setVisibility(View.VISIBLE);
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    return true;

                case R.id.navigation_send:
                    mAddFriendButton.setVisibility(View.VISIBLE);
                    mFriendsList.setVisibility(View.VISIBLE);
                    return true;

                default:
                    return false;
            }
        }

    };

    private class PendingDeliveriesRequestResponder implements RequestResponder {

        PendingDeliveriesRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            // grab value of response field "success"
            Boolean success;
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                success = jsonResponse.getBoolean("success");

                if(success)
                {
                    try
                    {
                        JSONArray requests = jsonResponse.getJSONArray("pending_requests");
                        for (int i = 0; i < requests.length(); i++) {
                            JSONObject request = requests.getJSONObject(i);
                            mDeliveryUsernames.add(request.get("receiver_username").toString());
                            mDeliveryStatuses.add(request.get("request_date").toString());
                            mDeliveryDescriptions.add(request.get("delivery_message").toString());
                            mDeliveryIsPending.add(request.getBoolean("can_we_approve"));
                            mDeliveryRequestNumber.add(request.get("requestID").toString());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                final DeliveriesArrayAdapter arrayAdapter = new DeliveriesArrayAdapter
                        (mDeliveryUsernames, mDeliveryIsPending, mDeliveryStatuses, mDeliveryDescriptions, mDeliveryRequestNumber, MainActivity.this);

                mDeliveriesList.setAdapter(arrayAdapter);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class GetDroneLocationRequestResponder implements RequestResponder {

        GetDroneLocationRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            try
            {
                // TODO: ensure this actually agrees with the endpoint...

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray deliveries = jsonResponse.getJSONArray("delivery_data");
                for(int i = 0; i < deliveries.length(); ++i)
                {
                    JSONObject deliveryData = deliveries.getJSONObject(i);

                    Double latitude = deliveryData.getDouble("latitude");
                    Double longitude = deliveryData.getDouble("longitude");
                    LatLng droneLocation = new LatLng(latitude, longitude);

                    mMap.addMarker(new MarkerOptions().position(droneLocation)
                            .title("Drone Location"));

                }
            }
            catch(JSONException e)
            {
            }
        }
    }

    private class GetDeliveriesRequestResponder implements RequestResponder {

        GetDeliveriesRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray deliveries = jsonResponse.getJSONArray("pending_requests");

                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                final String username = sharedPref.getString("atlasUsername", "");
                final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                for(int i = 0; i < deliveries.length(); ++i)
                {
                    JSONObject delivery = deliveries.getJSONObject(i);
                    if(delivery.getBoolean("waiting_for_us"))
                    {
                        continue;
                    }

                    Map<String, String> parameterBody = new HashMap<>();
                    parameterBody.put("username", username);
                    parameterBody.put("token", atlasLoginKey);
                    parameterBody.put("delivery_id", delivery.getString("delivery_id"));

                    mGeneralRequest.POSTRequest("GetDroneLocation.php", parameterBody, new GetDroneLocationRequestResponder());
                }
            }
            catch(JSONException e)
            {
            }

        }
    }


    private class UpdateUserLocationRequestResponder implements RequestResponder {

        UpdateUserLocationRequestResponder()
        {
        }

        public void onResponse(String response)
        {
        }
    }

    private class PendingListRequestResponder implements RequestResponder {

        PendingListRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            // grab value of response field "success"
            Boolean success;
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                success = jsonResponse.getBoolean("success");

                if(success)
                {
                    JSONArray friends = jsonResponse.getJSONArray("pending_friends");
                    for (int i = 0; i < friends.length(); i++)
                    {
                        JSONObject friend = friends.getJSONObject(i);
                        mFriendUsernames.add(friend.get("username").toString());
                        mFriendNames.add(friend.get("first_name") + " " + friend.get("last_name"));
                        mFriendIsPending.add(true);
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class FriendsListRequestResponder implements RequestResponder {

        FriendsListRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            // grab value of response field "success"
            Boolean success;
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                success = jsonResponse.getBoolean("success");

                if(success)
                {
                    JSONArray friends = jsonResponse.getJSONArray("connections");
                    for (int i = 0; i < friends.length(); i++)
                    {
                        JSONObject friend = friends.getJSONObject(i);
                        if (friend.get("status").equals("friend"))
                        {
                            mFriendUsernames.add(friend.get("username").toString());
                            mFriendNames.add(friend.get("first_name") + " " + friend.get("last_name"));
                            mFriendIsPending.add(false);
                        }
                    }

                    final FriendsArrayAdapter arrayAdapter = new FriendsArrayAdapter
                            (mFriendUsernames, mFriendIsPending, mFriendNames, MainActivity.this);

                    mFriendsList.setAdapter(arrayAdapter);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

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

    protected void RefreshFriends()
    {
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("atlasUsername", "");
        final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

        mFriendIsPending.clear();
        mFriendUsernames.clear();
        mFriendNames.clear();

        Map<String, String> parameterBody = new HashMap<>();
        parameterBody.put("username", username);
        parameterBody.put("token", atlasLoginKey);
        mGeneralRequest.GETRequest("PendingFriends.php", parameterBody, new PendingListRequestResponder());
        mGeneralRequest.GETRequest("FriendsList.php", parameterBody, new FriendsListRequestResponder());
    }

    protected void RefreshDeliveries()
    {
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("atlasUsername", "");
        final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

        mDeliveryUsernames.clear();
        mDeliveryIsPending.clear();
        mDeliveryStatuses.clear();
        mDeliveryDescriptions.clear();
        mDeliveryRequestNumber.clear();

        Map<String, String> parameterBody = new HashMap<>();
        parameterBody.put("username", username);
        parameterBody.put("token", atlasLoginKey);
        mGeneralRequest.GETRequest("PendingRequests.php", parameterBody, new PendingDeliveriesRequestResponder());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInitalLocationSet = false;

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mMapFragment.getMapAsync(this);
        mMapFragment.getView().setVisibility(View.GONE);

        mSignOutButton = (FloatingActionButton) findViewById(R.id.signOutButton);
        mAddFriendButton = findViewById(R.id.add_friend_button);
        mFriendsList = (ListView) findViewById(R.id.friend_list);
        mDeliveriesList =  (ListView) findViewById(R.id.delivery_list);
        mPendingDeliveriesText = (TextView) findViewById(R.id.pending_deliveries_text);

        // unclear why this is necessary, as the layout should impose this already
        mDeliveriesList.setVisibility(View.GONE);
        mPendingDeliveriesText.setVisibility(View.GONE);

        mFriendUsernames = new ArrayList<>();
        mFriendIsPending = new ArrayList<>();
        mFriendNames = new ArrayList<>();

        mContext = this;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDeliveryUsernames = new ArrayList<>();
        mDeliveryIsPending = new ArrayList<>();
        mDeliveryStatuses = new ArrayList<>();
        mDeliveryDescriptions = new ArrayList<>();
        mDeliveryRequestNumber = new ArrayList<>();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {

            // updates user location every 5 seconds
            @Override
            public void run()
            {
                if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>()
                        {
                            @Override
                            public void onSuccess(Location location)
                            {
                                if(location != null)
                                {
                                    SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                                    final String username = sharedPref.getString("atlasUsername", "");
                                    final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                                    mLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    Map<String, String> parameterBody = new HashMap<>();
                                    parameterBody.put("username", username);
                                    parameterBody.put("token", atlasLoginKey);
                                    parameterBody.put("longitude", Double.toString(location.getLongitude()));
                                    parameterBody.put("latitude", Double.toString(location.getLatitude()));
                                    mGeneralRequest.POSTRequest("UpdateUserLocation.php", parameterBody, new UpdateUserLocationRequestResponder());

                                    if (mLocation != null)
                                    {
                                        mMap.clear();

                                        mMap.addMarker(new MarkerOptions().position(mLocation)
                                                .title("Your Location"));

                                        Map<String, String> deliveriesBody = new HashMap<>();
                                        deliveriesBody.put("username", username);
                                        deliveriesBody.put("token", atlasLoginKey);
                                        mGeneralRequest.GETRequest("GetDeliveries.php", deliveriesBody, new GetDeliveriesRequestResponder());

                                        if(!mInitalLocationSet)
                                        {
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 17.5f));
                                            mInitalLocationSet = true;
                                        }

                                    }
                                }
                            }
                        });

                }
                else
                {
                    ActivityCompat.requestPermissions((MainActivity) mContext, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
                }

            }
        }, 0, 5000);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(1).setChecked(true);

        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("atlasUsername", "");
        final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");
        TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Map<String, String> parameterBody = new HashMap<>();
        parameterBody.put("username", username);
        parameterBody.put("token", atlasLoginKey);

        mGeneralRequest.GETRequest("PendingFriends.php", parameterBody, new PendingListRequestResponder());
        mGeneralRequest.GETRequest("FriendsList.php", parameterBody, new FriendsListRequestResponder());
        mGeneralRequest.GETRequest("PendingRequests.php", parameterBody, new PendingDeliveriesRequestResponder());

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }
}

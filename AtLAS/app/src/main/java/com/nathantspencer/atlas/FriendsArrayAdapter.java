package com.nathantspencer.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.nathantspencer.atlas.LoginActivity.mGeneralRequest;

public class FriendsArrayAdapter extends BaseAdapter implements ListAdapter
{
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<Boolean> mIsPending = new ArrayList<>();
    private Context mContext;

    private class ConfirmFriendRequestResponder implements RequestResponder
    {
        ConfirmFriendRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            ((MainActivity) mContext).RefreshFriends();
        }
    }

    private class DenyFriendRequestResponder implements RequestResponder
    {
        DenyFriendRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            ((MainActivity) mContext).RefreshFriends();
        }
    }

    private class DeleteFriendRequestResponder implements RequestResponder
    {
        DeleteFriendRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            ((MainActivity) mContext).RefreshFriends();
        }
    }


    public FriendsArrayAdapter(ArrayList<String> list, ArrayList<Boolean> isPendingList, Context context)
    {
        this.mUsernames = list;
        this.mIsPending = isPendingList;
        this.mContext = context;
    }

    @Override
    public int getCount()
    {
        return mUsernames.size();
    }

    @Override
    public Object getItem(int pos)
    {
        return mUsernames.get(pos);
    }

    public Object getUsername(int pos)
    {
        return mUsernames.get(pos);
    }

    @Override
    public long getItemId(int pos)
    {
        return pos;
    }

    public ArrayList<Boolean> getIsPendingList()
    {
        return mIsPending;
    }

    public ArrayList<String> getUsernamesList()
    {
        return mUsernames;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friend, null);
        }

        TextView listItemText = view.findViewById(R.id.list_item_string);
        listItemText.setText(mUsernames.get(position));

        Button denyButton = view.findViewById(R.id.deny_friend);
        Button confirmButton = view.findViewById(R.id.confirm_friend);
        Button deleteButton = view.findViewById(R.id.delete_friend);

        if (!mIsPending.get(position))
        {
            denyButton.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
        }
        else
        {
            deleteButton.setVisibility(View.GONE);
        }

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sharedPref = mContext.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                final String username = sharedPref.getString("atlasUsername", "");
                final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("friend_username", mUsernames.get(position));
                parameterBody.put("token", atlasLoginKey);
                mGeneralRequest.POSTRequest("DenyFriend.php", parameterBody, new DenyFriendRequestResponder());
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sharedPref = mContext.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                final String username = sharedPref.getString("atlasUsername", "");
                final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("friend_username", mUsernames.get(position));
                parameterBody.put("token", atlasLoginKey);
                mGeneralRequest.POSTRequest("ConfirmFriend.php", parameterBody, new ConfirmFriendRequestResponder());
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sharedPref = mContext.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                final String username = sharedPref.getString("atlasUsername", "");
                final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                Map<String, String> parameterBody = new HashMap<>();
                parameterBody.put("username", username);
                parameterBody.put("friend_username", mUsernames.get(position));
                parameterBody.put("token", atlasLoginKey);
                mGeneralRequest.POSTRequest("DeleteFriend.php", parameterBody, new DeleteFriendRequestResponder());
            }
        });

        return view;
    }
}

package com.nathantspencer.atlas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private ArrayList<String> mNames = new ArrayList<>();
    private Context mContext;

    private Button mSendButton;

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

    private class RequestToSendRequestResponder implements RequestResponder
    {
        RequestToSendRequestResponder()
        {
        }

        public void onResponse(String response)
        {
        }
    }



    public FriendsArrayAdapter(ArrayList<String> list, ArrayList<Boolean> isPendingList, ArrayList<String> names, Context context)
    {
        this.mUsernames = list;
        this.mIsPending = isPendingList;
        this.mNames = names;
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

    @Override
    public long getItemId(int pos)
    {
        return pos;
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

        TextView name = view.findViewById(R.id.name);
        name.setText(mNames.get(position));

        Button denyButton = view.findViewById(R.id.deny_friend);
        Button confirmButton = view.findViewById(R.id.confirm_friend);
        Button deleteButton = view.findViewById(R.id.delete_friend);
        mSendButton = view.findViewById(R.id.send_friend);

        if (!mIsPending.get(position))
        {
            denyButton.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
        }
        else
        {
            deleteButton.setVisibility(View.GONE);
            mSendButton.setVisibility(View.GONE);
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
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Friend")
                        .setMessage("Are you sure you want to remove " + mUsernames.get(position) + " as a friend?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPref = mContext.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                                final String username = sharedPref.getString("atlasUsername", "");
                                final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                                Map<String, String> parameterBody = new HashMap<>();
                                parameterBody.put("username", username);
                                parameterBody.put("friend_username", mUsernames.get(position));
                                parameterBody.put("token", atlasLoginKey);
                                mGeneralRequest.POSTRequest("DeleteFriend.php", parameterBody, new DeleteFriendRequestResponder());
                             }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder sendAlert =  new AlertDialog.Builder(mContext);
                sendAlert.setTitle("Add a Request Description");
                sendAlert.setMessage("Type a short message describing the package you'd like to send to " + mUsernames.get(position) + ".");


                final EditText sendMessage = new EditText(mContext);
                sendMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                sendAlert.setView(sendMessage);

                sendAlert.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences sharedPref = mContext.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
                    final String username = sharedPref.getString("atlasUsername", "");
                    final String atlasLoginKey = sharedPref.getString("atlasLoginKey", "");

                    Map<String, String> parameterBody = new HashMap<>();
                    parameterBody.put("username", username);
                    parameterBody.put("friend_username", mUsernames.get(position));
                    parameterBody.put("token", atlasLoginKey);
                    parameterBody.put("delivery_message", "New delivery request from " + username + ".");
                    mGeneralRequest.POSTRequest("RequestToSend.php", parameterBody, new RequestToSendRequestResponder());
                }
                });

                sendAlert.show();
            }
        });

        return view;
    }
}

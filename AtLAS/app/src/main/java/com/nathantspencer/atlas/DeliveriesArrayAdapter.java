package com.nathantspencer.atlas;

import android.content.Context;
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

public class DeliveriesArrayAdapter extends BaseAdapter implements ListAdapter
{
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<Boolean> mIsPending = new ArrayList<>();
    private ArrayList<String> mStatuses = new ArrayList<>();
    private ArrayList<String> mDescriptions = new ArrayList<>();
    private Context mContext;

    private class AcceptDeliveryRequestResponder implements RequestResponder
    {
        AcceptDeliveryRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            ((MainActivity) mContext).RefreshDeliveries();
        }
    }

    private class RejectDeliveryRequestResponder implements RequestResponder
    {
        RejectDeliveryRequestResponder()
        {
        }

        public void onResponse(String response)
        {
            ((MainActivity) mContext).RefreshDeliveries();
        }
    }

    public DeliveriesArrayAdapter(ArrayList<String> list, ArrayList<Boolean> isPendingList, ArrayList<String> statuses, ArrayList<String> descriptions, Context context)
    {
        this.mUsernames = list;
        this.mIsPending = isPendingList;
        this.mStatuses = statuses;
        this.mDescriptions = descriptions;
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
            view = inflater.inflate(R.layout.request, null);
        }

        TextView listItemText = view.findViewById(R.id.list_item_string);
        listItemText.setText(mUsernames.get(position));

        TextView status = view.findViewById(R.id.status);
        status.setText(mStatuses.get(position));

        TextView description = view.findViewById(R.id.description);
        description.setText(mDescriptions.get(position));

        Button acceptButton = view.findViewById(R.id.accept_delivery);
        Button rejectButton = view.findViewById(R.id.reject_delivery);

        if (!mIsPending.get(position))
        {
            acceptButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);
        }

        rejectButton.setOnClickListener(new View.OnClickListener() {
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
               // mGeneralRequest.POSTRequest("DenyFriend.php", parameterBody, new DenyFriendRequestResponder());
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
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
               // mGeneralRequest.POSTRequest("ConfirmFriend.php", parameterBody, new ConfirmFriendRequestResponder());
            }
        });

        return view;
    }
}

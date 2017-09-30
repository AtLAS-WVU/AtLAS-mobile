package com.nathantspencer.atlas;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

interface RequestResponder {
    void onResponse(String response);
}

class GeneralRequest {

    private RequestQueue mRequestQueue;
    final private String mBaseURL = "http://104.237.135.184/AtLAS/";

    GeneralRequest(Context applicationContext)
    {
        mRequestQueue = Volley.newRequestQueue(applicationContext);
    }

    //
    protected void POSTRequest(final String urlSuffix, final Map<String, String> parameterBody, final RequestResponder responder)
    {
        class POSTTask extends AsyncTask<Void, Void, Boolean> {

            private final Map<String, String> mParameterBody;

            private POSTTask(final Map<String, String> parameterBody) {
                mParameterBody = parameterBody;
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                String url = mBaseURL + urlSuffix;
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        VolleyLog.d("Response: %s", response);
                        responder.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        return parameterBody;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };
                mRequestQueue.add(request);
                return true;
            }
        }

        POSTTask task = new POSTTask(parameterBody);
        task.execute((Void) null);
    }
}

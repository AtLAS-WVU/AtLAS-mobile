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

/**
 * An object implementing this interface is needed to respond to requests made through
 * {@code GeneralRequest}. Typically, anonymous instances of private nested classes implementing
 * this interface are used.
 */
interface RequestResponder
{
    /**
     * A function which handles the response received from a request.
     *
     * @param response a String response to be handled
     */
    void onResponse(String response);
}

/**
 * A singleton owned and maintained by {@code LoginActivity} which oversees the lone Volley request
 * queue used by AtLAS. It is used to make POST and GET requests.
 *
 * @see LoginActivity
 */
public class GeneralRequest
{

    private RequestQueue mRequestQueue;
    final private String mBaseURL = "http://104.237.135.184/AtLAS/";

    /**
     * Constructor for {@code GeneralRequest}.
     *
     * @param applicationContext context of the owning activity
     */
    GeneralRequest(Context applicationContext)
    {
        mRequestQueue = Volley.newRequestQueue(applicationContext);
    }

    /**
     * Performs a POST request and handles the response according to the {@code RequestResponder}
     * provided.
     *
     * @param urlSuffix the String URL endpoint to be appended to the base URL
     * @param parameterBody a map containing the names and values of all data fields in the request
     * @param responder a RequestResponder which handles the response from the POST request
     *
     * @see RequestResponder
     */
    public void POSTRequest(final String urlSuffix, final Map<String, String> parameterBody, final RequestResponder responder)
    {

        class POSTTask extends AsyncTask<Void, Void, Boolean>
        {

            private final Map<String, String> mParameterBody;

            private POSTTask(final Map<String, String> parameterBody)
            {
                mParameterBody = parameterBody;
            }

            @Override
            protected Boolean doInBackground(Void... params)
            {
                String url = mBaseURL + urlSuffix;
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
										{
                        VolleyLog.d("Response: %s", response);
                        responder.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
										{
                        VolleyLog.e(error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String,String> getParams()
                    {
                        return parameterBody;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String,String> params = new HashMap<>();
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

    /**
     * Performs a GET request and handles the response according to the {@code RequestResponder}
     * provided.
     *
     * @param urlSuffix the String URL endpoint to be appended to the base URL
     * @param parameterBody a map containing the names and values of all data fields in the request
     * @param responder a RequestResponder which handles the response from the GET request
     *
     * @see RequestResponder
     */
    public void GETRequest(final String urlSuffix, final Map<String, String> parameterBody, final RequestResponder responder)
    {

        class GETTask extends AsyncTask<Void, Void, Boolean>
        {

            private final Map<String, String> mParameterBody;

            private GETTask(final Map<String, String> parameterBody)
            {
                mParameterBody = parameterBody;
            }

            @Override
            protected Boolean doInBackground(Void... params)
            {
                String url = mBaseURL + urlSuffix;
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        VolleyLog.d("Response: %s", response);
                        responder.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.e(error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String,String> getParams()
                    {
                        return parameterBody;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String,String> params = new HashMap<>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };

                mRequestQueue.add(request);
                return true;
            }
        }

        GETTask task = new GETTask(parameterBody);
        task.execute((Void) null);
    }
}

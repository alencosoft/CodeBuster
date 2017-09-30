package com.android.codebreaker.data;

/**
 * <p>A helper class that sets up the query for the purpose of verifying a user ("accounts"
 * table).</p>
 *
 * <p>Notice that the base class will take the listener from the activity. In that way, we'll
 * bypass this class completely when the query results come back.</p>
 */

import android.content.Context;
import android.util.Log;

import com.android.codebreaker.R;

import java.net.URLEncoder;

public class VerifyCredentialsDataRequest extends DataRequestHelper
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The context of the calling activity
    Context mContext;

    /**
     * The data request constructor. Takes the listener class object (ie; AuthenticatorActivity)
     * and the context of the same class.
     *
     * @param listener - Needed to communicate with the calling class with the query results.
     * @param context - Needed to access resources (strings) to build the HTTP request url.
     */
    public VerifyCredentialsDataRequest(IDataRequestCallback listener, Context context)
    {
        Log.d(TAG, "Constructor");

        // Set the context so we can build the HTTP request URL using resources (strings)
        mContext = context;

        // Set's the calling class as the listener to the base class.
        super.setListener(listener);
    }

    /**
     * Builds and then executes the query. The final HTTP request is in the form of URL and query
     * string. The base class puts them together.
     *
     * @param userData - Contains the user's "Username" and "Password" needed to authenticate.
     */
    public void execute(UserData userData)
    {
        Log.d(TAG, "execute()");

        try
        {
            // We need the "Username" and "Password". So, if we don't have them there's no sense
            // in trying to authenticate.
            if (userData.getUsername().length() == 0 ||
                    userData.getPassword().length() == 0)
            {
                Log.e(TAG, "execute(): ERROR: Username or Password were empty!");
                return;
            }

            // All links are stored in resources (strings).
            String link =
                    mContext.getResources().getString(R.string.url_header) +
                            mContext.getResources().getString(R.string.url_tail_verify_credentials);

            Log.i(TAG, "execute(): link:" + link);

            // Setup the query string...
            String queryString = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(userData.getUsername(), "UTF-8");
            queryString += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                    URLEncoder.encode(userData.getPassword(), "UTF-8");

            Log.i(TAG, "execute(): queryString:" + queryString);

            // Set the URL and Query String in the base class.
            super.setParams(link, queryString);

            // Finally, call the AsyncTask "execute" method and wait for the response.
            super.execute("");
        }

        // We had a problem with the query. Log it.
        catch(Exception e)
        {
            Log.e(TAG + ": Exception", e.getMessage());
        }
    }
}
package com.android.codebreaker.data;

/**
 * <p>A helper class that sets up the query that saves the user's score on the server.</p>
 *
 * <p>Notice that the base class will take the listener from the activity. In that way, we'll
 * bypass this class completely when the query results come back.</p>
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.android.codebreaker.R;

import java.net.URLEncoder;

public class SaveScoreDataRequest extends DataRequestHelper
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
    public SaveScoreDataRequest(IDataRequestCallback listener, Context context)
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
     * @param userData - Contains user's data needed for the query (ie; score, turns...).
     */
    public void execute(UserData userData)
    {
        Log.d(TAG, "execute()");

        try
        {
            // We need the several pieces of data. If we don't have them, then throw an error
            // back to the listener.
            if (userData.getUsername().length() == 0 ||
                    userData.getSecretNumber().length() == 0 ||
                    Integer.toString(userData.getTurns()).length() == 0 ||
                    Integer.toString(userData.getTimeInSeconds()).length() == 0 ||
                    Integer.toString(userData.getScore()).length() == 0 ||
                    Integer.toString(userData.getAuthToken()).length() == 0)
            {

                Log.e(TAG, "execute().ERROR: " +
                        Resources.getSystem().getString(R.string.query_params_missing));
                return;
            }

            // All links are stored in resources (strings).
            String link =
                    mContext.getResources().getString(R.string.url_header) +
                            mContext.getResources().getString(R.string.url_tail_submit_scores);

            Log.i(TAG, "execute().link:" + link);

            // Setup the query string...
            String queryString = URLEncoder.encode("account_id", "UTF-8") + "=" +
                    URLEncoder.encode(Integer.toString(userData.getAuthToken()), "UTF-8");
            queryString += "&" + URLEncoder.encode("secret_number", "UTF-8") + "=" +
                    URLEncoder.encode(userData.getSecretNumber(), "UTF-8");
            queryString += "&" + URLEncoder.encode("turns", "UTF-8") + "=" +
                    URLEncoder.encode(Integer.toString(userData.getTurns()), "UTF-8");
            queryString += "&" + URLEncoder.encode("time_in_seconds", "UTF-8") + "=" +
                    URLEncoder.encode(Integer.toString(userData.getTimeInSeconds()), "UTF-8");
            queryString += "&" + URLEncoder.encode("score", "UTF-8") + "=" +
                    URLEncoder.encode(Integer.toString(userData.getScore()), "UTF-8");

            Log.i(TAG, "execute().queryString:" + queryString);

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
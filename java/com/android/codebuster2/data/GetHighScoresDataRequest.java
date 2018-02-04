package com.android.codebuster2.data;

/**
 * <p>A helper class that sets up the query for high scores.</p>
 *
 * <p>Notice that the base class will take the listener from the activity. In that way, we'll
 * bypass this class completely when the query results come back.</p>
 */

import android.content.Context;
import android.util.Log;

import com.android.codebuster2.R;

public class GetHighScoresDataRequest extends DataRequestHelper
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
    public GetHighScoresDataRequest(IDataRequestCallback listener, Context context)
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
     */
    public void execute()
    {
        Log.d(TAG, "execute()");

        try
        {
            // All links are stored in resources (strings).
            String link =
                    mContext.getResources().getString(R.string.url_header) +
                            mContext.getResources().getString(R.string.url_tail_get_high_scores);

            Log.d(TAG, "execute(): link:" + link);

            // The base class takes two params to submit a request: URL and query string. In this
            // case there isn't a query string needed (ie; no database params for filtering).
            super.setParams(link, "");

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
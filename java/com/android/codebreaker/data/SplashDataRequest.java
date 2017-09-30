package com.android.codebreaker.data;

/**
 * <p>A helper class that sets up the query for the "misc" table which contains the
 *    "website link" and "game play instructions".</p>
 *
 * <p>Notice that the base class will take the listener from the activity. In that way, we'll
 * bypass this class completely when the query results come back.</p>
 */

import android.content.Context;
import android.util.Log;

import com.android.codebreaker.R;

public class SplashDataRequest extends DataRequestHelper
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The context of the calling activity
    Context mContext;

    /**
     * The data request constructor. Takes the listener class object (ie; SplashActivity) and the
     * context of the same class.
     *
     * @param listener - Needed to communicate with the calling class with the query results.
     * @param context - Needed to access resources (strings) to build the HTTP request url.
     */
    public SplashDataRequest(IDataRequestCallback listener, Context context)
    {
        Log.d(TAG, "Constructor");

        // Set the context so we can build the HTTP request URL using resources (strings)
        mContext = context;

        // Set's this class as the listener to the base class.
        super.setListener(listener);
    }

    /**
     * Builds and then executes the query. The final HTTP request is in the form of URL and query
     * string. The base class puts them together.
     *
     * Note: There isn't a Query String needed. (eg; No database filtering needed)
     */
    public void execute()
    {
        Log.d(TAG, "execute()");

        try
        {
            // All links are stored in resources (strings).
            String link =
                    mContext.getResources().getString(R.string.url_header) +
                    mContext.getResources().getString(R.string.url_tail_splash);

            Log.i(TAG, ".execute(): link:" + link);

            // The base class takes two params to submit a request: URL and query string. In this
            // case there isn't a query string needed (ie; no database params for filtering).
            super.setParams(link, "");

            // Finally, execute the query. See the base class "AsyncTask" for more on the
            // "execute" method.
            super.execute("");
        }

        // Something went wrong. We'll log it.
        catch(Exception e)
        {
            Log.e(TAG + ": Exception", e.getMessage());
        }
    }
}
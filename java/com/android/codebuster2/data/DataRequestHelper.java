package com.android.codebuster2.data;

/**
 * <p>A base class that is used to handle the mundane properties/tasks associated with all server
 * interaction (queries). </p>
 *
 * <p>All query sub-classes should extend this base class. The subclasses will also need to
 * implement the IDataRequestCallback interface. Simply call setListener with the subclass "this"
 * as a param and all resultant communication will be easily accessible. </p>
 *
 * <p>To work through this base class the following steps should be taken: </p>
 *
 * <ul>
 *     <li>Extend this class;</li>
 *     <li>Implement "IDataRequestCallback" (two callback methods);</li>
 *     <li>Call "setListener" with the subclass' "this" property;</li>
 *     <li>Call "setParams" with the URL and Query String BEFORE calling the AsyncTasks
 *         "execute" method;</li>
 *     <li>Call the AsyncTasks "execute" method;</li>
 *     <li>Handle the "IDataRequestCallback" methods with the results of the query.</li>
 * </ul>
 */

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.android.codebuster2.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

public class DataRequestHelper extends AsyncTask<String, Integer, String>
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The array that holds the URL and Query String.
    private String[] mParams;

    // The listener with the two callback methods that will handle the query result.
    private IDataRequestCallback mListener;

    /**
     * The "listener" here should be the subclass.
     *
     * @param listener
     */
    protected void setListener(IDataRequestCallback listener)
    {
        Log.d(TAG, "setListener()");

        mListener = listener;
    }

    /**
     * Set the URL and Query String for communicating with the server.
     *
     * @param link
     * @param data
     */
    public void setParams(String link, String data)
    {
        Log.d(TAG, "setParams(): Link and Query String.");

        mParams = new String[] {link, data};
    }

    /**
     * Overrides the "AsyncTask" base class method of the same name to set up the:
     *
     * <ul>
     *     <li>Server connection object and its' properties;</li>
     *     <li>URL to include base URL and Query String;</li>
     *     <li>Reader that takes in the query result;</li>
     * </ul>
     *
     * Note: This method handles all the gory details of establishing and destroying the connection
     *       object and its' properties.
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(String... params)
    {
        Log.d(TAG, "doInBackground()");

        // Instantiate the connection object to the server:
        HttpURLConnection connection = null;

        // Instantiate and populate the Query String (2nd param):
        String postData = mParams[1];

        // Try to query the server. If successful read the server response one line at a time.
        try
        {
            // Instantiate and populate the URL (1st param):
            URL url = new URL(mParams[0]);

            Log.d(TAG, "URL: " + url);

            // Connection properties
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(postData.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write(postData.getBytes("UTF-8"));
            connection.getInputStream();

            // Read the response:
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            // Append the new line "\n" character to the end of each line.
            try
            {
                // Read Server Response
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
            }

            // We're done with the reader so close it and set it up for GC.
            finally
            {
                reader.close();
            }

            // Return the resulting response as a string.
            return stringBuilder.toString();
        }

        // We had a problem somewhere. Log the error and call the listener's "onQueryTaskError"
        // callback method with the error. There could be a multitude of possible errors. The most
        // likely error is a timeout due to the device being disconnected from the internet.
        catch (Exception e)
        {
            Log.e(TAG, "ERROR: " + e.getMessage());
            mListener.onQueryTaskError(e.getMessage());
        }

        // Disconnect the server connection object:
        finally
        {
            if (connection != null)
            {
                Log.i(TAG, "Disconnecting the server connection.");
                connection.disconnect();
            }
        }

        // We had a problem so just return null.
        return null;
    }

    /**
     * We received the result back so handle it.
     *
     * @param result
     */
    @Override
    protected void onPostExecute(String result)
    {
        Log.d(TAG, "Overriding the onPostExecute() method.");

        // Empty result is an automatic error. Log it and call the subclass' "onQueryTaskError"
        // method with the error message.
        if (result == null || result.isEmpty())
        {
            Log.e(TAG, Resources.getSystem().getString(R.string.result_empty));
            mListener.onQueryTaskError(Resources.getSystem().getString(R.string.result_empty));
            return;
        }

        // Yay! We were successful with our query. Call the subclass' "onQueryTaskCompleted"
        // method with the results.
        Log.i(TAG, "onPostExecute(): Query result came back.");

        mListener.onQueryTaskCompleted(result);
    }
}

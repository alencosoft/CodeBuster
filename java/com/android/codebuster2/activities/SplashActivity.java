package com.android.codebuster2.activities;

/**
 * <p>"Splash Screen": The first screen presented to the user. Only one interesting piece here: If
 * there isn't an internet connection or the query for data comes back null/empty, than the
 * instructions populate via canned data in res/values/strings.</p>
 *
 * <p>Includes (top to bottom):</p>
 *
 * <ul>
 *     <li> Website link (ie; GreggsCoolApps.com);</li>
 *     <li> The "Code Buster" logo;</li>
 *     <li> "Begin" button;</li>
 *     <li> "Game Play" instructions.</li>
 * </ul>
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.codebuster2.R;
import com.android.codebuster2.data.IDataRequestCallback;
import com.android.codebuster2.data.SplashDataRequest;
import com.android.codebuster2.misc.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends Activity implements IDataRequestCallback
{
    // Used in JSon parsing... (ie; Removing "magic" strings)
    private final String JSON_MAIN_NODE = "misc";
    private final String JSON_CHILD_NODE_NAME = "name";
    private final String JSON_CHILD_NODE_VALUE = "value";
    private final String DATABASE_FIELD_WEBSITE_LINK_TEXT = "website_link_text";
    private final String DATABASE_FIELD_INSTRUCTIONS = "instructions";

    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The "Game Play" or Instructions scrolling text field. We access this in a few places
    // so I decided to go ahead and just create the pointer to the object once:
    private TextView mTxtGamePlay;

    /**
     * <p>Setup the screen and fill it with components. Once that is accomplished, start the "misc"
     * query for website link and instructions.</p>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // "Game Play" Instructions
        mTxtGamePlay = (TextView) findViewById(R.id.txtGamePlay);
        mTxtGamePlay.setMovementMethod(new ScrollingMovementMethod());

        // If user is disconnected from the internet then we don't want to bother with
        // querying for data:
        if ( ! Utilities.isUserConnectedToInternet(getApplicationContext()))
        {
            onQueryTaskError(getString(R.string.no_internet_connection_error));
            return;
        }

        // Start the "misc" query for website link and instructions.
        queryForMiscData();
    }

    /**
     * <p>Get instructions and website link from server database table...</p>
     */
    private void queryForMiscData()
    {
        // Try to execute the query. Note: If there is an exception thrown, then it's a code
        // problem (probably in "SplashDataRequest").
        try
        {
            SplashDataRequest splashDataRequest =
                    new SplashDataRequest(this, getApplicationContext());
            splashDataRequest.execute();
        }

        // We should never get here.
        catch(Exception e)
        {
            Log.e("Exception", "" + e.getMessage());
        }
    }

    /**
     * <p>The method name says it all: "Begin the Game"...</p>
     *
     * <p>I'm not a big fan of the button's res->layout "onClick" property. It does make it easier
     * than including a listener in the "onCreate" method. However, it's not readily apparent
     * in the activity code how these callbacks work (ie; where does the listener code live?). But,
     * for simplicity sake it's quick and easy to implement and I can't justify the excessive code
     * to create the listener. So, just look in res->layout->activity_splash for the button's
     * "onClick" property.</p>
     *
     * @param view
     */
    public void onBeginGameButtonClickCallback(View view)
    {
        Log.d(TAG, "onBeginGameButtonClickCallback()");

        // Create the "MainActivity" intent and start it.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * ###################################################################################
     * <p>Callbacks from SplashDataRequest's base class "DataRequestHelper". These callbacks
     * are required for the "IDataRequestCallback" interface.</p>
     * ###################################################################################
     */

    /**
     * <p>We received a result back from the server, Yay! Several steps need to be accomplished:</p>
     *
     * <ul>
     *     <li>First a sanity check for a non-empty result;</li>
     *     <li>Attempt to parse the result in JSon format;</li>
     *     <li>Populate the "website link" and "instructions" text fields if above tests pass.</li>
     * </ul>
     *
     * @param result
     */
    public void onQueryTaskCompleted(String result)
    {
        Log.d(TAG, "onQueryTaskCompleted().result: " + result);

        String instructions = "";

        // Parse the JSon "result" using the parent node "misc":
        try
        {
            JSONObject jsonResponse = new JSONObject(result);
            JSONArray jsonMainNode = jsonResponse.optJSONArray(JSON_MAIN_NODE);

            // Walk each child node:
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                String outputName = jsonChildNode.optString(JSON_CHILD_NODE_NAME);
                String outputValue = jsonChildNode.optString(JSON_CHILD_NODE_VALUE);

                Log.d(TAG, "onQueryTaskCompleted(): outputName: " + outputName);
                Log.d(TAG, "onQueryTaskCompleted(): outputValue: " + outputValue);

                // Only two possible name/value pairs (Website link or Instructions):
                switch (outputName)
                {
                    case DATABASE_FIELD_WEBSITE_LINK_TEXT:

                        TextView txtLink = (TextView) findViewById(R.id.txtLink);
                        txtLink.setText(Html.fromHtml(outputValue));

                        break;

                    case DATABASE_FIELD_INSTRUCTIONS:

                        instructions = outputValue;

                        break;
                }
            }
        }

        // This should never happen but if it does we'll log it.
        catch(JSONException e)
        {
            Log.d(TAG, "onQueryTaskCompleted(): Error (JSONException): " + e.toString());
        }

        // Appears we had a connection problem so use baked-in instructions instead.
        if (instructions.isEmpty())
        {
            Log.d(TAG, "onQueryTaskCompleted(): instructions.isEmpty()");

            // Canned instructions:
            instructions = getString(R.string.splash_activity_game_play_body);
        }

        // Finally, fill in the instructions text:
        mTxtGamePlay.setText(Html.fromHtml(instructions));
    }

    /**
     * <p>Connection problem? Server down? Nuclear holocaust? We probably received an error while
     * trying to build/commit/execute the "misc" query. So, log the error and populate the
     * instructions using 'canned' data.</p>
     *
     * <p>Note: In case of an error and no data from the server, we WON'T display the website
     * link textfield.</p>
     *
     * @param errorString
     */
    public void onQueryTaskError(String errorString)
    {
        Log.d(TAG, "onQueryTaskError(): Error:" + errorString);

        // Use baked-in instructions and skip the web-site link.
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mTxtGamePlay.setText(
                        Html.fromHtml(getString(R.string.splash_activity_game_play_body)));
            }
        });
    }
}

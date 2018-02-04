package com.android.codebuster2.activities;

/**
 * <p>As the class name implies, this class shows all of the high scores for all users. The scores
 * are sorted by score in descending order with the current user's score highlighted and at
 * the top of the visual list. There is also a "PLAY AGAIN" button which takes the user back to
 * the main UI screen where the game resides.</p>
 */

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.codebuster2.data.GetHighScoresDataRequest;
import com.android.codebuster2.data.IDataRequestCallback;
import com.android.codebuster2.data.UserData;
import com.android.codebuster2.lists.GetScoresListAdapter;
import com.android.codebuster2.lists.GetScoresListRowData;
import com.android.codebuster2.R;
import com.android.codebuster2.misc.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class GetHighScoresActivity extends Activity implements IDataRequestCallback
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // A reference to the High Scores list:
    ListView mListView;

    // We need to know where the user's score is in the list. It's set here to "-1" which means
    // the user's score isn't in the list. This could happen if the user decides to skip
    // logging in.
    int mUserIndexInScoresList = -1;

    // The bridge between the Response list view and the data:
    GetScoresListAdapter mGetScoresListAdapter;

    // The data tied to the List Adapter by row:
    ArrayList<GetScoresListRowData> mData;

    // Where all of the user's data is stored like "score"...
    private UserData mUserData;

    /**
     * <p>Initializes the screen with the visual components.</p>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_scores);

        mListView = (ListView) findViewById(R.id.list);
        mListView.setItemsCanFocus(true);

        // The data to build the list (from the "High Scores" query results):
        mData = new ArrayList<>();

        // Get the UserData object from the previous activity:
        if (savedInstanceState == null)
        {
            // Comes from "AuthenticatorActivity" and is a "one-stop shop" for all user data.
            mUserData =
                    (UserData) getIntent().getSerializableExtra(getString(R.string.KEY_USER_DATA));
        }

        // We need an internet connection to show the high scores list:
        if ( ! Utilities.isUserConnectedToInternet(getApplicationContext()))
        {
            // Log the disconnected state:
            Log.d(TAG, "onCreate(): " + getString(R.string.no_internet_connection_error));

            // Cancel the remaining work:
            return;
        }

        queryForHighScoresData();
    }

    /**
     * <p>The user has returned to the game.</p>
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    /**
     * <p>Get the High Scores list from server database table...</p>
     */
    private void queryForHighScoresData()
    {
        // Try to execute the query. Note: If there is an exception thrown, then it's a code
        // problem (probably in "GetHighScoresDataRequest").
        try
        {
            GetHighScoresDataRequest getHighScoresDataRequest =
                    new GetHighScoresDataRequest(this, getApplicationContext());
            getHighScoresDataRequest.execute();
        }

        // We should never get here.
        catch(Exception e)
        {
            Log.e("Exception", "" + e.getMessage());
        }
    }

    /**
     * <p>To get here = we received data and the visual UI components have been initialized.
     * This method merges the data with the ListView and sets some of the list's properties.</p>
     */
    public void showTheScores()
    {
        // Initialize the list's adapter and set it on the ListView object:
        mGetScoresListAdapter =
                new GetScoresListAdapter(
                        this,
                        R.layout.listview_item_row_get_scores,
                        mData,
                        mUserIndexInScoresList);
        mListView.setAdapter(mGetScoresListAdapter);

        // Set the selected item in the list to the user's score:
        mListView.setSelection(mUserIndexInScoresList);

        // Puts the highlighted list item at the top of the visual list:
        mListView.setItemChecked(mUserIndexInScoresList, true);

        // Tell the adapter to recalculate/redraw the ListView:
        mGetScoresListAdapter.notifyDataSetChanged();
    }

    /**
     * <p>Does the user want to play again? If so, transition back to "MainActivity".</p>
     *
     * @param v
     */
    public void playAgainButtonClick(View v)
    {
        // Create the intent and pass variables
        Intent intent = new Intent(this, MainActivity.class);

        // Start the new activity
        startActivity(intent);
    }

    /**
     * ###################################################################################
     * <p>Callbacks from GetHighScoreshDataRequest's base class "DataRequestHelper". These
     * callbacks are required for the "IDataRequestCallback" interface.</p>
     * ###################################################################################
     */

    /**
     * <p>We received a result back from the server, Yay! Several steps need to be accomplished:</p>
     *
     * <ul>
     *     <li>First a sanity check for a non-empty result;</li>
     *     <li>Attempt to parse the result in JSon format;</li>
     *     <li>Populate the High Scores list if above tests pass.</li>
     * </ul>
     *
     * @param result
     */
    public void onQueryTaskCompleted(String result)
    {
        // Oops, no data! This is bad.
        if (result.isEmpty() || result == null)
        {
            Log.d(TAG, "onQueryTaskCompleted(): " + getString(R.string.result_empty));
            return;
        }

        Log.d(TAG, "onQueryTaskCompleted().result: " + result);

        // Parse the JSon "result" using the parent node "scores":
        try
        {
            JSONObject jsonResponse = new JSONObject(result);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("scores");

            // Walk the array of scores and add the following to the list's data object:
            //     1) The current count (combines the iterator "i" and the length of the array);
            //     2) The "username";
            //     3) The formatted score.
            for(int i = 0; i < jsonMainNode.length(); i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                // The "username":
                String outputUsername = jsonChildNode.optString("username");

                // The formatted score:
                String score = jsonChildNode.optString("score");
                String formattedScore = NumberFormat.getNumberInstance(Locale.US).format(
                        Integer.parseInt(score));

                // Add it to the list's data object. Note: The number counts back from
                // array.length to "1" combining the JSon child nodes and the iterator. Thus, the
                // list is essentially in descending order:
                mData.add(0, new GetScoresListRowData((jsonMainNode.length() - i) /* 1 */,
                        outputUsername /* 2 */, formattedScore /* 3 */));

                // Find the user in the list
                if (outputUsername.equals(mUserData.getUsername()) &&
                        score.equals(Integer.toString(mUserData.getScore())))
                {
                    mUserIndexInScoresList = jsonMainNode.length() - i - 1;
                }
            }
        }
        catch(JSONException e)
        {
            Toast.makeText(
                    getApplicationContext(),
                    "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        // Everything's ready, so show the list:
        showTheScores();
    }

    /**
     * <p>Connection problem? Server down? Nuclear holocaust? We received an error while trying
     * to build/commit/execute the "getHighScores" query. So, log the error.</p>
     *
     * @param errorString
     */
    public void onQueryTaskError(String errorString)
    {
        Log.d(TAG, "onQueryTaskError(): Error:" + errorString);
    }
}

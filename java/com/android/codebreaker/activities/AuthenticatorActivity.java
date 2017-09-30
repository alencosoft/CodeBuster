package com.android.codebreaker.activities;

/**
 * <p>[Birds Eye View]</p>
 *
 * <p>Accomplishes two main objectives:</p>
 *
 * <ul>
 *     <li>Authenticates the user;</li>
 *     <li>Stores the user's score;</li>
 * </ul>
 *
 * <p>[Details]</p>
 *
 * <p>There is a state machine that guides the code through the various stages of
 * authentication and database interaction. The stages (states) are:</p>
 *
 * <ul>
 *     <li>Try to get the user's Account info from the devices Account Manager;</li>
 *     <li>Verify the user's Account info against the database;</li>
 *     <li>Store new user Account info in the database (if a new user);</li>
 *     <li>Store the user's score in the database;</li>
 * </ul>
 *
 * <p>Each one of these "states" is represented visually on the screen in the form of a list.
 * The user can see what's happening and receives immediate feedback that the process was either
 * successful or unsuccessful.</p>
 *
 * <p>Note: The visual ListView is a "roll-your-own" adapter/data model. This was a test to see
 *          just how difficult it is to control/populate a ListView without the Android
 *          framework help. Turns out, it's not that tough! :)</p>
 *
 * <p>Storing the user's score is trivial so the bulk of this next section will relate only to User
 * Authentication.</p>
 *
 * <p>User Authentication: There are two places the user's account information is stored:</p>
 *
 * <ul>
 *     <li>On the device (ie; Account Manager);</li>
 *     <li>In the server-side database table "accounts".</li>
 * </ul>
 *
 * <p>When a new user goes through this authentication process he/she is prompted to enter
 * a username/password. In this dialogue box (see auth/AuthenticatorLoginDialogFragment) is a
 * "Remember me" checkbox, as well. The default state of this checkbox is "checked". During the
 * process of storing the user's credentials a new Account is added to the device using the
 * Android Account Manager. This will allow the user to skip the login process on consecutive
 * game plays.</p>
 *
 * <p>There are several edge cases that this code handles while adding a new user concerning
 * Authentication. Some of them are:</p>
 *
 * <ul>
 *     <li>Username and/or Password not entered;</li>
 *     <li>Username correct but Password incorrect;</li>
 *     <li>Username and/or Password don't match required length (8-16 chars).</li>
 * </ul>
 *
 * <p>NOTE: If a user types in an incorrect username, a new account is created using the
 * incorrectly entered username. This is expected behavior and the responsibility for entering
 * correct information is on the user.</p>
 *
 * <p>Database Interaction: All of the database queries are handled in separate (specific)
 * Data Request classes (see /data). These classes build the URL's and query strings that
 * communicate with specific .php files on the server. Each of the Data Request classes extend
 * a base class (see /data/DataRequestHelper). This "helper" class handles the connection object
 * and its' properties, executing the queries, and getting the results to the proper callbacks.</p>
 */

import android.accounts.Account;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.codebreaker.R;
import com.android.codebreaker.auth.Authenticator;
import com.android.codebreaker.auth.AuthenticatorLoginDialogFragment;
import com.android.codebreaker.data.IDataRequestCallback;
import com.android.codebreaker.data.SaveScoreDataRequest;
import com.android.codebreaker.data.UserData;
import com.android.codebreaker.data.VerifyCredentialsDataRequest;
import com.android.codebreaker.misc.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticatorActivity extends FragmentActivity
        implements AuthenticatorLoginDialogFragment.AuthenticatorLoginDialogListener,
        IDataRequestCallback
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // State machine states for the Authentication process:
    private final int STATE_GET_ACCOUNT_INFO = 0;   // From Account Manager
    private final int STATE_VERIFY_CREDENTIALS = 1; // Verify account against db
    private final int STATE_STORE_CREDENTIALS = 2;  // Store account in devices Account Manager and db
    private final int STATE_STORE_SCORE = 3;        // Store the user's score in db

    // Available Images for the AuthListRows object:
    private final int IMAGE_PLEASE_WAIT = 0;
    private final int IMAGE_CHECKMARK = 1;
    private final int IMAGE_X = 2;

    // Keeps track of the "state" of the Authentication process.
    private int mCurrentState = 0;

    // Should data storage be skipped? (ie; The user selected the "Not now" button when prompted
    // to login.)
    private boolean mShouldSkipDataStorage = false;

    // A place to store each "AuthListRow" LinearLayout references:
    private LinearLayout[] mAuthListRows = new LinearLayout[5];

    // A reference to "the brains" of the device's Account Manager Authentication process. See
    // "Authenticator.java" for details.
    private Authenticator mAuthenticator;

    // Where all of the user's data is stored like "score"...
    private UserData mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Used throughout this process for verification and data retrieval.
        mAuthenticator = new Authenticator(getBaseContext());

        // Comes from "MainActivity" and is a "one-stop shop" for all user data.
        mUserData = (UserData) getIntent().getSerializableExtra(getString(R.string.KEY_USER_DATA));

        // This view includes the Logo, "Congratulations" text, and the Authentication state's
        // visual feedback to the user (steps).
        setContentView(R.layout.activity_authentication);

        // We need an internet connection to authenticate the user:
        if ( ! Utilities.isUserConnectedToInternet(getApplicationContext()))
        {
            // Log the disconnected state:
            Log.d(TAG, "onCreate(): " + getString(R.string.no_internet_connection_error));

            // Cancel all queries:
            mShouldSkipDataStorage = true;
        }

        // Everything is now setup so let's begin the Authentication process:
        // Change the state to "STATE_GET_ACCOUNT_INFO".
        changeState(STATE_GET_ACCOUNT_INFO);
    }

    /**
     * <p>Changes the state of the Authentication process. Most of the state changes are forward
     * moving.</p>
     *
     * @param stateToChangeTo
     */
    private void changeState(int stateToChangeTo)
    {
        // Store the new state
        mCurrentState = stateToChangeTo;

        // A simple way to handle each state:
        switch (mCurrentState)
        {
            // State ONE: "Get account info"
            case STATE_GET_ACCOUNT_INFO:

                Log.d(TAG, "changeState(): STATE_GET_ACCOUNT_INFO");

                // Create the visible row on screen (eg; "Getting account info")
                mAuthListRows[mCurrentState] = createAuthenticationStepRow(
                        getString(R.string.state_get_account_info_description));

                // Declare and find the user's account in AccountManager by going through the
                // "Authenticator" utility class:
                Account account = mAuthenticator.findAccount();

                // If an account exists then use it and move to Account Verification state:
                if (account != null)
                {
                    Log.d(TAG, "changeState().STATE_GET_ACCOUNT_INFO: ACCOUNT EXISTS");

                    // Store the account info in the User's data object for future states:
                    storeAccountInUserData(
                            account.name,
                            mAuthenticator.getPasswordFromAccount(account),
                            Integer.parseInt(mAuthenticator.getAuthTokenFromAccount(account)),
                            true);
                }

                // Internet disconnected state: d'oh!
                else if (mShouldSkipDataStorage)
                {
                    changeState(STATE_VERIFY_CREDENTIALS);
                    break;
                }

                // If an account doesn't exist, then the user will have to create one:
                else
                {
                    Log.d(TAG, ".changeState().STATE_GET_ACCOUNT_INFO: ACCOUNT DOES NOT EXIST");
                    showAuthenticatorLoginDialog();
                }

                break;

            // State TWO: "Verify user's account info"
            case STATE_VERIFY_CREDENTIALS:

                Log.d(TAG, "changeState(): STATE_VERIFY_CREDENTIALS");

                // Create the visible row on screen (eg; "Verifying account")
                mAuthListRows[mCurrentState] = createAuthenticationStepRow(
                        getString(R.string.state_verifying_account));

                // User wants to skip this. So, skip everything else and move to next state:
                // STATE_STORE_CREDENTIALS
                if (mShouldSkipDataStorage)
                {
                    Log.i(TAG,"Skipping...");

                    changeState(STATE_STORE_CREDENTIALS);
                    break;
                }

                // The username/password check against what we have in the server database:
                VerifyCredentialsDataRequest verifyCredentialsDataRequest =
                        new VerifyCredentialsDataRequest(this, getApplicationContext());
                verifyCredentialsDataRequest.execute(mUserData);

                break;

            // State THREE: "Store the user's account information".
            // Note: Data has already been stored during the verification process. This state is
            // only intended as a visual queue that their account info has been stored.
            case STATE_STORE_CREDENTIALS:

                Log.d(TAG, "changeState(): STATE_STORE_CREDENTIALS");

                // Create the visible row on screen (eg; "Storing account")
                mAuthListRows[mCurrentState] = createAuthenticationStepRow(
                        getString(R.string.state_storing_account));

                // Change the list row image to a checkmark or "X" for "Storing account"
                // and set the "Skipping" text visibility.
                if (mShouldSkipDataStorage)
                {
                    Log.i(TAG,"Skipping...");

                    setRowImage(mCurrentState, IMAGE_X);
                    setSkippingVisibility(mCurrentState, View.VISIBLE);
                }
                else
                {
                    setRowImage(mCurrentState, IMAGE_CHECKMARK);
                    setSkippingVisibility(mCurrentState, View.INVISIBLE);
                }

                // Moving on...
                changeState(STATE_STORE_SCORE);

                break;

            // State FOUR: "Store the user's score in the database"
            case STATE_STORE_SCORE:

                Log.d(TAG, "changeState(): STATE_STORE_SCORE");

                // Create the visible row on screen (eg; "Storing score")
                mAuthListRows[mCurrentState] = createAuthenticationStepRow(
                        getString(R.string.state_storing_score));

                // Does the user want to skip this step? If so, show that this is happening
                // visually in the list and eventually transition to the "GetHighScores" activity.
                if (mShouldSkipDataStorage)
                {
                    Log.i(TAG,"Skipping...");

                    setRowImage(mCurrentState, IMAGE_X);
                    setSkippingVisibility(mCurrentState, View.VISIBLE);
                    startTimerBeforeGoingToHighScoresActivity();
                    break;
                }

                // Query that inserts the new score:
                SaveScoreDataRequest saveScoreDataRequest =
                        new SaveScoreDataRequest(this, getApplicationContext());
                saveScoreDataRequest.execute(mUserData);

                break;
        }
    }

    /**
     * <p>Add a new row to the Authentication state list. The list represents rows with the
     * following:</p>
     *
     * <ul>
     *     <li>Please wait indicator (image anim), check mark, or "X" (all 3 of these images
     *        share the same ImageView);</li>
     *     <li>Description TextView (ie; "Getting your account");</li>
     *     <li>"skipped" TextView: Displayed if the user chooses to skip an authentication
     *         step.</li>
     * </ul>
     *
     * @param descriptionText
     * @return The linear layout that contains the new row. It may be necessary to manipulate
     * the icon/image and "skipping" text after adding this new row.
     */
    private LinearLayout createAuthenticationStepRow(String descriptionText)
    {
        // Create the new row (a horizontal linear layout):
        LinearLayout childContainer = new LinearLayout(this);
        childContainer.setOrientation(LinearLayout.HORIZONTAL);

        // Please wait indicator, check mark, or "X" (all 3 of these images share the
        // same ImageView). For now, we set this image to the "Please wait" version:
        final ImageView img = new ImageView(this);
        img.setBackgroundResource(R.drawable.please_wait_animation);
        img.setId(R.id.authentication_row_image_id);

        // Get the background, which has been compiled to an AnimationDrawable object.
        final AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();

        // Start the animation (looped playback by default).
        frameAnimation.start();
        childContainer.addView(img);

        // Description TextView (ie; "Getting account"):
        TextView description = new TextView(this);
        description.setText(descriptionText);
        description.setTextSize(18);
        description.setPadding(35, 0, 35, 0);
        childContainer.addView(description);

        // "skipped" TextView: Used if the user chooses to skip some of the authentication steps
        // like logging in.
        TextView skipping = new TextView(this);
        skipping.setText(R.string.authentication_skipping);
        skipping.setTextColor(Color.RED);
        skipping.setTextSize(18);
        skipping.setId(R.id.authentication_row_skipping_id);
        skipping.setVisibility(View.INVISIBLE);

        // Should the "skipped" text be visible?
        // Note: We never "skip" the "Get High Scores" state.
        if (mShouldSkipDataStorage)
        {
            skipping.setVisibility(View.VISIBLE);
            img.setBackgroundResource(R.drawable.auth_row_state_failure);
        }

        // Add "skipped" TextView to it's container even if it's invisible
        childContainer.addView(skipping);

        // Add this new nested LinearLayout "child" to LinearLayout "parent" defined in XML
        // (eg; linearMain)
        LinearLayout parentContainer = (LinearLayout) findViewById(R.id.linearMain);
        parentContainer.addView(childContainer);

        // Return the childContainer to caller for later manipulation (if necessary):
        return childContainer;
    }

    /**
     * <p>Changes the image in a specific Authentication list row. There are three possible
     * images:</p>
     *
     * <ul>
     *     <li>Please wait animation = processing the state;</li>
     *     <li>Checkmark = successfully accomplished</li>
     *     <li>An "x" = unsuccessfully accomplished or "skipped"</li>
     * </ul>
     *
     * @param whichRow
     * @param whichImage
     */
    public void setRowImage(int whichRow, int whichImage)
    {
        // Get the correct row and image
        LinearLayout linearLayout = mAuthListRows[whichRow];
        ImageView img = (ImageView) linearLayout.findViewById(R.id.authentication_row_image_id);

        // Ok, change to the correct image please:
        switch(whichImage)
        {
            case IMAGE_PLEASE_WAIT:
                img.setBackgroundResource(R.drawable.auth_row_state_pending);
                break;

            case IMAGE_CHECKMARK:
                img.setBackgroundResource(R.drawable.auth_row_state_success);
                break;

            case IMAGE_X:
                img.setBackgroundResource(R.drawable.auth_row_state_failure);
                break;
        }
    }

    /**
     * <p>If the user chooses to skip storing their information on the database (eg; clicking
     * the "Not now" button during the login stage) then we want to skip all of the authentication
     * and database interaction phases except getting the high scores. We show this by making
     * the "skipping" textfield in the Authentication list row visible.</p>
     *
     * @param whichRow
     * @param visibility
     */
    public void setSkippingVisibility(int whichRow, int visibility)
    {
        // Get the correct row and textfield then set it's visibility to the passed param:
        LinearLayout linearLayout = mAuthListRows[whichRow];
        TextView skipping =
                (TextView) linearLayout.findViewById(R.id.authentication_row_skipping_id);
        skipping.setVisibility(visibility);
    }

    /**
     * <p>Creates an instance of the "LOGIN" dialog fragment (popup window) and shows it.</p>
     */
    public void showAuthenticatorLoginDialog()
    {
        // Create a new instance of the "AuthenticatorLoginDialogFragment":
        AuthenticatorLoginDialogFragment dialog = new AuthenticatorLoginDialogFragment();

        // Show it:
        dialog.show(getFragmentManager(), "AuthenticatorLoginDialogFragment");

        // Populate the username if one exists in UserData. Note: The username may have been
        // stored in a previous login attempt. This would only happen if the user entered an
        // incorrect password.
        dialog.setUsername(mUserData.getUsername());
    }

    /**
     * ###################################################################
     * <p>"Login" Dialogue Callbacks</p>
     *
     * <p>The "Login" dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * (POSITIVE/NEGATIVE clicks) defined by the AuthenticatorLoginDialogFragment->
     * AuthenticatorLoginDialogListener interface.</p>
     * ###################################################################
     */

    /**
     * <p>"Login" dialog "POSITIVE" callback</p>
     *
     * @param username   User input TextField
     * @param password   User input TextField
     * @param rememberMe Checkbox in dialog window
     */
    @Override
    public void onLoginDialogPositiveClick(String username, String password, Boolean rememberMe)
    {
        Log.d(TAG, "onLoginDialogPositiveClick()");

        // Store the account info in the UserData object:
        storeAccountInUserData(username, password, -1 /* A temporary AuthToken */, rememberMe);
    }

    /**
     * <p>"Login" dialog "NEGATIVE" callback. This occurs when the user selects the "Not now"
     * button (eg; User chooses to skip logging in).</p>
     */
    @Override
    public void onLoginDialogNegativeClick()
    {
        Log.d(TAG, "onLoginDialogNegativeClick()");

        // Change the mShouldSkipDataStorage class variable to true, modify the
        // "Getting account info" image to an "X", and set the 'skipping' textfield to visible:
        mShouldSkipDataStorage = true;

        Log.d(TAG, "onLoginDialogNegativeClick(): Changing background image to 'X'");
        setRowImage(mCurrentState, IMAGE_X);

        Log.d(TAG, "onLoginDialogNegativeClick(): Setting 'skipping' textfield to visible");
        setSkippingVisibility(mCurrentState, View.VISIBLE);

        // Moving on...
        // Note: We still want to go to the "Verify credentials" state just to show the user
        //       the state will be skipped.
        Log.d(TAG, "onLoginDialogNegativeClick(): Changing state to STATE_VERIFY_CREDENTIALS");
        changeState(STATE_VERIFY_CREDENTIALS);
    }

    /**
     * <p>Store all of the below information in UserData. This data comes from the login
     * dialogue fragment.</p>
     *
     * @param username
     * @param password
     * @param authToken
     * @param rememberMe
     */
    private void storeAccountInUserData(
            String username,
            String password,
            int authToken,
            boolean rememberMe)
    {
        Log.d(TAG, "storeAccountInUserData()");

        // Store the data for use in future states.
        mUserData.setUsername(username);
        mUserData.setPassword(password);
        mUserData.setAuthToken(authToken);
        mUserData.setRememberMe(rememberMe);

        // Change the "please wait" anim to a "Check" image and set 'skipping' textfield to
        // invisible:
        Log.i(TAG, "storeAccountInUserData(): Changing background image to 'Check'");
        setRowImage(mCurrentState, IMAGE_CHECKMARK);

        Log.i(TAG, "storeAccountInUserData(): Setting 'skipping' textfield to invisible");
        setSkippingVisibility(mCurrentState, View.INVISIBLE);

        // Moving on...
        Log.i(TAG, "storeAccountInUserData(): Changing state to STATE_VERIFY_CREDENTIALS");
        changeState(STATE_VERIFY_CREDENTIALS);
    }

    /**
     * <p>Moving on to the "High Scores" activity...</p>
     */
    public void goToHighScoresActivity()
    {
        Log.d(TAG, "goToHighScoresActivity()");

        // Create the intent with the UserData object
        Intent getHighScoresIntent = new Intent(this, GetHighScoresActivity.class);
        getHighScoresIntent.putExtra(getString(R.string.KEY_USER_DATA), mUserData);

        // On to showing High Scores...
        startActivity(getHighScoresIntent);
    }

    /**
     * #################################################################
     * Query Task callbacks (see IDataRequestCallback)
     * #################################################################
     */

    /**
     * <p>Error handling when things go wrong during the various queries.</p>
     *
     * @param errorString
     */
    public void onQueryTaskError(String errorString)
    {
        // First we'll log the error:
        Log.d(TAG, "onQueryTaskError().Error: " + errorString);

        // We need to handle this error if we're in the "Verify Credentials" state:
        if (mCurrentState == STATE_VERIFY_CREDENTIALS)
        {
            onTaskErrorDuringVerifyCredentials(errorString);
        }
    }

    /**
     * <p>Use the state machine to determine which query result has returned then call state-
     * specific methods to handle the results.</p>
     *
     * <p>Note: The current state in the state machine will direct flow.</p>
     *
     * @param result The database (JSon) response:
     */
    public void onQueryTaskCompleted(String result)
    {
        // Log the result
        Log.d(TAG, "onQueryTaskCompleted().result: " + result);

        // These are the only states that query for data:
        switch (mCurrentState)
        {
            case STATE_VERIFY_CREDENTIALS:

                onVerifyCredentialsTaskCompleted(result);
                break;

            case STATE_STORE_SCORE:

                onSaveScoreTaskCompleted(result);
                break;
        }
    }

    /**
     * #################################################################
     * State specific calls based on query results
     * #################################################################
     */

    /**
     * <p>This is a very specific case that needs to be handled during the verification process.
     * </p>
     *
     * <p>Need to remove user's account info and ask for credentials. The only possibility
     * for this failure is if the user entered a valid username but an invalid password.</p>
     *
     * <p>Note: If the user enters an invalid username than a new Account is created and the
     * code flow would never get here.</p>
     *
     * @param errorMessage
     */
    public void onTaskErrorDuringVerifyCredentials(String errorMessage)
    {
        Log.d(TAG, "onTaskErrorDuringVerifyCredentials().errorMessage: " + errorMessage);

        // Let's be sure we're in the correct state (sanity check):
        if (mCurrentState == STATE_VERIFY_CREDENTIALS)
        {
            // ###############################################
            // Start the Authentication process over
            // ###############################################

            //     STEP 1: Remove the current users account on the device if one exists:
            Account account = mAuthenticator.findAccount();

            if (account != null)
            {
                Log.d(TAG, "onQueryTaskError().removeAccount()");
                mAuthenticator.removeAccount(account);
            }

            //     STEP 2: Clear "password" from user's data (UserData):
            mUserData.setPassword("");

            //     STEP 3: Message the user that the login failed:
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.login_failed_password),
                    Toast.LENGTH_SHORT).show();

            //     STEP 4: Remove all the rows in the visible authentication steps:
            LinearLayout parentContainer = (LinearLayout) findViewById(R.id.linearMain);
            parentContainer.removeAllViews();

            //     STEP 5: Go to initial State (ie; "Getting account info") and start the
            //             Authentication process over:
            changeState(STATE_GET_ACCOUNT_INFO);
        }
    }

    /**
     * <p>The user verification query was a success! Well, at least we got a result back. There
     * are several possibilities here:</p>
     *
     * <ul>
     *     <li>The result was null; (Note: This should never happen)</li>
     *     <li>Three possible Errors:</li>
     *         <ul>
     *             <li>"Required fields were missing";</li>
     *             <li>"The password was invalid";</li>
     *             <li>"The INSERT statement failed". (Note: This should never happen)</li>
     *         </ul>
     *     <li>Success! Yay. This is the expected 'happy' flow.</li>
     * </ul>
     *
     * @param result
     */
    public void onVerifyCredentialsTaskCompleted(String result)
    {
        int intResult = 0;

        // Parse the JSon result.
        try
        {
            JSONObject jsonResponse = new JSONObject(result);
            intResult = jsonResponse.optInt("result");
        }

        // What happened here? This is bad. Something went wrong with the data. This is
        // probably not a JSon exception but rather a malformed response coming back from
        // the server. Either way, we need to log the problem.
        catch(JSONException e)
        {
            Log.e(TAG, "onVerifyCredentialsTaskCompleted().JSONException: " + e.getMessage());
            return;
        }

        // Something went wrong. This should either be a negative number (-1 to -3 = errors) or a
        // positive number representing success and the user's userId.
        if (intResult == 0)
        {
            Log.e(TAG, "onVerifyCredentialsTaskCompleted(): Invalid return of '0'");
            onTaskErrorDuringVerifyCredentials(result);
            return;
        }

        // Handle the 3 possible errors originating from the server:
        //     -1 = "Required fields were missing";
        //     -2 = "The password was invalid";
        //     -3 = "The INSERT statement failed";
        else if (intResult < 0)
        {
            switch (intResult)
            {
                case -1:
                    Log.e(TAG, getString(R.string.query_params_missing));
                    break;

                // A special case that needs special attention:
                case -2:
                    onTaskErrorDuringVerifyCredentials(getString(R.string.query_params_missing));
                    break;

                case -3:
                    Log.e(TAG, getString(R.string.insert_query_failed));
                    break;
            }

            // We don't want to go any further.
            return;
        }

        // ###########################################################
        // And finally, we have the 'happy' flow:
        // ###########################################################

        // Store the returned "id" (from result) as the AuthToken in UserData:
        mUserData.setAuthToken(intResult);

        // We only want to store the user's account info if the "Remember me" checkbox is checked
        // and an account DOES NOT already exist:
        if (mUserData.getRememberMe() && mAuthenticator.findAccount() == null)
        {
            // Store account info in AccountManager on the user's device:
            Log.d(TAG, "Adding Account to AccountManager");

            mAuthenticator.addAccountExplicitly(
                    mUserData.getUsername(),
                    mUserData.getPassword(),
                    mUserData.getAuthToken());
        }

        // Change the list row image to a checkmark for "Verifying account" and don't show
        // the "skipping" textfield:
        setRowImage(mCurrentState, IMAGE_CHECKMARK);
        setSkippingVisibility(mCurrentState, View.INVISIBLE);

        // Move on to next state: "STATE_STORE_CREDENTIALS"
        changeState(STATE_STORE_CREDENTIALS);
    }

    /**
     * <p>The score was saved in the database. This method displays the successful insertion
     * in the steps/states list visible to the user.</p>
     *
     *   <p>Note: Uses a three second timer to delay calling the next activity. This is important,
     *         in my opinion, so the user can scan the Authentication process steps to see that
     *         everything went according to their prediction. If this delay wasn't in place,
     *         the list would disappear soon after it was drawn. The user would probably be
     *         confused by that.</p>
     *
     * @param result
     */
    public void onSaveScoreTaskCompleted(String result)
    {
        Log.d(TAG, "onSaveScoreTaskCompleted()");

        // Don't display the 'skipping' textfield:
        setSkippingVisibility(mCurrentState, View.INVISIBLE);

        // Delay going to High Scores activity so user can read Authentication steps:
        startTimerBeforeGoingToHighScoresActivity();
    }

    private void startTimerBeforeGoingToHighScoresActivity()
    {
        Log.d(TAG, "startTimerBeforeGoingToHighScoresActivity()");

        // Create and start the "Delay" timer:
        // Note: After this three second delay, the "High Scores" activity is called.
        final Handler timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // After the 3 second delay...
                // Move on to the High Scores activity
                goToHighScoresActivity();
            }
        }, 3000);
    }
}
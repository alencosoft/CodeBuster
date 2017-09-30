package com.android.codebreaker.activities;

/**
 * <p>This is the actual game or main activity. In order to understand the code you should probably
 * first understand the game. Here are the instructions and game play tactics:</p>
 *
 *     <p><i>The game is similar to the old MasterMind game from the '70s where two players try to
 *        guess a colored ball sequence in less turns than their opponent. This game, however,
 *        uses numbers (0-9) instead of colored balls with four slots. If you choose a correct
 *        number you will see a "Gray" circle in the results list. If that same number is in the
 *        correct spot you will see a "Green" circle. The game is over when you choose all four
 *        correct numbers and in their correct order (all "Green" circles). A twist: this game is
 *        timed. If you finish quickly (few turns - less time) your score will be high and the
 *        reverse is true for a lower score.</i></p>
 *
 * <p>The activity layout is broken down into three distinct sections:</p>
 *
 * <ul>
 *     <li>Four number pickers  (each limited to integers 0-9);</li>
 *     <li>An "OK" button       (each click is a turn);</li>
 *     <li>An unselectable list (reveals the outcome of each turn).</li>
 * </ul>
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.android.codebreaker.data.UserData;
import com.android.codebreaker.lists.InputResponseListAdapter;
import com.android.codebreaker.lists.InputResponseListRowData;
import com.android.codebreaker.R;
import com.android.codebreaker.misc.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends Activity implements NumberPicker.OnValueChangeListener
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The maximum number of turns. We don't want to embarrass anyone:
    private final int MAX_NUMBER_OF_TURNS = 100;

    // The bridge between the Response list view and the data:
    InputResponseListAdapter mInputResponseListAdapter;

    // The data tied to the List Adapter by row:
    ArrayList<InputResponseListRowData> mData;

    // Store the Number Picker current values:
    Map<Integer, Integer> mNumberPickerValues = new TreeMap<Integer, Integer>();

    // Holds the Secret Number (four digits each 0-9) in a four integer array:
    int[] mSecretNumber = new int[4];

    // Need to keep track of how many times the user clicks the "OK" button (ie; Number of turns).
    // The final score is calculated based, in part, on these turns.
    Integer mTurns = 0;

    // A reference to the list that shows the user each turn and its' result.
    ListView mListView;

    // We need to preserve the start time since "duration" is a factor in calculating the score.
    long mStartTime;

    /**
     * <p>Initializes the screen with the visual components.</p>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Don't want a title on this screen/activity:
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // See /res/layout/activity_main for more details:
        setContentView(R.layout.activity_main);

        // Set properties on the four Number Pickers
        setNumberPickerProperties(
                (NumberPicker) findViewById(R.id.numberPicker1), savedInstanceState);
        setNumberPickerProperties(
                (NumberPicker) findViewById(R.id.numberPicker2), savedInstanceState);
        setNumberPickerProperties(
                (NumberPicker) findViewById(R.id.numberPicker3), savedInstanceState);
        setNumberPickerProperties(
                (NumberPicker) findViewById(R.id.numberPicker4), savedInstanceState);

        // The reference to the /res/layout/list_view_all_lists.list:
        mListView = (ListView) findViewById(R.id.list);

        // This is the first time the user came to this activity:
        if(savedInstanceState == null)
        {
            // Set the secret number
            Random randomGenerator = new Random();
            mSecretNumber[0] = randomGenerator.nextInt(10);
            mSecretNumber[1] = randomGenerator.nextInt(10);
            mSecretNumber[2] = randomGenerator.nextInt(10);
            mSecretNumber[3] = randomGenerator.nextInt(10);

            Log.i(TAG, "****** SECRET NUMBER ******: " +
                    Utilities.convertIntArrayToString(mSecretNumber));

            // Set the start time
            mStartTime = System.currentTimeMillis();

            // Initialize mData (Holds
            mData = new ArrayList<>();
        }

        // User has returned to this activity so set the class variables to what they were
        // before the user left:
        else
        {
            mTurns = savedInstanceState.getInt("mTurns");
            mSecretNumber = savedInstanceState.getIntArray("mSecretNumber");
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable("mListView"));
            mData = savedInstanceState.getParcelableArrayList("mData");
            mStartTime = savedInstanceState.getLong("mStartTime");
        }

        // Create the new list adapter
        mInputResponseListAdapter =
                new InputResponseListAdapter(this, R.layout.listview_item_row_input_response, mData);

        // Bind the list adapter to the list view
        mListView.setAdapter(mInputResponseListAdapter);

        // Things have changed, so refresh the list in the view
        mInputResponseListAdapter.notifyDataSetChanged();
    }

    /**
     * <p>The user has returned to the game. So make it look like it did before.</p>
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save Number Picker states
        NumberPicker np1 = (NumberPicker) findViewById(R.id.numberPicker1);
        outState.putInt(Integer.toString(np1.getId()), mNumberPickerValues.get(np1.getId()));

        NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        outState.putInt(Integer.toString(np2.getId()), mNumberPickerValues.get(np2.getId()));

        NumberPicker np3 =(NumberPicker) findViewById(R.id.numberPicker3);
        outState.putInt(Integer.toString(np3.getId()), mNumberPickerValues.get(np3.getId()));

        NumberPicker np4 =(NumberPicker) findViewById(R.id.numberPicker4);
        outState.putInt(Integer.toString(np4.getId()), mNumberPickerValues.get(np4.getId()));

        // Save start time
        outState.putLong("mStartTime", mStartTime);

        // Save number of turns
        outState.putInt("mTurns", mTurns);

        // Save secret number
        outState.putIntArray("mSecretNumber", mSecretNumber);

        // Save list view state
        outState.putParcelable("mListView", mListView.onSaveInstanceState());

        // Save the list view's data
        outState.putParcelableArrayList("mData", mData);
    }

    /**
     * <p>Setup a Number Picker with the correct properties and value.</p>
     *
     * @param numberPicker
     * @param savedInstanceState - In case we're returning to the game
     */
    private void setNumberPickerProperties(NumberPicker numberPicker, Bundle savedInstanceState)
    {
        Log.d(TAG, "Setting Number Picker properties.");

        // The Properties:
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(9);
        numberPicker.setOnValueChangedListener(this);

        // The default value for each Number Picker:
        int value = 0;

        // Override the default value if we're coming back to the game:
        if (savedInstanceState != null)
        {
            value = savedInstanceState.getInt(Integer.toString(numberPicker.getId()));
            numberPicker.setValue(value);
        }

        // Save the Number Picker value in the values array:
        mNumberPickerValues.put(numberPicker.getId(), value);
    }

    /**
     * <p>Required by the <i>NumberPicker.OnValueChangeListener</i> interface.</p>
     *
     * @param numberPicker
     * @param oldVal
     * @param newVal
     */
    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal)
    {
        // Set the new value in the values array:
        mNumberPickerValues.put(numberPicker.getId(), newVal);
    }

    /**
     * <p>The "OK" button callback. This is the button the user selects when he/she thinks
     * they have the correct Secret Number.</p>
     *
     * @param v The View.
     */
    public void okButtonClick(View v)
    {
        Log.d(TAG, "'OK' button clicked");

        // Add another turn to the user's game:
        mTurns++;

        // First we need to convert the mNumberPickerValues hashmap values into an Array so
        // we can iterate over both the Secret Number and Number Picker values using one for loop:
        Collection<Integer> values = mNumberPickerValues.values();
        Integer[] numberPickerValues = values.toArray(new Integer[values.size()]);

        // We need to clone the Secret Number array so we can change the values when we're
        // comparing between the Secret Number array and the user's selected numbers in the
        // Number Pickers. We change the number to a -1 when we've located a "hit". Thus we won't
        // have any further "hits" on that same number in later iterations. For instance, if the
        // Secret Number is [1,2,3,4] and the user selected [1,1,1,1] we only want one hit.
        // After recording the one hit, we change the Secret Number's "1" to a -1. So, the final
        // Secret Number array looks like this: [-1,2,3,4].
        int[] tempSecretNumber = mSecretNumber.clone();

        // This is where we store the "hits":
        ArrayList<Integer> coloredBalls = new ArrayList<Integer>();

        // We need to keep track of this. If the number of "Green" balls is four, the user has
        // found the secret number and the UI needs to navigate to the Authenticator activity.
        int totalGreenBalls = 0;

        // How many numbers entered are in the right spots (ie; "Green" balls)?
        // This is fairly straight forward. We compare the Secret Number array index value to the
        // same index value in the Number Picker values array: There's a 1:1 relationship
        // concerning array indices.
        for (int iter = 0; iter < tempSecretNumber.length; iter++)
        {
            if (numberPickerValues[iter] == mSecretNumber[iter])
            {
                // Add a green to Colored Balls array:
                coloredBalls.add(R.drawable.green);

                // Make the Secret Number value a -1 so we won't have any more hits on that one:
                tempSecretNumber[iter] = -1;

                // Increment the Total Green Balls value. We use this later to see if we have
                // all Green balls which means the user has selected the Secret Number.
                totalGreenBalls++;
            }
        }

        // How many numbers entered are in the secret number but are NOT in the right spot?
        // This is a little more complicated. We are trying to find if any of the user's selected
        // numbers are in the Secret Number array. If we find one, we record it as a "Grey" ball.
        for (Integer value : mNumberPickerValues.values())
        {
            for (int iter = 0; iter < tempSecretNumber.length; iter++)
            {
                if (value == tempSecretNumber[iter])
                {
                    // Add a grey to Colored Balls array:
                    coloredBalls.add(R.drawable.grey);

                    // Make the Secret Number value a -1 so we won't have any more hits on that one:
                    tempSecretNumber[iter] = -1;

                    break;
                }
            }
        }

        // Fill the remaining slots in coloredBalls with black (ie; no hits):
        for (int iter = coloredBalls.size(); iter < 4; iter++)
        {
            coloredBalls.add(R.drawable.black);
        }

        // Add all of the data to the ArrayList. A new row is added to the list, at least it's
        // data.
        mData.add(0, new InputResponseListRowData(Integer.toString(mTurns),
                mNumberPickerValues.values().toString(),
                coloredBalls.get(0),
                coloredBalls.get(1),
                coloredBalls.get(2),
                coloredBalls.get(3)));

        // The List adapter will take the above "new" data and create a new visual row with it.
        mInputResponseListAdapter.notifyDataSetChanged();

        // Do we have all Green balls? If yes then the user has selected the Secret Number and
        // we need to move on to the Authenticator activity.
        //
        // Note: The below conditional could've followed the first loop above (the Green balls
        // loop). But, I think it's best to show the user the four green balls visually before
        // navigating away. It's a "woohoo" thing.
        if (totalGreenBalls == 4)
        {
            Log.i(TAG, "User has Correctly guessed the Secret Number.");

            gotoEndGame();
        }
    }

    /**
     * <p>The user has won! Yay! Let's store some of the data and move on to the Authenticator
     * activity.</p>
     */
    private void gotoEndGame()
    {
        Log.d(TAG, "gotoEndGame()");
        Log.d(TAG, "gotoEndGame(): Calculating score, storing stuff...");

        // Create the "serializable" UserData object that will be passed to the Authentication
        // activity.
        UserData userData = new UserData();

        // Set the Secret Number, Turns, Duration, and Score in the data object.
        userData.setSecretNumber(Utilities.convertIntArrayToString(mSecretNumber));
        userData.setTurns(mTurns);

        // Be sure the number of turns hasn't exceeded the max:
        if (mTurns > MAX_NUMBER_OF_TURNS)
        {
            userData.setTurns(MAX_NUMBER_OF_TURNS);
        }

        userData.setTimeInSeconds((int) (long) ((System.currentTimeMillis() - mStartTime) / 1000));
        userData.setScore(Utilities.calculateScore(mTurns, mStartTime));

        // Create the intent with the UserData object:
        Intent authenticationIntent = new Intent(this, AuthenticatorActivity.class);
        authenticationIntent.putExtra(getString(R.string.KEY_USER_DATA), userData);

        Log.d(TAG, "gotoEndGame(): Moving on to Authentication activity.");

        // On to User Authentication...
        startActivity(authenticationIntent);
    }
}

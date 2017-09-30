package com.android.codebreaker.misc;

/**
 * A few static Utility methods that make Unit Testing easier and don't clog activity code.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.codebreaker.R;

public class Utilities
{
    /**
     * Calculates the final score that will be displayed in the UI and stored in the Database.
     * This algorithm rewards speed and low number of turns.
     *
     * @param strTurns
     * @param intStartTime
     * @return String
     */
    public static int calculateScore(int turns, long startTime)
    {
        // Sanity check for passed variables
        if ( turns < 1 || startTime < 1 )
        {
            return 0;
        }

        // Get the duration of time since starting
        int duration = (int) (long) ((System.currentTimeMillis() - startTime) / 1000);

        // Set a cap on the number of turns
        if (turns > 100)
        {
            turns = 100;
        }

        // Set a cap on the duration
        if (duration > 9999)
        {
            duration = 9999;
        }

        // A simple algorithm that penalizes "sand-bagging" duration and turns. (ie; The longer
        // it takes and the more turns it takes equals a lower score.)
        return ((10000 - duration) / turns) * 100;

        // Converts/Returns the score in the desired format. (ie; 100000 will be returned
        // as "100,000".)
        //return NumberFormat.getNumberInstance(Locale.US).format(score);
    }

    /**
     * Method to check if a String "isNumeric". Will log a NumberFormatException if not.
     *
     * @param stringToCheck
     * @return True if it is a number; otherwise false.
     */
    public static Boolean isStringInt(String stringToCheck)
    {
        try
        {
            Integer.parseInt(stringToCheck);
            return true;
        }
        catch (NumberFormatException exception)
        {
            Log.d("", "Utilities.isStringInt().NumberFormatException: ", exception);
            return false;
        }
    }

    /**
     * Used mainly for converting the Secret Number array into a four character String.
     *
     * @param intArray
     * @return String
     */
    public static String convertIntArrayToString(int[] intArray)
    {
        // Straight-forward approach to concatenating Array values into a String.
        StringBuilder stringBuilder = new StringBuilder();

        // Loop through the integer array and append each array item to the end of the new string.
        for (int iterator : intArray)
        {
            stringBuilder.append(iterator);
        }

        // Return the new string.
        return stringBuilder.toString();
    }

    /**
     * <p>Uses the framework's ConnectivityManager to determine if the user is connected to the
     * internet.</p>
     *
     * @param context
     * @return boolean - true if connected; otherwise false
     */
    public static boolean isUserConnectedToInternet(Context context)
    {
        // The gateway to the device's wifi/network interface:
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get all information about an active network:
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Is there an active network?
        boolean isConnectedToNetwork =
                (activeNetwork != null && activeNetwork.isConnectedOrConnecting());

        // No active network so send the disconnected state message to the user:
        if ( ! isConnectedToNetwork)
        {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.no_internet_connection_error),
                    Toast.LENGTH_SHORT).show();
        }

        return isConnectedToNetwork;
    }
}

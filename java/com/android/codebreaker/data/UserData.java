package com.android.codebreaker.data;

/**
 * A data class used by several Activities and Database interactions. I like
 * this method of passing data between activities for several reasons:
 *
 *     1) Type Safety: Using Parcelable objects or Bundles requires a lot of overhead and
 *        neither type check at compile time. In other words, you could ask for the "Secret Number"
 *        by typing "secretnumber" in an activities passed Intent although the stored data "Key" is
 *        "secret_number". The code will compile fine but the resulting "Value" will be null. This
 *        is bad.
 *
 *     2) Unit Testing: It is easy to mock this class making unit testing a snap.
 */

import com.android.codebreaker.misc.Utilities;

import java.io.Serializable;

public class UserData implements Serializable
{
    private String mUsername;
    private String mPassword;
    private int mAuthToken;
    private boolean mRememberMe;
    private String mSecretNumber;
    private int mTurns;
    private int mTimeInSeconds;
    private int mScore;

    /**
     * Empty constructor. Use the individual setters below...
     */
    public UserData() { }

    /**
     * ##############################################################################
     * Below are the various data setters/getters. They are needed by Account
     * Authentication and Database interaction.
     * ##############################################################################
     */

    /**
     * "mUsername", "mPassword", "mAuthToken" - All used for user verification and data
     *     storage/retrieval.
     *
     *     NOTE: "mAuthToken" should be identical to DB.users table ("id" field).
     *
     * @param username, password, userId, rememberMe
     */

    // "mUsername"
    public void setUsername(String username)
    {
        mUsername = username.trim();
    }
    public String getUsername() { return mUsername; }

    // "mPassword"
    public void setPassword(String password)
    {
        mPassword = password.trim();
    }
    public String getPassword() { return mPassword; }

    // "mAuthToken"
    public void setAuthToken(int authToken)
    {
        mAuthToken = authToken;
    }
    public int getAuthToken() { return mAuthToken; }

    // "mRememberMe"
    public void setRememberMe(boolean rememberMe) { mRememberMe = rememberMe; }
    public boolean getRememberMe() { return mRememberMe; }

    /**
     * "mSecretNumber" - This number is actually stored as a String rather than an int. The
     *                   requirement is a series of four characters with each character between
     *                   "0" and "9". Thus converting this to an int is rather simple. I've
     *                   created a Utility method that ensures this String is an actual series
     *                   of four numbers.
     * @param secretNumber
     */
    public void setSecretNumber(String secretNumber)
    {
        mSecretNumber = "";

        if (Utilities.isStringInt(secretNumber))
        {
            mSecretNumber = secretNumber.trim();
        }
    }
    public String getSecretNumber() { return mSecretNumber; }

    /**
     * "mTurns" - The number of turns it takes the user to guess the Secret Number.
     */
    public void setTurns(int turns) { mTurns = turns; }
    public int getTurns() { return mTurns; }

    /**
     * "mTimeInSeconds" - The amount of time in seconds it takes the user to guess the
     * Secret Number.
     */
    public void setTimeInSeconds(int timeInSeconds) { mTimeInSeconds = timeInSeconds; }
    public int getTimeInSeconds() { return mTimeInSeconds; }

    /**
     * "mScore": For testing purposes, the actual score is calculated in the Utilities class.
     *           Thus this class can be mocked easily and doesn't require testing.
     * @param score
     */
    public void setScore(int score) { mScore = score; }
    public int getScore() { return mScore; }
}

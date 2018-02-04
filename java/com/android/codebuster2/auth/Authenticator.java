package com.android.codebuster2.auth;

/**
 * <p>So, why even worry about setting all of this info in the device's AccountManager? Glad you
 * asked. Having account info on the device allows the user to play successive games any time in
 * the future without having to login. This, in my opinion, is a very nice feature.</p>
 *
 * <p>This class handles all interactions with the device's Account Manager. This includes:</p>
 *
 * <ul>
 *     <li>adding an account;</li>
 *     <li>removing an account;</li>
 *     <li>finding an account;</li>
 *     <li>and a few getters from an account.</li>
 * </ul>
 *
 * <p>Note: I chose not to extend the framework's AbstractAccountAuthenticator because of the
 * overhead of the framework and lack of flexibility.</p>
 */

import android.accounts.*;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.codebuster2.R;

public class Authenticator
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The context of the calling activity (needed for acquiring strings in resources):
    private final Context mContext;

    // Framework object that allows access to Android device accounts:
    private AccountManager mAccountManager;

    /**
     * <p>Constructor. Not much, just set "context" and intialize the AccountManager object.</p>
     *
     * @param context
     */
    public Authenticator(Context context)
    {
        Log.d(TAG, "constructor()");

        // Set class level variables:
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }

    /**
     * <p>As the name implies, this method removes an account from the devices Account Manager.
     * This is a little dangerous as the Account Manager's "removeAccountExplicitly" method
     * requires API level 22 or higher. This is why I've set the minSDK to level 22 in the
     * Manifest and build.gradle files.</p>
     *
     * @param account
     */
    public final void removeAccount(Account account)
    {
        Log.d(TAG, "removeAccount()");

        // Remove it. This works for API calls above v.21.
        mAccountManager.removeAccountExplicitly(account);
    }

    /**
     * <p>A utility method that searches all Accounts for the "greggscoolapps.com" account. If one
     * is found it returns it. Otherwise the method returns null which means there isn't an
     * account.</p>
     *
     * @return
     */
    public final Account findAccount()
    {
        Log.d(TAG, "findAccount()");

        // Cycle through accounts looking for one of ours:
        for (Account account : mAccountManager.getAccounts())
        {
            // Is this account ours? If so, do some "info" logging and return the account:
            if (TextUtils.equals(account.type, mContext.getString(R.string.auth_type)))
            {
                Log.i(TAG, "findAccount(): Account exists! Here are the specifics:");
                Log.i(TAG, "               account.name: " + account.name);
                Log.i(TAG, "               account.password: " +
                        mAccountManager.getPassword(account));
                Log.i(TAG, "               account.authToken: " +
                        mAccountManager.peekAuthToken(account, account.type));

                return account;
            }
        }

        Log.w(TAG, "findAccount(): Account does NOT exist!");

        // Our account doesn't exist, so return null:
        return null;
    }

    /**
     * <p>Adds an account to the device. There are three params this "add" process requires:</p>
     *
     * <ul>
     *     <li>UserName;</li>
     *     <li>Password;</li>
     *     <li>Authentication Type.</li>
     * </ul>
     *
     * <p>The "Authentication Token" is NOT a required variable but is a nice place to store
     * the User's ID (from the accounts table on the server).</p>
     *
     * @param accountName
     * @param password
     * @param userDBTableId
     * @return
     */
    public Account addAccountExplicitly(
            String accountName,
            String password,
            int userDBTableId)
    {
        Log.d(TAG, "addAccountExplicitly()");

        // Create the new account with Account Name and TYPE
        final Account account = new Account(accountName, mContext.getString(R.string.auth_type));

        // Try to add the account to the Android device:
        if (mAccountManager.addAccountExplicitly(account, password, null))
        {
            // Worked, yay!
            Log.i(TAG, "addAccountExplicitly().Account added");

            // Set the AuthToken to the user_id from the server-side "accounts" table:
            mAccountManager.setAuthToken(
                    account,
                    mContext.getString(R.string.auth_type),
                    String.valueOf(userDBTableId));

            // Return the fully formed account we just added:
            return findAccount();
        }

        // Uh oh! Adding didn't work. It could be that the account already exists.
        Log.w(TAG, "addAccountExplicitly(): Account NOT added");

        // So, sadly, we'll return null:
        return null;
    }

    /**
     * <p>Get password:</p>
     *
     * @param account
     * @return The password associated with the account.
     */
    public String getPasswordFromAccount(Account account)
    {
        return mAccountManager.getPassword(account);
    }

    /**
     * <p>Get Authentication Token (eg; database accounts.user_id):</p>
     *
     * @param account
     * @return The Athentication Token associated with the account.
     */
    public String getAuthTokenFromAccount(Account account)
    {
        return mAccountManager.peekAuthToken(account, mContext.getString(R.string.auth_type));
    }
}

package com.android.codebreaker.auth;

/**
 * <p>This is the "Login/Create Account" dialogue window. Minus the UI components, the bulk of
 * this class handles input validity and passing information back to the calling activity
 * (using the AuthenticatorLoginDialogListener interface).</p>
 *
 * <p>It contains five UI components:</p>
 *
 * <ul>
 *     <li>Username editable textfield;</li>
 *     <li>Password editable textfield;</li>
 *     <li>Remember me Checkbox;</li>
 *     <li>Two buttons:</li>
 *          <ul>
 *              <li>"Login/Create account";</li>
 *              <li>"Not now".</li>
 *          </ul>
 * </ul>
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.codebreaker.R;

import java.util.regex.Pattern;

public class AuthenticatorLoginDialogFragment extends DialogFragment
{
    /**
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface AuthenticatorLoginDialogListener
    {
        public void onLoginDialogPositiveClick(
                String username,
                String password,
                Boolean rememberMe);

        public void onLoginDialogNegativeClick();
    }

    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // The various UI components... Needed to access their values:
    private EditText mUsername;
    private EditText mPassword;
    private CheckBox mRememberMe;

    // There's a setter for this. It's possible (race condition) that the username string is set
    // before the actual textfield has finished building.
    private String mUsernameString = "";

    // Delivers action events to any class that registers with the
    // AuthenticatorLoginDialogListener interface.
    AuthenticatorLoginDialogListener mListener;

    /**
     * <p>Instantiates the view and builds its' components.</p>
     *
     * @param savedInstanceState
     * @return this dialogue box
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateDialog()");

        // Setup the dialogue builder:
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater and set the view/class vars.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.login_popup, null);

        // We need access to these UI components so we can retrieve their "values":
        mUsername = (EditText) view.findViewById(R.id.username);
        mPassword = (EditText) view.findViewById(R.id.password);
        mRememberMe = (CheckBox) view.findViewById(R.id.remember_me);

        // Set the username text if this is a return trip to this dialog and a username
        // string exists.
        mUsername.setText(mUsernameString);

        // Set Positive button. Notice the "null" Click listener. We want to override this
        // so that the alert window won't always close when the user clicks this button. Why?
        // Because we need to first verify the Username and Password are valid entries. The
        // OnClickListener override is nested inside the OnShow() override below.
        builder.setPositiveButton(R.string.button_text_login_create, null);

        // If the user clicks the negative button then we just bail and move on.
        builder.setNegativeButton(
                R.string.button_text_login_cancel,
                new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                // Sends the negative button event back to the host activity
                mListener.onLoginDialogNegativeClick();

                // Then dismisses this dialog.
                dialog.dismiss();
            }
        });

        // Inflates and sets the layout for the dialog
        // Passes null as the parent view because its going in the dialog layout.
        builder.setView(view);

        // We need the AlertDialog reference so we can override the Positive button's
        // onClickListener. This gives us more control over when to dismiss the dialog.
        final AlertDialog thisDialog = builder.create();

        // We don't want to cancel the Login operation if a user touches outside the dialog box.
        thisDialog.setCanceledOnTouchOutside(false);

        // Override the onShow callback. Only one reason: We don't always want the dialog to cancel
        // on button mash. We need to verify the username and password are valid first.
        //
        // Note: We'll log all of the button mashes in the caller's listener methods.
        thisDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                Button positiveButton = thisDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                positiveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Check both Username and Password EditText fields for validity
                        if (checkInputValidity(mUsername.getText().toString().trim()) == true &&
                            checkInputValidity(mPassword.getText().toString().trim()) == true)
                        {
                            // Sends the positive button event back to the host activity
                            mListener.onLoginDialogPositiveClick(
                                    mUsername.getText().toString().trim(),
                                    mPassword.getText().toString().trim(),
                                    mRememberMe.isChecked());

                            // It's safe to dismiss the dialogue window at this point:
                            thisDialog.dismiss();
                        }
                    }
                });
            }
        });

        // In case the server-side account authentication didn't work. At this point the dialogue
        // window is still visible and the username is still populated. So, we want the "Password"
        // textfiled to have the focus:
        if (mUsername.getText().length() > 0)
        {
            mPassword.requestFocus();
        }

        return thisDialog;
    }

    /**
     * <p>Checks the validity of the username/password strings. There are three checks:</p>
     *
     * <ul>
     *     <li>Both the Username and Password textfields must have characters;</li>
     *     <li>Only letters and numbers are allowed in each;</li>
     *     <li>Both the Username and Password must be between 8 and 16 chars in length.</li>
     * </ul>
     *
     * @param stringToCheck
     * @return Boolean - True if valid, false otherwise.
     */
    private Boolean checkInputValidity(String stringToCheck)
    {
        // Are both the Username and Password filled in by the user?
        if (TextUtils.isEmpty(stringToCheck))
        {
            Toast.makeText(getActivity(), R.string.login_fail_empty_strings,
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        // Only letters and numbers are allowed.
        if (Pattern.matches("\\w+", stringToCheck))
        {
            // String must be between 8 and 16 chars long.
            if (stringToCheck.length() >= 8 && stringToCheck.length() <= 16)
            {
                return true;
            }

            // Is the string too short or too long?
            else
            {
                Toast.makeText(
                        getActivity(),
                        R.string.login_fail_text_length,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Only letters and numbers please!
        else
        {
            Toast.makeText(
                    getActivity(),
                    R.string.login_fail_validity,
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    /**
     * <p>Sets the username (class variable) and attempts to set it in the "username" textfield.
     * There is a race condition that may exist: This setter may be called before the username
     * textfield has finished building.</p>
     *
     * @param username
     */
    public void setUsername(String username)
    {
        Log.d(TAG, "setUsername()");

        // Populate the class variable with the passed string:
        mUsernameString = username;

        // Does the Username textfield exist?
        if (mUsername != null)
        {
            Log.i(TAG, "setUsername(): The Username textfield exists.");

            // Populate the Username textfield with passed string:
            mUsername.setText(mUsernameString);
        }
        else
        {
            Log.i(TAG, "setUsername(): The Username textfield DOES NOT exist.");
        }
    }

    /**
     * <p>Overrides the Fragment.onAttach() method to instantiate the
     * AuthenticatorLoginDialogListener</p>
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity)
    {
        Log.d(TAG, "onAttach()");

        super.onAttach(activity);

        // Verify that the host activity implemented the callback interface
        try
        {
            // Instantiate the AuthenticatorLoginDialogListener so we can send events to the host
            mListener = (AuthenticatorLoginDialogListener) activity;
        }
        catch (ClassCastException e)
        {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AuthenticatorLoginDialogListener");
        }
    }
}

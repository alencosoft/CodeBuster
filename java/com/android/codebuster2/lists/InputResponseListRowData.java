package com.android.codebuster2.lists;

/**
 * <p>A Parcelable data class to store each list "row's" data. This data object will then be used
 * to build out a row using the list's adapter class.</p>
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class InputResponseListRowData implements Parcelable
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    public String turn;
    public String response;
    public int icon1;
    public int icon2;
    public int icon3;
    public int icon4;

    /**
     * <p>Called from our activity to add a new data row to our list.</p>
     *
     * @param turn
     * @param response
     * @param icon1
     * @param icon2
     * @param icon3
     * @param icon4
     */
    public InputResponseListRowData(String turn,
                                    String response,
                                    int icon1,
                                    int icon2,
                                    int icon3,
                                    int icon4)
    {
        Log.d(TAG, "Constructor");

        this.turn = turn;
        this.response = response;
        this.icon1 = icon1;
        this.icon2 = icon2;
        this.icon3 = icon3;
        this.icon4 = icon4;
    }

    /**
     * <p>Called by "Creator" with flattened Parcel data.</p>
     *
     * @param in
     */
    private InputResponseListRowData(Parcel in)
    {
        Log.d(TAG, "InputResponseListRowData(Parcel in)");

        turn = in.readString();
        response = in.readString();
        icon1 = in.readInt();
        icon2 = in.readInt();
        icon3 = in.readInt();
        icon4 = in.readInt();
    }

    /**
     * Required by interface: Not used and isn't very useful, IMHO.
     *
     * @return Zero. No usages.
     */
    public int describeContents()
    {
        return 0;
    }

    /**
     * Required by interface: Not used and isn't very useful, IMHO.
     *
     * @return Empty string. No usages.
     */
    @Override
    public String toString()
    {
        return "";
    }

    /**
     * <p>Just as the "Creator" reconstructs flattened data, this one does the opposite. It
     * flattens the data so the Parcel can be sent somewhere (ie; an intent). Think of this
     * "flattening" as a QueryString.</p>
     *
     * @param out
     * @param flags
     */
    public void writeToParcel(Parcel out, int flags)
    {
        Log.d(TAG, "writeToParcel()");

        out.writeString(turn);
        out.writeString(response);
        out.writeInt(icon1);
        out.writeInt(icon2);
        out.writeInt(icon3);
        out.writeInt(icon4);
    }

    /**
     * <p>Creator class reconstructs parcelable data objects. Here the construction of the
     * "GetScoresListRowData" is created with a flattened Parcel (from our adapter class) and calls
     * private constructor above with "Parcel in". This will then store the info in class vars
     * where we can access them.</p>
     */
    public static final Parcelable.Creator<InputResponseListRowData>
            CREATOR = new Parcelable.Creator<InputResponseListRowData>()
    {
        // Used in Logs
        private final String TAG = this.getClass().getSimpleName();

        // Calls class private constructor:
        public InputResponseListRowData createFromParcel(Parcel in)
        {
            Log.d(TAG, "CREATOR: createFromParcel()");

            return new InputResponseListRowData(in);
        }

        public InputResponseListRowData[] newArray(int size)
        {
            Log.d(TAG, "CREATOR: newArray()");

            return new InputResponseListRowData[size];
        }
    };
}
package com.android.codebuster2.lists;

/**
 * <p>A Parcelable data class to store each list "row's" data. This data object will then be used
 * to build out a row using the list's adapter class.</p>
 */

import android.os.Parcel;
import android.os.Parcelable;

public class GetScoresListRowData implements Parcelable
{
    public int mIndex;
    public String mUsername;
    public String mScore;

    /**
     * <p>Called from our activity to add a new data row to our list.</p>
     *
     * @param index
     * @param username
     * @param score
     */
    public GetScoresListRowData(int index, String username, String score)
    {
        super();

        mIndex = index;
        mUsername = username;
        mScore = score;
    }

    /**
     * <p>Called by "Creator" with flattened Parcel data.</p>
     *
     * @param in
     */
    private GetScoresListRowData(Parcel in)
    {
        mIndex = in.readInt();
        mUsername = in.readString();
        mScore = in.readString();
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
     * @return The username and score of the "Score's List" row. No usages.
     */
    @Override
    public String toString()
    {
        return mUsername + ": " + mScore;
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
        out.writeInt(mIndex);
        out.writeString(mUsername);
        out.writeString(mScore);
    }

    /**
     * <p>Creator class reconstructs parcelable data objects. Here the construction of the
     * "GetScoresListRowData" is created with a flattened Parcel (from our adapter class) and calls
     * private constructor above with "Parcel in". This will then store the info in class vars
     * where we can access them.</p>
     */
    public static final Parcelable.Creator<GetScoresListRowData>
            CREATOR = new Parcelable.Creator<GetScoresListRowData>()
    {
        // Calls class private constructor:
        public GetScoresListRowData createFromParcel(Parcel in)
        {
            return new GetScoresListRowData(in);
        }

        public GetScoresListRowData[] newArray(int size)
        {
            return new GetScoresListRowData[size];
        }
    };
}
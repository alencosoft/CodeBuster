package com.android.codebreaker.lists;

/**
 * <p>The meld between the list components in each row and their data. (eg; The "glue" that binds
 * the components to the data).</p>
 *
 * <p>This approach (an array list adapter class that extends ArrayAdapter) is rather pedestrian.
 * Get the data (in our case from a server database), override the baseclass "getView()" method to
 * build out a row then populate that row with data.</p>
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.codebreaker.R;

import java.util.List;

public class GetScoresListAdapter extends ArrayAdapter<GetScoresListRowData>
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // We need the "activity" context for the Layout Inflater:
    Context mContext;

    // Needed for the Layout Inflater to point to the correct ListView:
    int mLayoutResourceId;

    // All of the data necessary to build the list (by row):
    List<GetScoresListRowData> mData = null;

    // Used for highlighting the user's current score:
    int mHighlightedIndex = -1;

    /**
     * <p>Sets up the variables needed for list population.</p>
     *
     * @param context
     * @param layoutResourceId
     * @param data
     * @param highlightedIndex
     */
    public GetScoresListAdapter(Context context,
                                int layoutResourceId,
                                List<GetScoresListRowData> data,
                                int highlightedIndex)
    {
        super(context, layoutResourceId, data);

        // Class variable assignments:
        mLayoutResourceId = layoutResourceId;
        mContext = context;
        mData = data;
        mHighlightedIndex = highlightedIndex;

        Log.i("Highlighted index", "" + Integer.toString(mHighlightedIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.d(TAG, "getView()");

        // The "holder" here is the set of UI components in each row:
        GetScoresListRowComponents holder = null;

        // Initialize the row's components only if the requested row is null (ie; has NOT been
        // initialized before):
        if(convertView == null)
        {
            Log.i(TAG, "getView(): The row is empty. That's good.");

            // Blowup (inflate) the list with the new row:
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);

            // Fill the holder with new components:
            holder = new GetScoresListRowComponents();

            Log.i(TAG, "getView(): Filling the row with UI components.");

            // Give the holder some teeth: Pointers for each textfield in the row:
            holder.txtNumber = (TextView)convertView.findViewById(R.id.txtNumber);
            holder.txtUsername = (TextView)convertView.findViewById(R.id.txtUsername);
            holder.txtScore = (TextView)convertView.findViewById(R.id.txtScore);

            // Add the "holder" to the new row in the form of a tag:
            convertView.setTag(holder);
        }

        // Row already exists so get the "holder" from the row's tag:
        else
        {
            Log.i(TAG, "getView(): Row already exists. Store pointers to the components.");

            holder = (GetScoresListRowComponents)convertView.getTag();
        }

        // We want the user's score to stand out, so, make the background of that row "BLUE":
        if (position == mHighlightedIndex)
        {
            convertView.setBackgroundColor(Color.parseColor("#000066"));
        }

        // Not the user's current score (row) so make the row's background transparent:
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        Log.i(TAG, "getView(): Populating the row with data.");

        // And finally, populate the various "holder" textfields with data:
        GetScoresListRowData getScoresListRowData = mData.get(position);
        holder.txtNumber.setText(Integer.toString(getScoresListRowData.mIndex));
        holder.txtUsername.setText(getScoresListRowData.mUsername);
        holder.txtScore.setText(getScoresListRowData.mScore);

        Log.i(TAG, "getView(): Row is now ready.");

        // The row is now ready so return it:
        return convertView;
    }

    /**
     * <p>A "holder" of the ListView's "row" UI components: Three textfields.</p>
     */
    static class GetScoresListRowComponents
    {
        TextView txtNumber;
        TextView txtUsername;
        TextView txtScore;
    }
}
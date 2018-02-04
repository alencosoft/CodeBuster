package com.android.codebuster2.lists;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.codebuster2.R;

import java.util.List;

public class InputResponseListAdapter extends ArrayAdapter<InputResponseListRowData>
{
    // Used in Logs
    private final String TAG = this.getClass().getSimpleName();

    // We need the "activity" context for the Layout Inflater:
    Context mContext;

    // Needed for the Layout Inflater to point to the correct ListView:
    int mLayoutResourceId;

    // All of the data necessary to build the list (by row):
    List<InputResponseListRowData> mData = null;

    public InputResponseListAdapter(
            Context context,
            int layoutResourceId,
            List<InputResponseListRowData> data)
    {
        super(context, layoutResourceId, data);

        // Class variable assignments:
        mLayoutResourceId = layoutResourceId;
        mContext = context;
        mData = data;
    }

    /**
     * {@inheritDoc}
     *
     * NOTE: This method is called twice as we need to modify a row as it gets pushed down in the
     *       list. Why? Because we want the latest round of "guesses" to be at the top of the
     *       list. Therefore, we change the row's appearance/values on each turn.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.d(TAG, "getView()");

        // The "holder" here is the set of UI components in each row:
        InputResponseListRowComponents inputResponseListRowComponents = null;

        // Initialize the row's components only if the requested row is null (ie; has NOT been
        // initialized before):
        if(convertView == null)
        {
            Log.i(TAG, "getView(): The row is empty. That's good.");

            // Blowup (inflate) the list with the new row:
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);

            Log.i(TAG, "getView(): Filling the row with UI components.");

            // Fill the row "container" with new components:
            inputResponseListRowComponents = new InputResponseListRowComponents();

            // Give the row "container" some teeth: Pointers for each UI component in the row:
            inputResponseListRowComponents.txtTurn     =
                    (TextView)convertView.findViewById(R.id.txtTurn);
            inputResponseListRowComponents.txtResponse =
                    (TextView)convertView.findViewById(R.id.txtResponse);
            inputResponseListRowComponents.imgIcon1    =
                    (ImageView)convertView.findViewById(R.id.imgIcon1);
            inputResponseListRowComponents.imgIcon2    =
                    (ImageView)convertView.findViewById(R.id.imgIcon2);
            inputResponseListRowComponents.imgIcon3    =
                    (ImageView)convertView.findViewById(R.id.imgIcon3);
            inputResponseListRowComponents.imgIcon4    =
                    (ImageView)convertView.findViewById(R.id.imgIcon4);

            // Add the row "container" to the new row in the form of a tag:
            convertView.setTag(inputResponseListRowComponents);
        }

        // Row already exists so get the row "container" from the row's tag:
        else
        {
            Log.i(TAG, "getView(): Row already exists. Store pointers to the components.");

            inputResponseListRowComponents = (InputResponseListRowComponents)convertView.getTag();
        }

        Log.i(TAG, "getView(): Populating the row with data.");

        // And finally, populate the various row "container" components with data:
        InputResponseListRowData inputResponseListRowData = mData.get(position);
        inputResponseListRowComponents.txtTurn.setText(inputResponseListRowData.turn);
        inputResponseListRowComponents.txtResponse.setText(inputResponseListRowData.response);
        inputResponseListRowComponents.imgIcon1.setImageResource(inputResponseListRowData.icon1);
        inputResponseListRowComponents.imgIcon2.setImageResource(inputResponseListRowData.icon2);
        inputResponseListRowComponents.imgIcon3.setImageResource(inputResponseListRowData.icon3);
        inputResponseListRowComponents.imgIcon4.setImageResource(inputResponseListRowData.icon4);

        // The row is now ready so return it:
        return convertView;
    }

    /**
     * <p>A row "container" of the ListView's "row" UI components: Two textfields and four
     * images.</p>
     */
    static class InputResponseListRowComponents
    {
        TextView txtTurn;
        TextView txtResponse;
        ImageView imgIcon1;
        ImageView imgIcon2;
        ImageView imgIcon3;
        ImageView imgIcon4;
    }
}
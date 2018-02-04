package com.android.codebuster2.data;

/**
 * <p>This is the generic (all purpose) interface used by all "...DataRequest" classes. These
 * callback methods are the communications tool used by these classes to get query results from
 * the "DataRequestHelper" base class.</p>
 */

public interface IDataRequestCallback
{
    void onQueryTaskCompleted(String result);  // A successful return with data.
    void onQueryTaskError(String errorString); // An error occurred.
}
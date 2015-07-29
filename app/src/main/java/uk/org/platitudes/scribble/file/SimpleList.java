/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.file;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.scribble.R;

/**
 * Provides a greatly simplified wrapper for the ListView class. The list is assumed to
 * consist of a single, small, column of data.
 */
public class SimpleList implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    /**
     * The ListView that displays the data.
     */
    private ListView mListView;

    /**
     * SimpleAdapter expects the data to be encoded as an ArrayList. Each row of data consists
     * of a HashMap. Each HashMap contains the same set of keys with each key specifying a
     * column of data.
     *
     * In this class there is a single fixed key, providing data for a single fixed column.
     * The column is named column_0 and is taken from the file list_row_layout.xml.
     */
    private SimpleAdapter mSimpleAdapter;

    /**
     * Set true if the data is to be ordered. Subclass must override the isLessThan method
     * for this to work properly.
     */
    private boolean orderObjects;

    /**
     * The data as expected by the SimpleAdapter class.
     */
    private ArrayList<HashMap<String, Object>> mListContents;

    /**
     * The inflated view that contains the ListView object. The resource ID of the ListView
     * object must be supplied when this SimpleView is created.
     */
    protected View mParentView;

    /**
     * The fixed set of single column keys and destinations for each row of data as it appears
     * in the HashMaps.
     */
    private static final String[] from = {"dataKey"};
    private static final int[] to = {R.id.column_0};

    /**
     * Each data object is encapsulated in a DataHolder object. By overriding the getName()
     * method in the SimpleList class, subclasses can control the name of the object as it
     * will be displayed in the ListView.
     */
    private class DataHolder {
        Object data;
        String name;
        DataHolder (Object o, String s) {data = o; name = s;}
        public String toString() {return name;}
    }

    /**
     * Creates a SimpleList. The supplied View should already be inflated and should contain
     * the ListView object identified by the listResource parameter.
     * @param v                 The view containing the ListView.
     * @param listResource      The resource id of the ListView.
     */
    public SimpleList (View v, int listResource) {
        mParentView = v;

        mListContents = new ArrayList<>();
        mSimpleAdapter = new SimpleAdapter(
                v.getContext(),
                mListContents,
                R.layout.list_row_layout,
                from,
                to);
        mListView = (ListView) v.findViewById(listResource);
        mListView.setAdapter(mSimpleAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

    }

    /**
     * Sets the contents of the list using an array of objects. If the orderObjects flag is set
     * then the data will be ordered according to the isLessThan method for comparing two
     * objects. Each object will be displayed using the result of the getName method.
     *
     * @param newData   An array of objects. Each is wrapped in a DataHolder which is placed
     *                  in a HashMap as a row in the list.
     */
    public void setContents (Object[] newData) {
        mListContents.clear();

        if (orderObjects) {
            orderObjects(newData);
        }
        for (Object o : newData) {
            HashMap<String, Object> hm = new HashMap<>();
            String name = getName(o);
            DataHolder dh = new DataHolder(o, name);
            hm.put(from[0], dh);
            mListContents.add(hm);
        }

        mListView.invalidateViews();
        mSimpleAdapter.notifyDataSetChanged();
    }

    /**
     * Does a simple bubble sort on the supplied array. isLessThan() is used to compare
     * data entries in the array.
     *
     * @param data      An array of objects to be sorted.
     */
    private void orderObjects (Object[] data) {
        if (data == null) return;
        if (data.length < 2) return;

        for (int i=1; i<data.length; i++) {
            // On each pass of the loop, assume that all precious objects are already ordered.
            Object cur = data[i];
            for (int j=i-1; j >=0; j--) {
                // compare cur to all prev objects
                Object prev = data[j];
                if (isLessThan(cur,prev)) {
                    // swap down a position
                    data[j] = cur;
                    data[j+1] = prev;
                } else {
                    // cur is in correct place
                    break;
                }
            }
        }
    }

    /**
     * Used to compare two objects. Subclasses should override this if they set the list to
     * be ordered.
     *
     * @param a     The a in a<b comparison
     * @param b     The b in a<b comparison.
     * @return      Returns true if a<b.
     */
    public boolean isLessThan (Object a, Object b) {return false;}

    /**
     * Extracts the name of an object. The result is displayed in the ListView to identify the
     * object. Subclasses should override this if object.toString does not provide the correct
     * object name.
     */
    public String getName (Object o) {return o.toString();}

    /**
     * Called when an object in the list is clicked. By default it does nothing. Most subclasses
     * will override this.
     *
     * @param o     The object selected.
     */
    public void onClick (Object o) {}

    /**
     * Called when an object in the list is clicked. By default it does nothing. Most subclasses
     * will override this.
     *
     * @param o     The object selected.
     */
    public void onLongClick (Object o, View v) {}

    /**
     * Called with a parameter of true if the data should be ordered.
     */
    public void setOrderObjects(boolean order) {orderObjects = order;}

    /**
     * Handles ListView item clicks and invokes onClick (Object o).
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> rowHashmap = mListContents.get(position);
        DataHolder dh = (DataHolder) rowHashmap.get(from[0]);
        onClick(dh.data);
    }

    /**
     * Handles ListView item clicks and invokes onLongClick (Object o, View v).
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> rowHashmap = mListContents.get(position);
        DataHolder dh = (DataHolder) rowHashmap.get(from[0]);
        onLongClick(dh.data, view);
        return true;
    }


}

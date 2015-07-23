package uk.org.platitudes.scribble.file;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.scribble.R;

/**
 */
public class SimpleList implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private ArrayList<HashMap<String, Object>> mListContents;
    private TextView mListTitle;
    protected View mParentView;

    private static final String[] from = {"dataKey"};
    private int[] to = {0};

    public class DataHolder {
        Object data;
        String name;
        DataHolder (Object o, String s) {data = o; name = s;}
        public String toString() {return name;}
    }

    public SimpleList (View v, String title, int listResource, int rowResource) {
        mParentView = v;
        to[0] = rowResource;

        mListContents = new ArrayList<>();
        mSimpleAdapter = new SimpleAdapter(
                v.getContext(),
                mListContents,
                R.layout.device_list_row_layout,
                from,
                to);
        mListView = (ListView) v.findViewById(listResource);
        mListView.setAdapter(mSimpleAdapter);
        mListView.setOnItemClickListener(this);

        TextView titleView = (TextView) v.findViewById(R.id.device_title);
        titleView.setText(title);
    }

    public void setContents (Object[] newData) {
        mListContents.clear();

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

    public String getName (Object o) {return o.toString();}
    public void onClick (Object o) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> rowHashmap = mListContents.get(position);
        DataHolder dh = (DataHolder) rowHashmap.get(from[0]);
        onClick(dh.data);
    }
}
